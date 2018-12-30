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

package org.mqttbee.mqtt.handler.subscribe;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import org.jctools.queues.MpscLinkedQueue;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionConfig;
import org.mqttbee.mqtt.MqttServerConnectionConfig;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.exceptions.MqttClientStateExceptions;
import org.mqttbee.mqtt.handler.MqttSessionAwareHandler;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.publish.incoming.MqttIncomingPublishFlows;
import org.mqttbee.mqtt.handler.publish.incoming.MqttSubscriptionFlow;
import org.mqttbee.mqtt.ioc.ClientScope;
import org.mqttbee.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;
import org.mqttbee.rx.SingleFlow;
import org.mqttbee.util.Ranges;
import org.mqttbee.util.UnsignedDataTypes;
import org.mqttbee.util.collections.ImmutableList;
import org.mqttbee.util.collections.IntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttSubscriptionHandler extends MqttSessionAwareHandler implements Runnable {

    public static final @NotNull String NAME = "subscription";
    public static final int MAX_SUB_PENDING = 10; // TODO configurable
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(MqttSubscriptionHandler.class);

    private final @NotNull MqttIncomingPublishFlows subscriptionFlows;

    private final @NotNull MpscLinkedQueue<MqttSubOrUnsubWithFlow> queued = MpscLinkedQueue.newMpscLinkedQueue();
    private final @NotNull AtomicInteger queuedCounter = new AtomicInteger();
    private final @NotNull IntMap<MqttSubOrUnsubWithFlow.Stateful> pending;
    private final @NotNull Ranges packetIdentifiers;
    private final @NotNull Ranges subscriptionIdentifiers;

    private boolean subscriptionIdentifiersAvailable;

    @Inject
    MqttSubscriptionHandler(final @NotNull MqttIncomingPublishFlows subscriptionFlows) {
        this.subscriptionFlows = subscriptionFlows;

        final int maxPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
        final int minPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MAX_SUB_PENDING + 1;
        pending = IntMap.range(minPacketIdentifier, maxPacketIdentifier);
        packetIdentifiers = new Ranges(minPacketIdentifier, maxPacketIdentifier);
        subscriptionIdentifiers = new Ranges(1, MqttVariableByteInteger.FOUR_BYTES_MAX_VALUE);
    }

    @Override
    public void onSessionStartOrResume(
            final @NotNull MqttClientConnectionConfig clientConnectionConfig,
            final @NotNull MqttServerConnectionConfig serverConnectionConfig) {

        super.onSessionStartOrResume(clientConnectionConfig, serverConnectionConfig);
        subscriptionIdentifiersAvailable = serverConnectionConfig.areSubscriptionIdentifiersAvailable();
    }

    public void subscribe(
            final @NotNull MqttSubscribe subscribe, final @NotNull MqttSubOrUnsubAckFlow<MqttSubAck> flow) {

        queued.offer(new MqttSubscribeWithFlow(subscribe, flow));
        execute(flow.getEventLoop());
    }

    public void subscribe(final @NotNull MqttSubscribe subscribe, final @NotNull MqttSubscriptionFlow flow) {
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
        if (ctx == null) {
            return;
        }
        int newPending = 0;
        while (true) {
            if (pending.size() == MAX_SUB_PENDING) {
                queuedCounter.getAndAdd(-newPending);
                return;
            }
            final MqttSubOrUnsubWithFlow subOrUnsubWithFlow = queued.poll();
            if (subOrUnsubWithFlow == null) {
                if (queuedCounter.addAndGet(-newPending) == 0) {
                    return;
                } else {
                    newPending = 0;
                    continue;
                }
            }
            final int packetIdentifier = packetIdentifiers.getId();
            if (packetIdentifier == -1) {
                LOGGER.error("No Packet Identifier available for (UN)SUBSCRIBE. This must not happen and is a bug.");
                return;
            }
            writeSubscribeOrUnsubscribe(ctx, subOrUnsubWithFlow, packetIdentifier);
            newPending++;
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

        final int subscriptionIdentifier = (subscriptionIdentifiersAvailable) ? subscriptionIdentifiers.getId() :
                MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;
        final MqttSubscribeWithFlow.Stateful statefulSubscribeWithFlow =
                subscribeWithFlow.createStateful(packetIdentifier, subscriptionIdentifier);
        pending.put(packetIdentifier, statefulSubscribeWithFlow);
        ctx.writeAndFlush(statefulSubscribeWithFlow.getSubscribe(), ctx.voidPromise());
    }

    private void writeUnsubscribe(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttUnsubscribeWithFlow unsubscribeWithFlow,
            final int packetIdentifier) {

        final MqttUnsubscribeWithFlow.Stateful statefulUnsubscribeWithFlow =
                unsubscribeWithFlow.createStateful(packetIdentifier);
        pending.put(packetIdentifier, statefulUnsubscribeWithFlow);
        ctx.writeAndFlush(statefulUnsubscribeWithFlow.getUnsubscribe(), ctx.voidPromise());
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
        final MqttStatefulSubscribe subscribe = statefulSubscribeWithFlow.getSubscribe();
        final SingleFlow<MqttSubAck> subAckFlow = statefulSubscribeWithFlow.getAckFlow();
        final int subscriptionCount = subscribe.stateless().getSubscriptions().size();
        final ReasonCodesState reasonCodesState = validateReasonCodes(subscriptionCount, subAck.getReasonCodes());

        if (reasonCodesState == ReasonCodesState.AT_LEAST_ONE_SUCCESSFUL) {
            if (!subAckFlow.isCancelled()) {
                subAckFlow.onSuccess(subAck);
            } else {
                LOGGER.warn("Subscribe was successful but the SubAckFlow has been cancelled");
            }
            MqttSubscriptionFlow subscriptionFlow = statefulSubscribeWithFlow.getSubscriptionFlow();
            if ((subscriptionFlow != null) && subscriptionFlow.isCancelled()) {
                subscriptionFlow = null;
            }
            subscriptionFlows.subscribe(subscribe, subAck, subscriptionFlow);
        } else {
            final String errorMessage;
            switch (reasonCodesState) {
                case COUNT_NOT_MATCHING:
                    errorMessage = "Count of Reason Codes in SUBACK does not match count of subscriptions in SUBSCRIBE";
                    break;
                case ALL_ERRORS:
                    errorMessage = "SUBACK contains only Error Codes";
                    break;
                default:
                    errorMessage = "Unknown error";
            }
            if (!subAckFlow.isCancelled()) {
                subAckFlow.onError(new Mqtt5MessageException(subAck, errorMessage));
            } else {
                LOGGER.warn(errorMessage + " but the SubAckFlow has been cancelled");
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
        final MqttStatefulUnsubscribe unsubscribe = statefulUnsubscribeWithFlow.getUnsubscribe();
        final SingleFlow<MqttUnsubAck> unsubAckFlow = statefulUnsubscribeWithFlow.getAckFlow();
        final int topicFilterCount = unsubscribe.stateless().getTopicFilters().size();
        final ReasonCodesState reasonCodesState = validateReasonCodes(topicFilterCount, unsubAck.getReasonCodes());

        if (reasonCodesState == ReasonCodesState.AT_LEAST_ONE_SUCCESSFUL) {
            if (!unsubAckFlow.isCancelled()) {
                unsubAckFlow.onSuccess(unsubAck);
            } else {
                LOGGER.warn("Unsubscribe was successful but the UnsubAckFlow has been cancelled");
            }
            subscriptionFlows.unsubscribe(unsubscribe, unsubAck);
        } else {
            final String errorMessage;
            switch (reasonCodesState) {
                case COUNT_NOT_MATCHING:
                    errorMessage =
                            "Count of Reason Codes in UNSUBACK does not match count of Topic Filters in UNSUBSCRIBE";
                    break;
                case ALL_ERRORS:
                    errorMessage = "UNSUBACK contains only Error Codes";
                    break;
                default:
                    errorMessage = "Unknown error";
            }
            if (!unsubAckFlow.isCancelled()) {
                unsubAckFlow.onError(new Mqtt5MessageException(unsubAck, errorMessage));
            } else {
                LOGGER.warn(errorMessage + " but the UnsubAckFlow has been cancelled");
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
    public void onSessionEnd(final @NotNull Throwable cause) {
        super.onSessionEnd(cause);

        pending.forEach((packetIdentifier, statefulSubOrUnsubWithFlow) -> {
            statefulSubOrUnsubWithFlow.getAckFlow().onError(cause);
            return true;
        });
        pending.clear();

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
            subOrUnsubWithFlow.getAckFlow().onError(cause);
            polled++;
        }
    }

    private enum ReasonCodesState {
        AT_LEAST_ONE_SUCCESSFUL,
        COUNT_NOT_MATCHING,
        ALL_ERRORS
    }

    private static @NotNull ReasonCodesState validateReasonCodes(
            final int count, final @NotNull ImmutableList<? extends Mqtt5ReasonCode> reasonCodes) {

        if (reasonCodes == Mqtt3UnsubAckView.REASON_CODES_ALL_SUCCESS) {
            return ReasonCodesState.AT_LEAST_ONE_SUCCESSFUL;
        }
        if (reasonCodes.size() != count) {
            return ReasonCodesState.COUNT_NOT_MATCHING;
        }
        for (final Mqtt5ReasonCode reasonCode : reasonCodes) {
            if (!reasonCode.isError()) {
                return ReasonCodesState.AT_LEAST_ONE_SUCCESSFUL;
            }
        }
        return ReasonCodesState.ALL_ERRORS;
    }
}
