/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt.handler.publish.incoming;

import com.hivemq.client.internal.annotations.CallByThread;
import com.hivemq.client.internal.logging.InternalLogger;
import com.hivemq.client.internal.logging.InternalLoggerFactory;
import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.advanced.interceptor.MqttClientInterceptors;
import com.hivemq.client.internal.mqtt.handler.MqttSessionAwareHandler;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.ioc.ClientScope;
import com.hivemq.client.internal.mqtt.message.publish.*;
import com.hivemq.client.internal.util.collections.IntIndex;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5IncomingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5IncomingQos2Interceptor;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PubCompReasonCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttIncomingQosHandler extends MqttSessionAwareHandler {

    public static final @NotNull String NAME = "qos.incoming";
    private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(MqttIncomingQosHandler.class);
    private static final IntIndex.@NotNull Spec<Object> INDEX_SPEC = new IntIndex.Spec<>(value -> {
        if (value instanceof MqttStatefulPublishWithFlows) {
            return ((MqttStatefulPublishWithFlows) value).publish.getPacketIdentifier();
        } else {
            return ((MqttPubRec) value).getPacketIdentifier();
        }
    });

    private final @NotNull MqttClientConfig clientConfig;
    final @NotNull MqttIncomingPublishService incomingPublishService;

    // valid for session
    private final @NotNull IntIndex<Object> messages = new IntIndex<>(INDEX_SPEC);
    // contains MqttStatefulPublishWithFlows with AT_LEAST_ONCE/EXACTLY_ONCE or MqttPubRec

    // valid for connection
    private int receiveMaximum;
    private long connectionIndex;

    @Inject
    MqttIncomingQosHandler(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttIncomingPublishFlows incomingPublishFlows) {

        this.clientConfig = clientConfig;
        incomingPublishService = new MqttIncomingPublishService(this, incomingPublishFlows);
    }

    @Override
    public void onSessionStartOrResume(
            final @NotNull MqttClientConnectionConfig connectionConfig, final @NotNull EventLoop eventLoop) {

        receiveMaximum = connectionConfig.getReceiveMaximum();
        connectionIndex++;
        super.onSessionStartOrResume(connectionConfig, eventLoop);
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof MqttStatefulPublish) {
            readPublish(ctx, (MqttStatefulPublish) msg);
        } else if (msg instanceof MqttPubRel) {
            readPubRel(ctx, (MqttPubRel) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readPublish(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {
        switch (publish.stateless().getQos()) {
            case AT_MOST_ONCE:
                readPublishQos0(publish);
                break;
            case AT_LEAST_ONCE:
                readPublishQos1(ctx, publish);
                break;
            case EXACTLY_ONCE:
                readPublishQos2(ctx, publish);
                break;
        }
    }

    private void readPublishQos0(final @NotNull MqttStatefulPublish publish) {
        incomingPublishService.onPublishQos0(new MqttStatefulPublishWithFlows(publish), receiveMaximum);
    }

    private void readPublishQos1(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {
        final MqttStatefulPublishWithFlows publishWithFlows = new MqttStatefulPublishWithFlows(publish);
        publishWithFlows.connectionIndex = connectionIndex;
        final Object prevMessage = messages.putIfAbsent(publishWithFlows);
        if (prevMessage == null) { // new message
            if (!readNewPublishQos1Or2(ctx, publishWithFlows)) {
                messages.remove(publish.getPacketIdentifier());
            }
        } else if (prevMessage instanceof MqttStatefulPublishWithFlows) {
            final MqttStatefulPublishWithFlows prevPublishWithFlows = (MqttStatefulPublishWithFlows) prevMessage;
            if (prevPublishWithFlows.publish.stateless().getQos() == MqttQos.AT_LEAST_ONCE) {
                if (prevPublishWithFlows.connectionIndex == connectionIndex) {
                    if (clientConfig.getMqttVersion() == MqttVersion.MQTT_5_0) {
                        LOGGER.error("QoS 1 PUBLISH ({}) must not be resent ({}) during the same connection",
                                prevPublishWithFlows.publish, publish);
                        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                                "QoS 1 PUBLISH must not be resent during the same connection");
                    } else { // resent message during the same connection & MQTT 3
                        checkDupFlagSet(ctx, publish);
                    }
                } else { // resent message or new message because session state on server differs, can not distinguish
                    messages.put(publishWithFlows);
                    if (!readNewPublishQos1Or2(ctx, publishWithFlows)) {
                        messages.put(prevMessage);
                    }
                }
            } else { // EXACTLY_ONCE
                LOGGER.error("QoS 1 PUBLISH ({}) must not carry the same packet identifier as a QoS 2 PUBLISH ({})",
                        publish, prevPublishWithFlows.publish);
                MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                        "QoS 1 PUBLISH must not carry the same packet identifier as a QoS 2 PUBLISH");
            }
        } else { // MqttPubRec
            LOGGER.error("QoS 1 PUBLISH ({}) must not carry the same packet identifier as a QoS 2 PUBLISH ({})",
                    publish, prevMessage);
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "QoS 1 PUBLISH must not carry the same packet identifier as a QoS 2 PUBLISH");
        }
    }

    private void readPublishQos2(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {
        final MqttStatefulPublishWithFlows publishWithFlows = new MqttStatefulPublishWithFlows(publish);
        publishWithFlows.connectionIndex = connectionIndex;
        final Object prevMessage = messages.putIfAbsent(publishWithFlows);
        if (prevMessage == null) { // new message
            if (!readNewPublishQos1Or2(ctx, publishWithFlows)) {
                messages.remove(publish.getPacketIdentifier());
            }
        } else if (prevMessage instanceof MqttStatefulPublishWithFlows) {
            final MqttStatefulPublishWithFlows prevPublishWithFlows = (MqttStatefulPublishWithFlows) prevMessage;
            if (prevPublishWithFlows.publish.stateless().getQos() == MqttQos.EXACTLY_ONCE) {
                if (prevPublishWithFlows.connectionIndex == connectionIndex) {
                    if (clientConfig.getMqttVersion() == MqttVersion.MQTT_5_0) {
                        LOGGER.error("QoS 2 PUBLISH ({}) must not be resent ({}) during the same connection",
                                prevPublishWithFlows.publish, publish);
                        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                                "QoS 2 PUBLISH must not be resent during the same connection");
                    } else { // resent message during the same connection & MQTT 3
                        checkDupFlagSet(ctx, publish);
                    }
                } else { // resent message
                    prevPublishWithFlows.connectionIndex = connectionIndex;
                    checkDupFlagSet(ctx, publish);
                }
            } else { // AT_LEAST_ONCE
                if (prevPublishWithFlows.connectionIndex == connectionIndex) {
                    LOGGER.error("QoS 2 PUBLISH ({}) must not carry the same packet identifier as a QoS 1 PUBLISH ({})",
                            publish, prevPublishWithFlows.publish);
                    MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                            "QoS 2 PUBLISH must not carry the same packet identifier as a QoS 1 PUBLISH");
                } else { // new message because session state on server differs
                    messages.put(publishWithFlows);
                    if (!readNewPublishQos1Or2(ctx, publishWithFlows)) {
                        messages.put(prevMessage);
                    }
                }
            }
        } else { // MqttPubRec, resent message and already acknowledged
            if (checkDupFlagSet(ctx, publish)) {
                writePubRec(ctx, (MqttPubRec) prevMessage);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean readNewPublishQos1Or2(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublishWithFlows publishWithFlows) {

        if (incomingPublishService.onPublishQos1Or2(publishWithFlows, receiveMaximum)) {
            return true;
        }
        LOGGER.error("Received more QoS 1 and/or 2 PUBLISH messages ({}) than allowed by receive maximum ({})",
                publishWithFlows.publish, receiveMaximum);
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.RECEIVE_MAXIMUM_EXCEEDED,
                "Received more QoS 1 and/or 2 PUBLISH messages than allowed by receive maximum");
        return false;
    }

    private boolean checkDupFlagSet(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {

        if (publish.isDup()) {
            return true;
        }
        LOGGER.error("DUP flag must be set for a resent PUBLISH ({})", publish);
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                "DUP flag must be set for a resent QoS " + publish.stateless().getQos().getCode() + " PUBLISH");
        return false;
    }

    @CallByThread("Netty EventLoop")
    void ack(final @NotNull MqttStatefulPublishWithFlows publishWithFlows) {
        switch (publishWithFlows.publish.stateless().getQos()) {
            case AT_LEAST_ONCE: {
                final MqttPubAck pubAck = buildPubAck(new MqttPubAckBuilder(publishWithFlows.publish));
                final Object prevMessage = messages.remove(pubAck.getPacketIdentifier());
                if (ack(prevMessage, publishWithFlows) && (ctx != null)) {
                    writePubAck(ctx, pubAck);
                }
                break;
            }
            case EXACTLY_ONCE: {
                final MqttPubRec pubRec = buildPubRec(new MqttPubRecBuilder(publishWithFlows.publish));
                final Object prevMessage = !pubRec.getReasonCode().isError() ? messages.put(pubRec) :
                        messages.remove(pubRec.getPacketIdentifier());
                if (ack(prevMessage, publishWithFlows) && (ctx != null)) {
                    writePubRec(ctx, pubRec);
                }
                break;
            }
        }
    }

    private boolean ack(
            final @Nullable Object prevMessage, final @NotNull MqttStatefulPublishWithFlows publishWithFlows) {

        if (prevMessage != publishWithFlows) {
            if (prevMessage == null) {
                // session has expired in the meantime
                messages.remove(publishWithFlows.publish.getPacketIdentifier());
            } else {
                // message has been overwritten by a new message because session state on server differs
                messages.put(prevMessage);
            }
            return false;
        }
        return publishWithFlows.connectionIndex == connectionIndex;
    }

    private void writePubAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubAck pubAck) {
        ctx.writeAndFlush(pubAck, ctx.voidPromise());
    }

    private void writePubRec(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubRec pubRec) {
        ctx.writeAndFlush(pubRec, ctx.voidPromise());
    }

    private void readPubRel(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubRel pubRel) {
        final Object prevMessage = messages.remove(pubRel.getPacketIdentifier());
        if (prevMessage instanceof MqttPubRec) { // normal case
            writePubComp(ctx, buildPubComp(new MqttPubCompBuilder(pubRel)));
        } else if (prevMessage == null) { // may be resent
            writePubComp(
                    ctx, buildPubComp(new MqttPubCompBuilder(pubRel).reasonCode(
                            Mqtt5PubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND)));
        } else { // MqttStatefulPublishWithFlows
            final MqttStatefulPublishWithFlows publishWithFlows = (MqttStatefulPublishWithFlows) prevMessage;
            messages.put(prevMessage); // revert
            if (publishWithFlows.publish.stateless().getQos() == MqttQos.EXACTLY_ONCE) { // PubRec not sent yet
                LOGGER.error(
                        "PUBREL ({}) must not carry the same packet identifier as an unacknowledged QoS 2 PUBLISH ({})",
                        pubRel, publishWithFlows.publish);
                MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                        "PUBREL must not carry the same packet identifier as an unacknowledged QoS 2 PUBLISH");
            } else { // AT_LEAST_ONCE
                LOGGER.error("PUBREL ({}) must not carry the same packet identifier as a QoS 1 PUBLISH ({})", pubRel,
                        publishWithFlows.publish);
                MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                        "PUBREL must not carry the same packet identifier as a QoS 1 PUBLISH");
            }
        }
    }

    private void writePubComp(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubComp pubComp) {
        ctx.writeAndFlush(pubComp, ctx.voidPromise());
    }

    @Override
    public void onSessionEnd(final @NotNull Throwable cause) {
        super.onSessionEnd(cause);
        messages.clear();
    }

    private @NotNull MqttPubAck buildPubAck(final @NotNull MqttPubAckBuilder pubAckBuilder) {
        final MqttClientInterceptors interceptors = clientConfig.getAdvancedConfig().getInterceptors();
        if (interceptors != null) {
            final Mqtt5IncomingQos1Interceptor interceptor = interceptors.getIncomingQos1Interceptor();
            if (interceptor != null) {
                interceptor.onPublish(clientConfig, pubAckBuilder.getPublish().stateless(), pubAckBuilder);
            }
        }
        return pubAckBuilder.build();
    }

    private @NotNull MqttPubRec buildPubRec(final @NotNull MqttPubRecBuilder pubRecBuilder) {
        final MqttClientInterceptors interceptors = clientConfig.getAdvancedConfig().getInterceptors();
        if (interceptors != null) {
            final Mqtt5IncomingQos2Interceptor interceptor = interceptors.getIncomingQos2Interceptor();
            if (interceptor != null) {
                interceptor.onPublish(clientConfig, pubRecBuilder.getPublish().stateless(), pubRecBuilder);
            }
        }
        return pubRecBuilder.build();
    }

    private @NotNull MqttPubComp buildPubComp(final @NotNull MqttPubCompBuilder pubCompBuilder) {
        final MqttClientInterceptors interceptors = clientConfig.getAdvancedConfig().getInterceptors();
        if (interceptors != null) {
            final Mqtt5IncomingQos2Interceptor interceptor = interceptors.getIncomingQos2Interceptor();
            if (interceptor != null) {
                interceptor.onPubRel(clientConfig, pubCompBuilder.getPubRel(), pubCompBuilder);
            }
        }
        return pubCompBuilder.build();
    }
}
