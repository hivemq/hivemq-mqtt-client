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
import java.util.LinkedList;
import javax.inject.Inject;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.publish.MqttIncomingPublishFlows;
import org.mqttbee.mqtt.handler.publish.MqttOutgoingQoSHandler;
import org.mqttbee.mqtt.handler.publish.MqttSubscriptionFlow;
import org.mqttbee.mqtt.handler.subscribe.MqttSubscribeWithFlow.MqttStatefulSubscribeWithFlow;
import org.mqttbee.mqtt.handler.subscribe.MqttUnsubscribeWithFlow.MqttStatefulUnsubscribeWithFlow;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;
import org.mqttbee.rx.SingleFlow;
import org.mqttbee.util.Ranges;
import org.mqttbee.util.collections.IntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Silvio Giebl */
@ChannelScope
public class MqttSubscriptionHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "subscription";
    public static final int MAX_SUB_PENDING = 10; // TODO configurable
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSubscriptionHandler.class);

    private enum ReasonCodesState {
        AT_LEAST_ONE_SUCCESSFUL,
        COUNT_NOT_MATCHING,
        ALL_ERRORS
    }

    private final MqttIncomingPublishFlows subscriptionFlows;
    private final Ranges packetIdentifiers;
    private final Ranges subscriptionIdentifiers;
    private final IntMap<MqttStatefulSubscribeWithFlow> subscribes;
    private final IntMap<MqttStatefulUnsubscribeWithFlow> unsubscribes;
    private final LinkedList<Object> queued;
    private int pending;

    private ChannelHandlerContext ctx; // TODO temp

    @Inject
    MqttSubscriptionHandler(
            final MqttIncomingPublishFlows subscriptionFlows, final MqttClientData clientData) {
        this.subscriptionFlows = subscriptionFlows;

        final MqttClientConnectionData clientConnectionData =
                clientData.getRawClientConnectionData();
        assert clientConnectionData != null;
        final MqttServerConnectionData serverConnectionData =
                clientData.getRawServerConnectionData();
        assert serverConnectionData != null;

        final int minPacketIdentifier =
                MqttOutgoingQoSHandler.getPubReceiveMaximum(
                                serverConnectionData.getReceiveMaximum())
                        + 1;
        final int maxPacketIdentifier = minPacketIdentifier + MAX_SUB_PENDING - 1;
        packetIdentifiers = new Ranges(minPacketIdentifier, maxPacketIdentifier);
        subscriptionIdentifiers =
                new Ranges(1, clientConnectionData.getSubscriptionIdentifierMaximum());
        subscribes = new IntMap<>(minPacketIdentifier, maxPacketIdentifier);
        unsubscribes = new IntMap<>(minPacketIdentifier, maxPacketIdentifier);
        queued = new LinkedList<>();
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        this.ctx = ctx; // TODO temp
    }

    public void subscribe(@NotNull final MqttSubscribeWithFlow subscribeWithFlow) {
        ctx.executor().execute(() -> handleSubscribe(ctx, subscribeWithFlow)); // TODO temp
    }

    private void handleSubscribe(
            @NotNull final ChannelHandlerContext ctx,
            @NotNull final MqttSubscribeWithFlow subscribeWithFlow) {

        if (pending == MAX_SUB_PENDING) {
            queued.offer(subscribeWithFlow);
            return;
        }

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier == -1) {
            // TODO must not happen
            return;
        }
        writeSubscribe(ctx, subscribeWithFlow, packetIdentifier);
    }

    private void writeSubscribe(
            @NotNull final ChannelHandlerContext ctx,
            @NotNull final MqttSubscribeWithFlow subscribeWithFlow,
            final int packetIdentifier) {

        final MqttStatefulSubscribeWithFlow statefulSubscribeWithFlow =
                subscribeWithFlow.createStateful(packetIdentifier, subscriptionIdentifiers.getId());
        subscribes.put(packetIdentifier, statefulSubscribeWithFlow);
        pending++;
        ctx.writeAndFlush(statefulSubscribeWithFlow.getSubscribe())
                .addListener(
                        future -> {
                            if (!future.isSuccess()) {
                                statefulSubscribeWithFlow.getSubAckFlow().onError(future.cause());
                                handleComplete(ctx, packetIdentifier);
                            }
                        });
    }

    public void unsubscribe(@NotNull final MqttUnsubscribeWithFlow unsubscribeWithFlow) {
        ctx.executor().execute(() -> handleUnsubscribe(ctx, unsubscribeWithFlow)); // TODO temp
    }

    private void handleUnsubscribe(
            @NotNull final ChannelHandlerContext ctx,
            @NotNull final MqttUnsubscribeWithFlow unsubscribeWithFlow) {

        if (pending == MAX_SUB_PENDING) {
            queued.offer(unsubscribeWithFlow);
            return;
        }

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier == -1) {
            // TODO must not happen
            return;
        }
        writeUnsubscribe(ctx, unsubscribeWithFlow, packetIdentifier);
    }

    private void writeUnsubscribe(
            @NotNull final ChannelHandlerContext ctx,
            @NotNull final MqttUnsubscribeWithFlow unsubscribeWithFlow,
            final int packetIdentifier) {

        final MqttStatefulUnsubscribeWithFlow statefulUnsubscribeWithFlow =
                unsubscribeWithFlow.createStateful(packetIdentifier);
        unsubscribes.put(packetIdentifier, statefulUnsubscribeWithFlow);
        pending++;
        ctx.writeAndFlush(statefulUnsubscribeWithFlow.getUnsubscribe())
                .addListener(
                        future -> {
                            if (!future.isSuccess()) {
                                statefulUnsubscribeWithFlow
                                        .getUnsubAckFlow()
                                        .onError(future.cause());
                                handleComplete(ctx, packetIdentifier);
                            }
                        });
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

    private void handleSubAck(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttSubAck subAck) {
        final int packetIdentifier = subAck.getPacketIdentifier();
        final MqttStatefulSubscribeWithFlow statefulSubscribeWithFlow =
                subscribes.remove(packetIdentifier);

        if (statefulSubscribeWithFlow == null) {
            MqttDisconnectUtil.disconnect(
                    ctx.channel(),
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Unknown packet identifier for SUBACK");
            return;
        }

        final MqttStatefulSubscribe subscribe = statefulSubscribeWithFlow.getSubscribe();
        final SingleFlow<Mqtt5SubAck> subAckFlow = statefulSubscribeWithFlow.getSubAckFlow();
        final int subscriptionCount = subscribe.getStatelessMessage().getSubscriptions().size();
        final ReasonCodesState reasonCodesState =
                validateReasonCodes(subscriptionCount, subAck.getReasonCodes());

        if (reasonCodesState == ReasonCodesState.AT_LEAST_ONE_SUCCESSFUL) {
            if (!subAckFlow.isCancelled()) {
                subAckFlow.onSuccess(subAck);
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
                    errorMessage =
                            "Count of Reason Codes in SUBACK does not match count of subscriptions in SUBSCRIBE";
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

    private void handleUnsubAck(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttUnsubAck unsubAck) {
        final int packetIdentifier = unsubAck.getPacketIdentifier();
        final MqttStatefulUnsubscribeWithFlow statefulUnsubscribeWithFlow =
                unsubscribes.remove(packetIdentifier);

        if (statefulUnsubscribeWithFlow == null) {
            MqttDisconnectUtil.disconnect(
                    ctx.channel(),
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Unknown packet identifier for UNSUBACK");
            return;
        }

        final MqttStatefulUnsubscribe unsubscribe = statefulUnsubscribeWithFlow.getUnsubscribe();
        final SingleFlow<Mqtt5UnsubAck> unsubAckFlow =
                statefulUnsubscribeWithFlow.getUnsubAckFlow();
        final int topicFilterCount = unsubscribe.getStatelessMessage().getTopicFilters().size();
        final ReasonCodesState reasonCodesState =
                validateReasonCodes(topicFilterCount, unsubAck.getReasonCodes());

        if (reasonCodesState == ReasonCodesState.AT_LEAST_ONE_SUCCESSFUL) {
            if (!unsubAckFlow.isCancelled()) {
                unsubAckFlow.onSuccess(unsubAck);
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

    private void handleComplete(
            @NotNull final ChannelHandlerContext ctx, final int packetIdentifier) {
        pending--;
        final Object subscribeOrUnsubscribe = queued.poll();
        if (subscribeOrUnsubscribe == null) {
            packetIdentifiers.returnId(packetIdentifier);
        } else {
            if (subscribeOrUnsubscribe instanceof MqttSubscribeWithFlow) {
                writeSubscribe(
                        ctx, (MqttSubscribeWithFlow) subscribeOrUnsubscribe, packetIdentifier);
            } else {
                writeUnsubscribe(
                        ctx, (MqttUnsubscribeWithFlow) subscribeOrUnsubscribe, packetIdentifier);
            }
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
