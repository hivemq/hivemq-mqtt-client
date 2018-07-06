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

package org.mqttbee.mqtt.handler.publish;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQos2ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckBuilder;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompBuilder;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecBuilder;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.util.collections.IntMap;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttIncomingQosHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "qos.incoming";

    private final MqttClientData clientData;
    private MqttIncomingPublishService incomingPublishService;
    private final Provider<MqttIncomingPublishService> incomingPublishServiceLazy; // TODO temp
    private final IntMap<Object> messages; // contains PubAck.class, PubRec.class, PubComp.class or a PubRel object

    private ChannelHandlerContext ctx;

    @Inject
    MqttIncomingQosHandler(
            final Provider<MqttIncomingPublishService> incomingPublishServiceLazy, final MqttClientData clientData) {

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        this.clientData = clientData;
        this.incomingPublishServiceLazy = incomingPublishServiceLazy;
        messages = IntMap.range(1, clientConnectionData.getReceiveMaximum());
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttStatefulPublish) {
            handlePublish(ctx, (MqttStatefulPublish) msg);
        } else if (msg instanceof MqttPubRel) {
            handlePubRel(ctx, (MqttPubRel) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handlePublish(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttStatefulPublish publish) {
        switch (publish.getStatelessMessage().getQos()) {
            case AT_MOST_ONCE:
                handlePublishQos0(publish);
                break;
            case AT_LEAST_ONCE:
                handlePublishQos1(ctx, publish);
                break;
            case EXACTLY_ONCE:
                handlePublishQos2(ctx, publish);
                break;
        }
    }

    private MqttIncomingPublishService getIncomingPublishService() { // TODO temp
        if (incomingPublishService == null) {
            incomingPublishService = incomingPublishServiceLazy.get();
        }
        return incomingPublishService;
    }

    private void handlePublishQos0(@NotNull final MqttStatefulPublish publish) {
        getIncomingPublishService().onPublish(publish); // TODO own for Qos 0
    }

    private void handlePublishQos1(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttStatefulPublish publish) {

        final Object previousMessage = messages.put(publish.getPacketIdentifier(), MqttPubAck.class);
        if (previousMessage == null) { // new message
            if (!getIncomingPublishService().onPublish(publish)) {
                disconnectReceiveMaximumExceeded(ctx);
            }
        } else if (previousMessage == MqttPubAck.class) { // resent message
            if (!publish.isDup()) {
                disconnectDupFlagNotSet(ctx);
            }
        } else if ((previousMessage == MqttPubRec.class) || (previousMessage == MqttPubComp.class)) { //packet id in use
            //ackQos1(ctx, new MqttPubAckBuilder(publish).reasonCode(Mqtt5PubAckReasonCode.PACKET_IDENTIFIER_IN_USE));
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Packet Identifier in use: QoS 1 Publish must not be received with the same Id as a QoS 2 Publish");
        } else { // PubRel: packet id in use
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Packet Identifier in use: QoS 1 Publish must not be received with the same Id as a PubRel");
        }
    }

    private void handlePublishQos2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttStatefulPublish publish) {

        final Object previousMessage = messages.put(publish.getPacketIdentifier(), MqttPubRec.class);
        if (previousMessage == null) { // new message
            if (getIncomingPublishService().onPublish(publish)) {
                ctx.writeAndFlush(buildPubRec(new MqttPubRecBuilder(publish)));
            } else {
                disconnectReceiveMaximumExceeded(ctx);
            }
        } else if ((previousMessage == MqttPubRec.class) || (previousMessage == MqttPubComp.class)) { // resent message
            if (!publish.isDup()) {
                disconnectDupFlagNotSet(ctx);
            }
        } else if (previousMessage == MqttPubAck.class) { // packet id in use
            //ctx.writeAndFlush(buildPubRec(
            //        new MqttPubRecBuilder(publish).reasonCode(Mqtt5PubRecReasonCode.PACKET_IDENTIFIER_IN_USE)));
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Packet Identifier in use: QoS 2 Publish must not be received with the same Id as a QoS 1 Publish");
        } else { // PubRel: packet id in use
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Packet Identifier in use: QoS 2 Publish must not be resent after a PubRel is sent");
        }
    }

    private static void disconnectReceiveMaximumExceeded(@NotNull final ChannelHandlerContext ctx) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.RECEIVE_MAXIMUM_EXCEEDED,
                "Received more QoS 1 and 2 Publishes than allowed by Receive Maximum");
    }

    private static void disconnectDupFlagNotSet(@NotNull final ChannelHandlerContext ctx) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                "Duplicate flag must be set for resent message");
    }

    private void handlePubRel(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRel pubRel) {
        final Object previousMessage = messages.put(pubRel.getPacketIdentifier(), pubRel);
        if (previousMessage == MqttPubComp.class) { // already emitted
            ackQos2(ctx, new MqttPubCompBuilder(pubRel));
        } else if (previousMessage == null) { // may be resent
            ackQos2(ctx, new MqttPubCompBuilder(pubRel).reasonCode(Mqtt5PubCompReasonCode.PACKET_IDENTIFIER_NOT_FOUND));
        } else if (previousMessage == MqttPubAck.class) { // packet id in use
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "PubRel must not be received with the same Packet Identifier as a QoS 1 Publish");
        } // PubRec: normal case, PubRel: resent
    }

    @CallByThread("Netty EventLoop")
    void ack(@NotNull final MqttStatefulPublish publish) {
        switch (publish.getStatelessMessage().getQos()) {
            case AT_LEAST_ONCE:
                ackQos1(ctx, new MqttPubAckBuilder(publish));
                break;
            case EXACTLY_ONCE:
                final Object previousMessage = messages.put(publish.getPacketIdentifier(), MqttPubComp.class);
                if (previousMessage instanceof MqttPubRel) { // PubRel already received
                    ackQos2(ctx, new MqttPubCompBuilder((MqttPubRel) previousMessage));
                }
                break;
        }
    }

    private void ackQos1(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubAckBuilder pubAckBuilder) {
        messages.remove(pubAckBuilder.getPublish().getPacketIdentifier());
        ctx.writeAndFlush(buildPubAck(pubAckBuilder));
    }

    private void ackQos2(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubCompBuilder pubCompBuilder) {
        messages.remove(pubCompBuilder.getPubRel().getPacketIdentifier());
        ctx.writeAndFlush(buildPubComp(pubCompBuilder));
    }

    @NotNull
    private MqttPubAck buildPubAck(@NotNull final MqttPubAckBuilder pubAckBuilder) {
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5IncomingQos1ControlProvider control = advanced.getIncomingQos1ControlProvider();
            if (control != null) {
                control.onPublish(clientData, pubAckBuilder.getPublish().getStatelessMessage(), pubAckBuilder);
            }
        }
        return pubAckBuilder.build();
    }

    @NotNull
    private MqttPubRec buildPubRec(@NotNull final MqttPubRecBuilder pubRecBuilder) {
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5IncomingQos2ControlProvider control = advanced.getIncomingQos2ControlProvider();
            if (control != null) {
                control.onPublish(clientData, pubRecBuilder.getPublish().getStatelessMessage(), pubRecBuilder);
            }
        }
        return pubRecBuilder.build();
    }

    @NotNull
    private MqttPubComp buildPubComp(@NotNull final MqttPubCompBuilder pubCompBuilder) {
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5IncomingQos2ControlProvider control = advanced.getIncomingQos2ControlProvider();
            if (control != null) {
                control.onPubRel(clientData, pubCompBuilder.getPubRel(), pubCompBuilder);
            }
        }
        return pubCompBuilder.build();
    }

}
