/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.internal.mqtt.handler.subscribe;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import org.jctools.queues.MpscLinkedQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.annotations.CallByThread;
import org.mqttbee.internal.mqtt.MqttClientConnectionConfig;
import org.mqttbee.internal.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.internal.mqtt.exceptions.MqttClientStateExceptions;
import org.mqttbee.internal.mqtt.handler.MqttSessionAwareHandler;
import org.mqttbee.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.internal.mqtt.handler.publish.incoming.MqttIncomingPublishFlows;
import org.mqttbee.internal.mqtt.handler.publish.incoming.MqttSubscribedPublishFlow;
import org.mqttbee.internal.mqtt.ioc.ClientScope;
import org.mqttbee.internal.mqtt.message.MqttCommonReasonCode;
import org.mqttbee.internal.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.internal.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.internal.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.internal.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import org.mqttbee.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.internal.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.internal.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;
import org.mqttbee.internal.util.Ranges;
import org.mqttbee.internal.util.UnsignedDataTypes;
import org.mqttbee.internal.util.collections.ImmutableList;
import org.mqttbee.internal.util.collections.IntMap;
import org.mqttbee.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import org.mqttbee.mqtt.mqtt5.exceptions.Mqtt5UnsubAckException;
import org.mqttbee.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttSubscriptionHandler extends MqttSessionAwareHandler implements Runnable {

    public static final @NotNull String NAME = "subscription";
    public static final int MAX_SUB_PENDING = 10; // TODO configurable
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(MqttSubscriptionHandler.class);

    private final @NotNull MqttIncomingPublishFlows incomingPublishFlows;

    private final @NotNull MpscLinkedQueue<MqttSubOrUnsubWithFlow> queued = MpscLinkedQueue.newMpscLinkedQueue();
    private final @NotNull AtomicInteger queuedCounter = new AtomicInteger();
    private final @NotNull IntMap<MqttSubOrUnsubWithFlow.Stateful> pending;
    private final @NotNull Ranges packetIdentifiers;
    private @Nullable Ranges subscriptionIdentifiers;

    private int currentWrite;

    @Inject
    MqttSubscriptionHandler(final @NotNull MqttIncomingPublishFlows incomingPublishFlows) {
        this.incomingPublishFlows = incomingPublishFlows;

        final int maxPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
        final int minPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MAX_SUB_PENDING + 1;
        pending = IntMap.range(minPacketIdentifier, maxPacketIdentifier);
        packetIdentifiers = new Ranges(minPacketIdentifier, maxPacketIdentifier);
    }

    @Override
    public void onSessionStartOrResume(final @NotNull MqttClientConnectionConfig connectionConfig) {
        super.onSessionStartOrResume(connectionConfig);
        if (connectionConfig.areSubscriptionIdentifiersAvailable() && (subscriptionIdentifiers == null)) {
            subscriptionIdentifiers = new Ranges(1, MqttVariableByteInteger.FOUR_BYTES_MAX_VALUE);
        }
    }

    public void subscribe(
            final @NotNull MqttSubscribe subscribe, final @NotNull MqttSubscriptionFlow<MqttSubAck> flow) {

        queued.offer(new MqttSubscribeWithFlow(subscribe, flow));
        execute(flow.getEventLoop());
    }

    public void unsubscribe(
            final @NotNull MqttUnsubscribe unsubscribe, final @NotNull MqttSubOrUnsubAckFlow<MqttUnsubAck> flow) {

        queued.offer(new MqttUnsubscribeWithFlow(unsubscribe, flow));
        execute(flow.getEventLoop());
    }

    private void execute(final @NotNull EventLoop eventLoop) {
        if (queuedCounter.getAndIncrement() == 0) {
            eventLoop.execute(this);
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void run() {
        if (!hasSession) {
            clearQueued(MqttClientStateExceptions.notConnected());
            return;
        }
        final ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            return;
        }
        int removedFromQueue = 0;
        while (true) {
            if (pending.size() == MAX_SUB_PENDING) {
                queuedCounter.getAndAdd(-removedFromQueue);
                return;
            }
            final MqttSubOrUnsubWithFlow subOrUnsubWithFlow = queued.poll();
            if (subOrUnsubWithFlow == null) {
                if (queuedCounter.addAndGet(-removedFromQueue) == 0) {
                    return;
                } else {
                    removedFromQueue = 0;
                    continue;
                }
            }
            final int packetIdentifier = packetIdentifiers.getId();
            if (packetIdentifier == -1) {
                LOGGER.error("No Packet Identifier available for (UN)SUBSCRIBE. This must not happen and is a bug.");
                return;
            }
            writeSubscribeOrUnsubscribe(ctx, subOrUnsubWithFlow, packetIdentifier);
            removedFromQueue++;
        }
    }

    private void writeSubscribeOrUnsubscribe(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttSubOrUnsubWithFlow subOrUnsubWithFlow,
            final int packetIdentifier) {

        if (subOrUnsubWithFlow instanceof MqttSubscribeWithFlow) {
            writeSubscribe(ctx, (MqttSubscribeWithFlow) subOrUnsubWithFlow, packetIdentifier);
        } else {
            writeUnsubscribe(ctx, (MqttUnsubscribeWithFlow) subOrUnsubWithFlow, packetIdentifier);
        }
    }

    private void writeSubscribe(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttSubscribeWithFlow subscribeWithFlow,
            final int packetIdentifier) {

        final MqttSubscriptionFlow<MqttSubAck> flow = subscribeWithFlow.getFlow();
        if (!flow.init()) {
            return;
        }

        final int subscriptionIdentifier = (subscriptionIdentifiers != null) ? subscriptionIdentifiers.getId() :
                MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;
        final MqttStatefulSubscribe statefulSubscribe =
                subscribeWithFlow.getMessage().createStateful(packetIdentifier, subscriptionIdentifier);

        final MqttSubscribeWithFlow.Stateful statefulSubscribeWithFlow =
                new MqttSubscribeWithFlow.Stateful(statefulSubscribe, flow);
        pending.put(packetIdentifier, statefulSubscribeWithFlow);

        currentWrite = packetIdentifier;
        ctx.writeAndFlush(statefulSubscribeWithFlow.getMessage(), ctx.voidPromise());
        if (currentWrite != -1) { // no exception was handled
            incomingPublishFlows.subscribe(statefulSubscribe, subscribeWithFlow.getPublishFlow());
            currentWrite = -1;
        }
    }

    private void writeUnsubscribe(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttUnsubscribeWithFlow unsubscribeWithFlow,
            final int packetIdentifier) {

        final MqttSubOrUnsubAckFlow<MqttUnsubAck> flow = unsubscribeWithFlow.getFlow();
        if (!flow.init()) {
            return;
        }

        final MqttStatefulUnsubscribe statefulUnsubscribe =
                unsubscribeWithFlow.getMessage().createStateful(packetIdentifier);

        final MqttUnsubscribeWithFlow.Stateful statefulUnsubscribeWithFlow =
                new MqttUnsubscribeWithFlow.Stateful(statefulUnsubscribe, flow);
        pending.put(packetIdentifier, statefulUnsubscribeWithFlow);

        currentWrite = packetIdentifier;
        ctx.writeAndFlush(statefulUnsubscribeWithFlow.getMessage(), ctx.voidPromise());
        currentWrite = -1;
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof MqttSubAck) {
            readSubAck(ctx, (MqttSubAck) msg);
        } else if (msg instanceof MqttUnsubAck) {
            readUnsubAck(ctx, (MqttUnsubAck) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readSubAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttSubAck subAck) {
        final int packetIdentifier = subAck.getPacketIdentifier();
        final MqttSubOrUnsubWithFlow.Stateful statefulSubOrUnsubWithFlow = pending.remove(packetIdentifier);

        if (statefulSubOrUnsubWithFlow == null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Unknown packet identifier for SUBACK");
            return;
        }
        if (!(statefulSubOrUnsubWithFlow instanceof MqttSubscribeWithFlow.Stateful)) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "SUBACK received for an UNSUBSCRIBE");
            return;
        }
        final MqttSubscribeWithFlow.Stateful statefulSubscribeWithFlow =
                (MqttSubscribeWithFlow.Stateful) statefulSubOrUnsubWithFlow;
        final MqttStatefulSubscribe subscribe = statefulSubscribeWithFlow.getMessage();
        final MqttSubscriptionFlow<MqttSubAck> flow = statefulSubscribeWithFlow.getFlow();

        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
        final boolean countNotMatching = subscribe.stateless().getSubscriptions().size() != reasonCodes.size();
        final boolean allErrors = MqttCommonReasonCode.allErrors(subAck.getReasonCodes());

        incomingPublishFlows.subAck(subscribe, subAck, statefulSubscribeWithFlow.getPublishFlow());

        if (!(countNotMatching || allErrors)) {
            if (!flow.isCancelled()) {
                flow.onSuccess(subAck);
            } else {
                LOGGER.warn("Subscribe was successful but the SubAck flow has been cancelled");
            }
        } else {
            final String errorMessage;
            if (countNotMatching) {
                errorMessage = "Count of Reason Codes in SUBACK does not match count of subscriptions in SUBSCRIBE";
            } else { // allErrors
                errorMessage = "SUBACK contains only Error Codes";
            }
            if (!flow.isCancelled()) {
                flow.onError(new Mqtt5SubAckException(subAck, errorMessage));
            } else {
                LOGGER.warn(errorMessage + " but the SubAck flow has been cancelled");
            }
        }

        handleComplete(ctx, packetIdentifier);
    }

    private void readUnsubAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttUnsubAck unsubAck) {
        final int packetIdentifier = unsubAck.getPacketIdentifier();
        final MqttSubOrUnsubWithFlow.Stateful statefulSubOrUnsubWithFlow = pending.remove(packetIdentifier);

        if (statefulSubOrUnsubWithFlow == null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Unknown packet identifier for UNSUBACK");
            return;
        }
        if (!(statefulSubOrUnsubWithFlow instanceof MqttUnsubscribeWithFlow.Stateful)) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "UNSUBACK received for a SUBSCRIBE");
            return;
        }
        final MqttUnsubscribeWithFlow.Stateful statefulUnsubscribeWithFlow =
                (MqttUnsubscribeWithFlow.Stateful) statefulSubOrUnsubWithFlow;
        final MqttStatefulUnsubscribe unsubscribe = statefulUnsubscribeWithFlow.getMessage();
        final MqttSubOrUnsubAckFlow<MqttUnsubAck> flow = statefulUnsubscribeWithFlow.getFlow();

        final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes = unsubAck.getReasonCodes();
        final boolean countNotMatching = unsubscribe.stateless().getTopicFilters().size() != reasonCodes.size();
        final boolean allErrors = MqttCommonReasonCode.allErrors(unsubAck.getReasonCodes());

        if ((reasonCodes == Mqtt3UnsubAckView.REASON_CODES_ALL_SUCCESS) || !(countNotMatching || allErrors)) {

            incomingPublishFlows.unsubscribe(unsubscribe, unsubAck);

            if (!flow.isCancelled()) {
                flow.onSuccess(unsubAck);
            } else {
                LOGGER.warn("Unsubscribe was successful but the UnsubAck flow has been cancelled");
            }
        } else {
            final String errorMessage;
            if (countNotMatching) {
                errorMessage = "Count of Reason Codes in UNSUBACK does not match count of Topic Filters in UNSUBSCRIBE";
            } else { // allErrors
                errorMessage = "UNSUBACK contains only Error Codes";
            }
            if (!flow.isCancelled()) {
                flow.onError(new Mqtt5UnsubAckException(unsubAck, errorMessage));
            } else {
                LOGGER.warn(errorMessage + " but the UnsubAck flow has been cancelled");
            }
        }

        handleComplete(ctx, packetIdentifier);
    }

    private void handleComplete(final @NotNull ChannelHandlerContext ctx, final int packetIdentifier) {
        final MqttSubOrUnsubWithFlow subOrUnsubWithFlow = queued.poll();
        if (subOrUnsubWithFlow == null) {
            packetIdentifiers.returnId(packetIdentifier);
        } else {
            queuedCounter.getAndDecrement();
            writeSubscribeOrUnsubscribe(ctx, subOrUnsubWithFlow, packetIdentifier);
        }
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        if (!(cause instanceof IOException) && (currentWrite != -1)) {
            final MqttSubOrUnsubWithFlow.Stateful statefulSubOrUnsubWithFlow = pending.remove(currentWrite);
            assert statefulSubOrUnsubWithFlow != null;
            statefulSubOrUnsubWithFlow.getFlow().onError(cause);
            handleComplete(ctx, currentWrite);
            currentWrite = -1;
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }

    @Override
    public void onSessionEnd(final @NotNull Throwable cause) {
        super.onSessionEnd(cause);

        pending.forEach((packetIdentifier, statefulSubOrUnsubWithFlow) -> {
            packetIdentifiers.returnId(statefulSubOrUnsubWithFlow.getMessage().getPacketIdentifier());
            if (!(statefulSubOrUnsubWithFlow.getFlow() instanceof MqttSubscribedPublishFlow)) {
                statefulSubOrUnsubWithFlow.getFlow().onError(cause);
            } // else flow.onError is already called via incomingPublishFlows.clear() in IncomingQosHandler
            return true;
        });
        pending.clear();
        subscriptionIdentifiers = null;

        clearQueued(cause);
    }

    private void clearQueued(final @NotNull Throwable cause) {
        int polled = 0;
        while (true) {
            final MqttSubOrUnsubWithFlow subOrUnsubWithFlow = queued.poll();
            if (subOrUnsubWithFlow == null) {
                if (queuedCounter.addAndGet(-polled) == 0) {
                    break;
                } else {
                    polled = 0;
                    continue;
                }
            }
            if (subOrUnsubWithFlow.getFlow().init()) {
                subOrUnsubWithFlow.getFlow().onError(cause);
            }
            polled++;
        }
    }
}
