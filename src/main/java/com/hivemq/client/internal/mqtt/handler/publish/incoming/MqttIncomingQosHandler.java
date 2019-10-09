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

package com.hivemq.client.internal.mqtt.handler.publish.incoming;

import com.hivemq.client.internal.annotations.CallByThread;
import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.advanced.interceptor.MqttClientInterceptors;
import com.hivemq.client.internal.mqtt.handler.MqttSessionAwareHandler;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.ioc.ClientScope;
import com.hivemq.client.internal.mqtt.message.MqttMessage;
import com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client.internal.mqtt.message.publish.puback.MqttPubAck;
import com.hivemq.client.internal.mqtt.message.publish.puback.MqttPubAckBuilder;
import com.hivemq.client.internal.mqtt.message.publish.pubcomp.MqttPubComp;
import com.hivemq.client.internal.mqtt.message.publish.pubcomp.MqttPubCompBuilder;
import com.hivemq.client.internal.mqtt.message.publish.pubrec.MqttPubRec;
import com.hivemq.client.internal.mqtt.message.publish.pubrec.MqttPubRecBuilder;
import com.hivemq.client.internal.mqtt.message.publish.pubrel.MqttPubRel;
import com.hivemq.client.internal.netty.ContextFuture;
import com.hivemq.client.internal.netty.DefaultContextPromise;
import com.hivemq.client.internal.util.collections.IntIndex;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5IncomingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5IncomingQos2Interceptor;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttIncomingQosHandler extends MqttSessionAwareHandler
        implements ContextFuture.Listener<MqttMessage.WithId> {

    public static final @NotNull String NAME = "qos.incoming";
    private static final @NotNull IntIndex.Spec<MqttMessage.WithId> INDEX_SPEC =
            new IntIndex.Spec<>(MqttMessage.WithId::getPacketIdentifier);

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttIncomingPublishFlows incomingPublishFlows;
    private final @NotNull MqttIncomingPublishService incomingPublishService;

    private final @NotNull IntIndex<MqttMessage.WithId> messages = new IntIndex<>(INDEX_SPEC);
    // contains StatefulPublish with AT_LEAST_ONCE/EXACTLY_ONCE, MqttPubAck or MqttPubRec

    private int receiveMaximum;

    @Inject
    MqttIncomingQosHandler(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttIncomingPublishFlows incomingPublishFlows) {

        this.clientConfig = clientConfig;
        this.incomingPublishFlows = incomingPublishFlows;
        incomingPublishService = new MqttIncomingPublishService(this);
    }

    @Override
    public void onSessionStartOrResume(
            final @NotNull MqttClientConnectionConfig connectionConfig, final @NotNull EventLoop eventLoop) {

        super.onSessionStartOrResume(connectionConfig, eventLoop);
        receiveMaximum = connectionConfig.getReceiveMaximum();
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
        incomingPublishService.onPublishQos0(publish, receiveMaximum);
    }

    private void readPublishQos1(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {
        final MqttMessage.WithId prevMessage = messages.putIfAbsent(publish);
        if (prevMessage == null) { // new message
            readNewPublishQos1Or2(ctx, publish);
        } else if ((prevMessage instanceof MqttStatefulPublish) &&
                (((MqttStatefulPublish) prevMessage).stateless().getQos() == MqttQos.AT_LEAST_ONCE)) { // resent message
            checkDupFlagSet(ctx, publish);
        } else if (prevMessage instanceof MqttPubAck) { // resent message and already acknowledged
            if (checkDupFlagSet(ctx, publish)) {
                writePubAck(ctx, (MqttPubAck) prevMessage);
            }
        } else { // EXACTLY_ONCE or MqttPubRec
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "QoS 1 PUBLISH must not be received with the same packet identifier as a QoS 2 PUBLISH");
        }
    }

    private void readPublishQos2(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {
        final MqttMessage.WithId prevMessage = messages.putIfAbsent(publish);
        if (prevMessage == null) { // new message
            readNewPublishQos1Or2(ctx, publish);
        } else if ((prevMessage instanceof MqttStatefulPublish) &&
                (((MqttStatefulPublish) prevMessage).stateless().getQos() == MqttQos.EXACTLY_ONCE)) { // resent message
            checkDupFlagSet(ctx, publish);
        } else if (prevMessage instanceof MqttPubRec) { // resent message and already acknowledged
            if (checkDupFlagSet(ctx, publish)) {
                writePubRec(ctx, (MqttPubRec) prevMessage);
            }
        } else { // AT_LEAST_ONCE or MqttPubAck
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "QoS 2 PUBLISH must not be received with the same packet identifier as a QoS 1 PUBLISH");
        }
    }

    private void readNewPublishQos1Or2(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {

        if (!incomingPublishService.onPublishQos1Or2(publish, receiveMaximum)) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.RECEIVE_MAXIMUM_EXCEEDED,
                    "Received more QoS 1 and/or 2 PUBLISHes than allowed by Receive Maximum");
        }
    }

    private boolean checkDupFlagSet(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {

        if (!publish.isDup()) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "DUP flag must be set for a resent QoS " + publish.stateless().getQos().getCode() + " PUBLISH");
            return false;
        }
        return true;
    }

    @CallByThread("Netty EventLoop")
    void ack(final @NotNull MqttStatefulPublish publish) {
        switch (publish.stateless().getQos()) {
            case AT_LEAST_ONCE:
                final MqttPubAck pubAck = buildPubAck(new MqttPubAckBuilder(publish));
                messages.put(pubAck);
                if (ctx != null) {
                    writePubAck(ctx, pubAck);
                }
                break;
            case EXACTLY_ONCE:
                final MqttPubRec pubRec = buildPubRec(new MqttPubRecBuilder(publish));
                messages.put(pubRec);
                if (ctx != null) {
                    writePubRec(ctx, pubRec);
                }
                break;
        }
    }

    private void writePubAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubAck pubAck) {
        ctx.writeAndFlush(pubAck, new DefaultContextPromise<>(ctx.channel(), pubAck)).addListener(this);
    }

    private void writePubRec(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubRec pubRec) {
        if (pubRec.getReasonCode().isError()) {
            ctx.writeAndFlush(pubRec, new DefaultContextPromise<>(ctx.channel(), pubRec)).addListener(this);
        } else {
            ctx.writeAndFlush(pubRec, ctx.voidPromise());
        }
    }

    @Override
    public void operationComplete(final @NotNull ContextFuture<? extends MqttMessage.WithId> future) {
        if (future.isSuccess()) {
            messages.remove(future.getContext().getPacketIdentifier());
        } else {
            future.channel().pipeline().fireExceptionCaught(future.cause());
        }
    }

    private void readPubRel(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubRel pubRel) {
        final MqttMessage.WithId prevMessage = messages.remove(pubRel.getPacketIdentifier());
        if (prevMessage instanceof MqttPubRec) { // normal case
            writePubComp(ctx, buildPubComp(new MqttPubCompBuilder(pubRel)));
        } else if (prevMessage == null) { // may be resent
            writePubComp(
                    ctx, buildPubComp(new MqttPubCompBuilder(pubRel).reasonCode(
                            Mqtt5PubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND)));
        } else if ((prevMessage instanceof MqttStatefulPublish) &&
                (((MqttStatefulPublish) prevMessage).stateless().getQos() ==
                        MqttQos.EXACTLY_ONCE)) { // PubRec not sent yet
            messages.put(prevMessage); // revert
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "PUBREL must not be received with the same packet identifier as a QoS 2 PUBLISH when no PUBREC has been sent yet");
        } else { // MqttQos.AT_LEAST_ONCE or MqttPubAck
            messages.put(prevMessage); // revert
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "PUBREL must not be received with the same packet identifier as a QoS 1 PUBLISH");
        }
    }

    private void writePubComp(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubComp pubComp) {
        ctx.writeAndFlush(pubComp, ctx.voidPromise());
    }

    @Override
    public void onSessionEnd(final @NotNull Throwable cause) {
        super.onSessionEnd(cause);
        incomingPublishFlows.clear(cause);
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

    @NotNull MqttIncomingPublishFlows getIncomingPublishFlows() {
        return incomingPublishFlows;
    }

    @NotNull MqttIncomingPublishService getIncomingPublishService() {
        return incomingPublishService;
    }
}
