/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.handler.subscribe;

import com.hivemq.client.internal.annotations.CallByThread;
import com.hivemq.client.internal.logging.InternalLogger;
import com.hivemq.client.internal.logging.InternalLoggerFactory;
import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client.internal.mqtt.handler.MqttSessionAwareHandler;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttIncomingPublishFlows;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttSubscribedPublishFlow;
import com.hivemq.client.internal.mqtt.ioc.ClientScope;
import com.hivemq.client.internal.mqtt.message.MqttCommonReasonCode;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttStatefulSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.suback.MqttSubAck;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client.internal.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import com.hivemq.client.internal.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;
import com.hivemq.client.internal.util.Ranges;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.internal.util.collections.IntMap;
import com.hivemq.client.internal.util.collections.NodeList;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5UnsubAckException;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttSubscriptionHandler extends MqttSessionAwareHandler implements Runnable {

    public static final @NotNull String NAME = "subscription";
    private static final @NotNull InternalLogger LOGGER =
            InternalLoggerFactory.getLogger(MqttSubscriptionHandler.class);
    private static final @NotNull ToIntFunction<MqttSubOrUnsubWithFlow.Stateful> ID_FUNCTION =
            x -> x.getMessage().getPacketIdentifier();
    public static final int MAX_SUB_PENDING = 10; // TODO configurable

    private final @NotNull MqttIncomingPublishFlows incomingPublishFlows;

    private final @NotNull ConcurrentLinkedQueue<MqttSubOrUnsubWithFlow> queued = new ConcurrentLinkedQueue<>();
    private final @NotNull AtomicInteger queuedCounter = new AtomicInteger();
    private final @NotNull IntMap<MqttSubOrUnsubWithFlow.Stateful> pendingMap = new IntMap<>(ID_FUNCTION);
    private final @NotNull NodeList<MqttSubOrUnsubWithFlow.Stateful> pending = new NodeList<>();
    private final @NotNull Ranges packetIdentifiers;

    private @Nullable MqttSubOrUnsubWithFlow.Stateful resendPending, currentPending;
    private @Nullable Ranges subscriptionIdentifiers;

    @Inject
    MqttSubscriptionHandler(final @NotNull MqttIncomingPublishFlows incomingPublishFlows) {
        this.incomingPublishFlows = incomingPublishFlows;

        final int maxPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
        final int minPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MAX_SUB_PENDING + 1;
        packetIdentifiers = new Ranges(minPacketIdentifier, maxPacketIdentifier);
    }

    @Override
    public void onSessionStartOrResume(
            final @NotNull MqttClientConnectionConfig connectionConfig, final @NotNull EventLoop eventLoop) {

        super.onSessionStartOrResume(connectionConfig, eventLoop);
        if (connectionConfig.areSubscriptionIdentifiersAvailable() && (subscriptionIdentifiers == null)) {
            subscriptionIdentifiers = new Ranges(1, MqttVariableByteInteger.FOUR_BYTES_MAX_VALUE);
        }
        if ((pending.getFirst() != null) || (queuedCounter.get() > 0)) {
            resendPending = pending.getFirst();
            eventLoop.execute(this);
        }
    }

    public void subscribe(
            final @NotNull MqttSubscribe subscribe, final @NotNull MqttSubscriptionFlow<MqttSubAck> flow) {

        queue(new MqttSubscribeWithFlow(subscribe, flow));
    }

    public void unsubscribe(
            final @NotNull MqttUnsubscribe unsubscribe, final @NotNull MqttSubOrUnsubAckFlow<MqttUnsubAck> flow) {

        queue(new MqttUnsubscribeWithFlow(unsubscribe, flow));
    }

    private void queue(final @NotNull MqttSubOrUnsubWithFlow subOrUnsubWithFlow) {
        queued.offer(subOrUnsubWithFlow);
        if (queuedCounter.getAndIncrement() == 0) {
            subOrUnsubWithFlow.getFlow().getEventLoop().execute(this);
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
        for (; resendPending != null; resendPending = resendPending.getNext()) {
            if (resendPending instanceof MqttSubscribeWithFlow.Stateful) {
                writeSubscribe(ctx, (MqttSubscribeWithFlow.Stateful) resendPending);
            } else {
                writeUnsubscribe(ctx, (MqttUnsubscribeWithFlow.Stateful) resendPending);
            }
        }
        int removedFromQueue = 0;
        while (true) {
            if (pendingMap.size() == MAX_SUB_PENDING) {
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

        if (!subOrUnsubWithFlow.getFlow().init()) {
            return;
        }

        if (subOrUnsubWithFlow instanceof MqttSubscribeWithFlow) {
            final MqttSubscribeWithFlow subscribeWithFlow = (MqttSubscribeWithFlow) subOrUnsubWithFlow;

            final int subscriptionIdentifier = (subscriptionIdentifiers != null) ? subscriptionIdentifiers.getId() :
                    MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;
            final MqttStatefulSubscribe statefulSubscribe =
                    subscribeWithFlow.getMessage().createStateful(packetIdentifier, subscriptionIdentifier);

            final MqttSubscribeWithFlow.Stateful statefulSubscribeWithFlow =
                    new MqttSubscribeWithFlow.Stateful(statefulSubscribe, subscribeWithFlow.getFlow());

            addPending(statefulSubscribeWithFlow);

            if (writeSubscribe(ctx, statefulSubscribeWithFlow)) {
                incomingPublishFlows.subscribe(statefulSubscribe, statefulSubscribeWithFlow.getPublishFlow());
            }
        } else {
            final MqttUnsubscribeWithFlow unsubscribeWithFlow = (MqttUnsubscribeWithFlow) subOrUnsubWithFlow;

            final MqttStatefulUnsubscribe statefulUnsubscribe =
                    unsubscribeWithFlow.getMessage().createStateful(packetIdentifier);

            final MqttUnsubscribeWithFlow.Stateful statefulUnsubscribeWithFlow =
                    new MqttUnsubscribeWithFlow.Stateful(statefulUnsubscribe, unsubscribeWithFlow.getFlow());

            addPending(statefulUnsubscribeWithFlow);

            writeUnsubscribe(ctx, statefulUnsubscribeWithFlow);
        }
    }

    private void addPending(final @NotNull MqttSubOrUnsubWithFlow.Stateful newPending) {
        pendingMap.put(newPending);
        pending.add(newPending);
    }

    private boolean writeSubscribe(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull MqttSubscribeWithFlow.Stateful statefulSubscribeWithFlow) {

        final MqttStatefulSubscribe statefulSubscribe = statefulSubscribeWithFlow.getMessage();
        currentPending = statefulSubscribeWithFlow;
        ctx.writeAndFlush(statefulSubscribe, ctx.voidPromise());
        if (currentPending == null) { // exception was handled
            return false;
        }
        currentPending = null;
        return true;
    }

    private void writeUnsubscribe(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull MqttUnsubscribeWithFlow.Stateful statefulUnsubscribeWithFlow) {

        currentPending = statefulUnsubscribeWithFlow;
        ctx.writeAndFlush(statefulUnsubscribeWithFlow.getMessage(), ctx.voidPromise());
        currentPending = null;
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
        final MqttSubOrUnsubWithFlow.Stateful statefulSubOrUnsubWithFlow = pendingMap.remove(packetIdentifier);

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

        completePending(ctx, statefulSubscribeWithFlow);
    }

    private void readUnsubAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttUnsubAck unsubAck) {
        final int packetIdentifier = unsubAck.getPacketIdentifier();
        final MqttSubOrUnsubWithFlow.Stateful statefulSubOrUnsubWithFlow = pendingMap.remove(packetIdentifier);

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

        completePending(ctx, statefulUnsubscribeWithFlow);
    }

    private void completePending(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttSubOrUnsubWithFlow.Stateful oldPending) {

        pending.remove(oldPending);

        final int packetIdentifier = oldPending.getMessage().getPacketIdentifier();
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
        if (!(cause instanceof IOException) && (currentPending != null)) {
            pendingMap.remove(currentPending.getMessage().getPacketIdentifier());
            currentPending.getFlow().onError(cause);
            completePending(ctx, currentPending);
            currentPending = null;
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }

    @Override
    public void onSessionEnd(final @NotNull Throwable cause) {
        super.onSessionEnd(cause);

        for (MqttSubOrUnsubWithFlow.Stateful current = pending.getFirst(); current != null;
             current = current.getNext()) {
            packetIdentifiers.returnId(current.getMessage().getPacketIdentifier());
            if (!(current.getFlow() instanceof MqttSubscribedPublishFlow)) {
                current.getFlow().onError(cause);
            } // else flow.onError is already called via incomingPublishFlows.clear() in IncomingQosHandler
        }
        pendingMap.clear();
        pending.clear();
        resendPending = null;
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
