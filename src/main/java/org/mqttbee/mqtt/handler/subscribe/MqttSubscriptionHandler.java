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
import org.jctools.queues.SpscLinkedQueue;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.handler.disconnect.ChannelCloseEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.publish.MqttIncomingPublishFlows;
import org.mqttbee.mqtt.handler.publish.MqttSubscriptionFlow;
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

    public static final String NAME = "subscription";
    public static final int MAX_SUB_PENDING = 10; // TODO configurable
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSubscriptionHandler.class);

    private enum ReasonCodesState {
        AT_LEAST_ONE_SUCCESSFUL,
        COUNT_NOT_MATCHING,
        ALL_ERRORS
    }

    private final MqttIncomingPublishFlows subscriptionFlows;

    private final SpscLinkedQueue<Object> queued = new SpscLinkedQueue<>(); // contains Mqtt(Un)SubscribeWithFlow
    private final AtomicInteger queuedCounter = new AtomicInteger();
    private final IntMap<Object> pending; // contains MqttStateful(Un)SubscribeWithFlow
    private final Ranges packetIdentifiers;
    private final Ranges subscriptionIdentifiers;

    private ChannelHandlerContext ctx;
    private volatile ChannelHandlerContext ctxVolatile; // TODO inject EventLoop/get from clientData

    @Inject
    MqttSubscriptionHandler(final MqttIncomingPublishFlows subscriptionFlows, final MqttClientData clientData) {
        this.subscriptionFlows = subscriptionFlows;

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        final int maxPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
        final int minPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MAX_SUB_PENDING + 1;
        pending = IntMap.range(minPacketIdentifier, maxPacketIdentifier);
        packetIdentifiers = new Ranges(minPacketIdentifier, maxPacketIdentifier);
        subscriptionIdentifiers = new Ranges(1, clientConnectionData.getSubscriptionIdentifierMaximum());
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
        ctxVolatile = ctx;
    }

    public void subscribe(@NotNull final MqttSubscribeWithFlow subscribeWithFlow) {
        queued.offer(subscribeWithFlow);
        trySchedule();
    }

    public void unsubscribe(@NotNull final MqttUnsubscribeWithFlow unsubscribeWithFlow) {
        queued.offer(unsubscribeWithFlow);
        trySchedule();
    }

    private void trySchedule() {
        if (queuedCounter.getAndIncrement() == 0) {
            ctxVolatile.executor().execute(this);
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void run() {
        if (ctx == null) {
            clear(new NotConnectedException());
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
                    continue;
                }
            }
            final int packetIdentifier = packetIdentifiers.getId();
            if (packetIdentifier == -1) {
                LOGGER.error("No Packet Identifier available for (UN)SUBSCRIBE. This must not happen and is a bug.");
                return;
            }
            writeSubscribeOrUnsubscribe(subscribeOrUnsubscribe, packetIdentifier);
            newPending++;
        }
    }

    private void writeSubscribeOrUnsubscribe(@NotNull final Object subscribeOrUnsubscribe, final int packetIdentifier) {
        if (subscribeOrUnsubscribe instanceof MqttSubscribeWithFlow) {
            writeSubscribe((MqttSubscribeWithFlow) subscribeOrUnsubscribe, packetIdentifier);
        } else {
            writeUnsubscribe((MqttUnsubscribeWithFlow) subscribeOrUnsubscribe, packetIdentifier);
        }
    }

    private void writeSubscribe(@NotNull final MqttSubscribeWithFlow subscribeWithFlow, final int packetIdentifier) {
        final MqttStatefulSubscribeWithFlow statefulSubscribeWithFlow =
                subscribeWithFlow.createStateful(packetIdentifier, subscriptionIdentifiers.getId());
        pending.put(packetIdentifier, statefulSubscribeWithFlow);
        ctx.writeAndFlush(statefulSubscribeWithFlow.getSubscribe(), ctx.voidPromise());
    }

    private void writeUnsubscribe(
            @NotNull final MqttUnsubscribeWithFlow unsubscribeWithFlow, final int packetIdentifier) {

        final MqttStatefulUnsubscribeWithFlow statefulUnsubscribeWithFlow =
                unsubscribeWithFlow.createStateful(packetIdentifier);
        pending.put(packetIdentifier, statefulUnsubscribeWithFlow);
        ctx.writeAndFlush(statefulUnsubscribeWithFlow.getUnsubscribe(), ctx.voidPromise());
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttSubAck) {
            handleSubAck(ctx, (MqttSubAck) msg);
        } else if (msg instanceof MqttUnsubAck) {
            handleUnsubAck(ctx, (MqttUnsubAck) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handleSubAck(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttSubAck subAck) {
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
        final int subscriptionCount = subscribe.getStatelessMessage().getSubscriptions().size();
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

        handleComplete(packetIdentifier);
    }

    private void handleUnsubAck(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttUnsubAck unsubAck) {
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
        final int topicFilterCount = unsubscribe.getStatelessMessage().getTopicFilters().size();
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

        handleComplete(packetIdentifier);
    }

    private void handleComplete(final int packetIdentifier) {
        final Object subscribeOrUnsubscribe = queued.poll();
        if (subscribeOrUnsubscribe == null) {
            packetIdentifiers.returnId(packetIdentifier);
        } else {
            queuedCounter.getAndDecrement();
            writeSubscribeOrUnsubscribe(subscribeOrUnsubscribe, packetIdentifier);
        }
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof ChannelCloseEvent) {
            handleChannelCloseEvent((ChannelCloseEvent) evt);
        }
        ctx.fireUserEventTriggered(evt);
    }

    private void handleChannelCloseEvent(@NotNull final ChannelCloseEvent channelCloseEvent) {
        ctx = null;
        clear(channelCloseEvent.getCause());
    }

    private void clear(@NotNull final Throwable cause) {
        pending.accept((packetIdentifier, subscribeOrUnsubscribe) -> {
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

    private static ReasonCodesState validateReasonCodes(
            final int count, @NotNull final ImmutableList<? extends Mqtt5ReasonCode> reasonCodes) {

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
