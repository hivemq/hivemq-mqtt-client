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

package org.mqttbee.mqtt.handler.publish.incoming;

import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQos2ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.handler.disconnect.ChannelCloseEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.ioc.ClientScope;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckBuilder;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompBuilder;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecBuilder;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.util.UnsignedDataTypes;
import org.mqttbee.util.collections.ChunkedIntArrayQueue;
import org.mqttbee.util.collections.IntMap;

import javax.inject.Inject;

import static org.mqttbee.api.mqtt.datatypes.MqttQos.*;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttIncomingQosHandler extends ChannelInboundHandlerAdapter implements ChannelFutureListener {

    public static final @NotNull String NAME = "qos.incoming";

    private final @NotNull MqttClientData clientData;
    private final @NotNull MqttIncomingPublishFlows incomingPublishFlows;

    private final @NotNull MqttIncomingPublishService incomingPublishService;
    private final @NotNull IntMap<Object> messages = IntMap.range(1, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE);
    // contains AT_LEAST_ONCE, EXACTLY_ONCE, MqttPubAck or MqttPubRec
    private final @NotNull ChunkedIntArrayQueue pubAckQueue = new ChunkedIntArrayQueue(16);

    private @Nullable ChannelHandlerContext ctx;
    private int receiveMaximum;

    @Inject
    MqttIncomingQosHandler(
            final @NotNull MqttClientData clientData, final @NotNull MqttIncomingPublishFlows incomingPublishFlows) {

        this.clientData = clientData;
        this.incomingPublishFlows = incomingPublishFlows;
        incomingPublishService = new MqttIncomingPublishService(this);
    }

    @Override
    public void handlerAdded(final @NotNull ChannelHandlerContext ctx) {
        this.ctx = ctx;

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        receiveMaximum = clientConnectionData.getReceiveMaximum();
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
        switch (publish.getStatelessMessage().getQos()) {
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
        incomingPublishService.onPublish(publish, receiveMaximum); // TODO own queue for QoS 0
    }

    private void readPublishQos1(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {
        final Object previousMessage = messages.put(publish.getPacketIdentifier(), AT_LEAST_ONCE);
        if (previousMessage == null) { // new message
            readNewPublishQos1Or2(ctx, publish);
        } else if (previousMessage == AT_LEAST_ONCE) { // resent message
            checkDupFlagSet(ctx, publish);
        } else if (previousMessage instanceof MqttPubAck) { // resent message and already acknowledged
            checkDupFlagSet(ctx, publish);
            writePubAck(ctx, (MqttPubAck) previousMessage);
        } else { // MqttQos.EXACTLY_ONCE or MqttPubRec
            messages.put(publish.getPacketIdentifier(), previousMessage);
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "QoS 1 PUBLISH must not be received with the same packet identifier as a QoS 2 PUBLISH");
        }
    }

    private void readPublishQos2(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {
        final Object previousMessage = messages.put(publish.getPacketIdentifier(), EXACTLY_ONCE);
        if (previousMessage == null) { // new message
            readNewPublishQos1Or2(ctx, publish);
        } else if (previousMessage == EXACTLY_ONCE) { // resent message
            checkDupFlagSet(ctx, publish);
        } else if (previousMessage instanceof MqttPubRec) { // resent message and already acknowledged
            checkDupFlagSet(ctx, publish);
            writePubRec(ctx, (MqttPubRec) previousMessage);
        } else { // MqttQos.AT_LEAST_ONCE or MqttPubAck
            messages.put(publish.getPacketIdentifier(), previousMessage);
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "QoS 2 PUBLISH must not be received with the same packet identifier as a QoS 1 PUBLISH");
        }
    }

    private void readNewPublishQos1Or2(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {

        if (!incomingPublishService.onPublish(publish, receiveMaximum)) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.RECEIVE_MAXIMUM_EXCEEDED,
                    "Received more QoS 1 and/or 2 PUBLISHes than allowed by Receive Maximum");
        }
    }

    private static void checkDupFlagSet(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish) {

        if (!publish.isDup()) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "DUP flag must be set for a resent QoS " + publish.getQos().getCode() + " PUBLISH");
        }
    }

    @CallByThread("Netty EventLoop")
    void ack(final @NotNull MqttStatefulPublish publish) {
        if (publish.getQos() == AT_MOST_ONCE) { // TODO remove if own queue for QoS 0
            return;
        }
        if (publish.getQos() == AT_LEAST_ONCE) {
            final MqttPubAck pubAck = buildPubAck(new MqttPubAckBuilder(publish));
            messages.put(publish.getPacketIdentifier(), pubAck);
            if (ctx != null) {
                writePubAck(ctx, pubAck);
            }
        } else { // EXACTLY_ONCE
            final MqttPubRec pubRec = buildPubRec(new MqttPubRecBuilder(publish));
            messages.put(publish.getPacketIdentifier(), pubRec);
            if (ctx != null) {
                writePubRec(ctx, pubRec);
            }
        }
    }

    private void writePubAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubAck pubAck) {
        pubAckQueue.offer(pubAck.getPacketIdentifier());
        ctx.writeAndFlush(pubAck).addListener(this);
    }

    @Override
    public void operationComplete(final @NotNull ChannelFuture future) {
        if (future.isSuccess()) {
            messages.remove(pubAckQueue.poll(-1));
        }
    }

    private void writePubRec(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubRec pubRec) {
        ctx.writeAndFlush(pubRec, ctx.voidPromise());
    }

    private void readPubRel(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubRel pubRel) {
        final Object previousMessage = messages.remove(pubRel.getPacketIdentifier());
        if (previousMessage instanceof MqttPubRec) { // normal case
            writePubComp(ctx, buildPubComp(new MqttPubCompBuilder(pubRel)));
        } else if (previousMessage == null) { // may be resent
            writePubComp(
                    ctx, buildPubComp(new MqttPubCompBuilder(pubRel).reasonCode(
                            Mqtt5PubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND)));
        } else if (previousMessage == EXACTLY_ONCE) { // PubRec not sent yet
            messages.put(pubRel.getPacketIdentifier(), previousMessage);
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "PUBREL must not be received with the same packet identifier as a QoS 2 PUBLISH when no PUBREC has been sent yet");
        } else { // MqttQos.AT_LEAST_ONCE or MqttPubAck
            messages.put(pubRel.getPacketIdentifier(), previousMessage);
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "PUBREL must not be received with the same packet identifier as a QoS 1 PUBLISH");
        }
    }

    private void writePubComp(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubComp pubComp) {
        ctx.writeAndFlush(pubComp, ctx.voidPromise());
    }

    @Override
    public void userEventTriggered(final @NotNull ChannelHandlerContext ctx, final @NotNull Object evt) {
        if (evt instanceof ChannelCloseEvent) {
            handleChannelCloseEvent();
        }
        ctx.fireUserEventTriggered(evt);
    }

    private void handleChannelCloseEvent() {
        ctx = null;
        pubAckQueue.clear();
    }

    private @NotNull MqttPubAck buildPubAck(final @NotNull MqttPubAckBuilder pubAckBuilder) {
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5IncomingQos1ControlProvider control = advanced.getIncomingQos1ControlProvider();
            if (control != null) {
                control.onPublish(clientData, pubAckBuilder.getPublish().getStatelessMessage(), pubAckBuilder);
            }
        }
        return pubAckBuilder.build();
    }

    private @NotNull MqttPubRec buildPubRec(final @NotNull MqttPubRecBuilder pubRecBuilder) {
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5IncomingQos2ControlProvider control = advanced.getIncomingQos2ControlProvider();
            if (control != null) {
                control.onPublish(clientData, pubRecBuilder.getPublish().getStatelessMessage(), pubRecBuilder);
            }
        }
        return pubRecBuilder.build();
    }

    private @NotNull MqttPubComp buildPubComp(final @NotNull MqttPubCompBuilder pubCompBuilder) {
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5IncomingQos2ControlProvider control = advanced.getIncomingQos2ControlProvider();
            if (control != null) {
                control.onPubRel(clientData, pubCompBuilder.getPubRel(), pubCompBuilder);
            }
        }
        return pubCompBuilder.build();
    }

    @NotNull EventLoop getEventLoop() {
        return clientData.getEventLoop();
    }

    @NotNull MqttIncomingPublishFlows getIncomingPublishFlows() {
        return incomingPublishFlows;
    }

    @NotNull MqttIncomingPublishService getIncomingPublishService() {
        return incomingPublishService;
    }

    @Override
    public boolean isSharable() {
        return clientData.getEventLoop().inEventLoop() && (ctx == null);
    }
}
