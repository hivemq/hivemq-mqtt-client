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

import com.google.common.collect.ImmutableList;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import org.jctools.queues.MpscLinkedQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.api.mqtt.MqttClientState;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.MqttServerConnectionConfig;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.publish.incoming.MqttIncomingPublishFlows;
import org.mqttbee.mqtt.handler.publish.incoming.MqttSubscriptionFlow;
import org.mqttbee.mqtt.handler.subscribe.MqttSubscribeWithFlow.MqttStatefulSubscribeWithFlow;
import org.mqttbee.mqtt.handler.subscribe.MqttUnsubscribeWithFlow.MqttStatefulUnsubscribeWithFlow;
import org.mqttbee.mqtt.ioc.ClientScope;
import org.mqttbee.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;
import org.mqttbee.rx.SingleFlow;
import org.mqttbee.util.Ranges;
import org.mqttbee.util.UnsignedDataTypes;
import org.mqttbee.util.collections.IntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttSubscriptionHandler extends ChannelInboundHandlerAdapter implements Runnable {

    public static final @NotNull String NAME = "subscription";
    public static final int MAX_SUB_PENDING = 10; // TODO configurable
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(MqttSubscriptionHandler.class);

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttIncomingPublishFlows subscriptionFlows;

    private final @NotNull MpscLinkedQueue<Object> queued = MpscLinkedQueue.newMpscLinkedQueue();
    // contains Mqtt(Un)SubscribeWithFlow
    private final @NotNull AtomicInteger queuedCounter = new AtomicInteger();
    private final @NotNull IntMap<Object> pending; // contains MqttStateful(Un)SubscribeWithFlow
    private final @NotNull Ranges packetIdentifiers;
    private final @NotNull Ranges subscriptionIdentifiers;

    private @Nullable ChannelHandlerContext ctx;
    private boolean subscriptionIdentifiersAvailable;

    @Inject
    MqttSubscriptionHandler(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttIncomingPublishFlows subscriptionFlows) {

        this.clientConfig = clientConfig;
        this.subscriptionFlows = subscriptionFlows;

        final int maxPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
        final int minPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MAX_SUB_PENDING + 1;
        pending = IntMap.range(minPacketIdentifier, maxPacketIdentifier);
        packetIdentifiers = new Ranges(minPacketIdentifier, maxPacketIdentifier);
        subscriptionIdentifiers = new Ranges(1, MqttVariableByteInteger.FOUR_BYTES_MAX_VALUE);
    }

    @Override
    public void handlerAdded(final @NotNull ChannelHandlerContext ctx) {
        this.ctx = ctx;

        final MqttServerConnectionConfig serverConnectionConfig = clientConfig.getRawServerConnectionConfig();
        assert serverConnectionConfig != null;

        subscriptionIdentifiersAvailable = serverConnectionConfig.areSubscriptionIdentifiersAvailable();
    }

    public void subscribe(final @NotNull MqttSubscribeWithFlow subscribeWithFlow) {
        queued.offer(subscribeWithFlow);
        if (queuedCounter.getAndIncrement() == 0) {
            clientConfig.executeInEventLoop(this);
        }
    }

    public void subscribe(final @NotNull MqttSubscribeWithFlow subscribeWithFlow, final @NotNull EventLoop eventLoop) {
        queued.offer(subscribeWithFlow);
        if (queuedCounter.getAndIncrement() == 0) {
            eventLoop.execute(this);
        }
    }

    public void unsubscribe(final @NotNull MqttUnsubscribeWithFlow unsubscribeWithFlow) {
        queued.offer(unsubscribeWithFlow);
        if (queuedCounter.getAndIncrement() == 0) {
            clientConfig.executeInEventLoop(this);
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void run() {
        if (ctx == null) {
            clear(new NotConnectedException());
            return;
        }
        int newPending = 0;
        while (true) {
            if (pending.size() == MAX_SUB_PENDING) {
                queuedCounter.getAndAdd(-newPending);
                return;
            }
            final Object subscribeOrUnsubscribe = queued.poll();
            if (subscribeOrUnsubscribe == null) {
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
            writeSubscribeOrUnsubscribe(ctx, subscribeOrUnsubscribe, packetIdentifier);
            newPending++;
        }
    }

    private void writeSubscribeOrUnsubscribe(
            final @NotNull ChannelHandlerContext ctx, final @NotNull Object subscribeOrUnsubscribe,
            final int packetIdentifier) {

        if (subscribeOrUnsubscribe instanceof MqttSubscribeWithFlow) {
            writeSubscribe(ctx, (MqttSubscribeWithFlow) subscribeOrUnsubscribe, packetIdentifier);
        } else {
            writeUnsubscribe(ctx, (MqttUnsubscribeWithFlow) subscribeOrUnsubscribe, packetIdentifier);
        }
    }

    private void writeSubscribe(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttSubscribeWithFlow subscribeWithFlow,
            final int packetIdentifier) {

        final int subscriptionIdentifier = (subscriptionIdentifiersAvailable) ? subscriptionIdentifiers.getId() :
                MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;
        final MqttStatefulSubscribeWithFlow statefulSubscribeWithFlow =
                subscribeWithFlow.createStateful(packetIdentifier, subscriptionIdentifier);
        pending.put(packetIdentifier, statefulSubscribeWithFlow);
        ctx.writeAndFlush(statefulSubscribeWithFlow.getSubscribe(), ctx.voidPromise());
    }

    private void writeUnsubscribe(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttUnsubscribeWithFlow unsubscribeWithFlow,
            final int packetIdentifier) {

        final MqttStatefulUnsubscribeWithFlow statefulUnsubscribeWithFlow =
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
        final Object subscribeOrUnsubscribe = pending.remove(packetIdentifier);

        if (subscribeOrUnsubscribe == null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Unknown packet identifier for SUBACK");
            return;
        }
        if (!(subscribeOrUnsubscribe instanceof MqttStatefulSubscribeWithFlow)) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "SUBACK received for an UNSUBSCRIBE");
            return;
        }
        final MqttStatefulSubscribeWithFlow statefulSubscribeWithFlow =
                (MqttStatefulSubscribeWithFlow) subscribeOrUnsubscribe;
        final MqttStatefulSubscribe subscribe = statefulSubscribeWithFlow.getSubscribe();
        final SingleFlow<Mqtt5SubAck> subAckFlow = statefulSubscribeWithFlow.getSubAckFlow();
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
        final Object subscribeOrUnsubscribe = pending.remove(packetIdentifier);

        if (subscribeOrUnsubscribe == null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Unknown packet identifier for UNSUBACK");
            return;
        }
        if (!(subscribeOrUnsubscribe instanceof MqttStatefulUnsubscribeWithFlow)) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "UNSUBACK received for a SUBSCRIBE");
            return;
        }
        final MqttStatefulUnsubscribeWithFlow statefulUnsubscribeWithFlow =
                (MqttStatefulUnsubscribeWithFlow) subscribeOrUnsubscribe;
        final MqttStatefulUnsubscribe unsubscribe = statefulUnsubscribeWithFlow.getUnsubscribe();
        final SingleFlow<Mqtt5UnsubAck> unsubAckFlow = statefulUnsubscribeWithFlow.getUnsubAckFlow();
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
        final Object subscribeOrUnsubscribe = queued.poll();
        if (subscribeOrUnsubscribe == null) {
            packetIdentifiers.returnId(packetIdentifier);
        } else {
            queuedCounter.getAndDecrement();
            writeSubscribeOrUnsubscribe(ctx, subscribeOrUnsubscribe, packetIdentifier);
        }
    }

    @Override
    public void userEventTriggered(final @NotNull ChannelHandlerContext ctx, final @NotNull Object evt) {
        if (evt instanceof MqttDisconnectEvent) {
            handleDisconnectEvent((MqttDisconnectEvent) evt);
        }
        ctx.fireUserEventTriggered(evt);
    }

    private void handleDisconnectEvent(final @NotNull MqttDisconnectEvent disconnectEvent) {
        ctx = null;
        clear(disconnectEvent.getCause());
    }

    private void clear(final @NotNull Throwable cause) {
        if (clientConfig.getState() != MqttClientState.DISCONNECTED) {
            return;
        }

        pending.forEach((packetIdentifier, subscribeOrUnsubscribe) -> {
            if (subscribeOrUnsubscribe instanceof MqttStatefulSubscribeWithFlow) {
                ((MqttStatefulSubscribeWithFlow) subscribeOrUnsubscribe).getSubAckFlow().onError(cause);
            } else {
                ((MqttStatefulUnsubscribeWithFlow) subscribeOrUnsubscribe).getUnsubAckFlow().onError(cause);
            }
            return true;
        });
        pending.clear();

        int polled = 0;
        while (true) {
            final Object subscribeOrUnsubscribe = queued.poll();
            if (subscribeOrUnsubscribe == null) {
                if (queuedCounter.addAndGet(-polled) == 0) {
                    break;
                } else {
                    polled = 0;
                    continue;
                }
            }
            if (subscribeOrUnsubscribe instanceof MqttSubscribeWithFlow) {
                ((MqttSubscribeWithFlow) subscribeOrUnsubscribe).getSubAckFlow().onError(cause);
            } else {
                ((MqttUnsubscribeWithFlow) subscribeOrUnsubscribe).getUnsubAckFlow().onError(cause);
            }
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

    @Override
    public boolean isSharable() {
        return ctx == null;
    }
}
