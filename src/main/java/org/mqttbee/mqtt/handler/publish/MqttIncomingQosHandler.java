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
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckBuilder;
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

    private MqttIncomingPublishService incomingPublishService;
    private final Provider<MqttIncomingPublishService> incomingPublishServiceLazy; // TODO temp
    private final IntMap<MqttPubRec> pubRecs;
    private final IntMap<MqttPubRel> pubRels;
    private final IntMap<Boolean> pubComps;

    private ChannelHandlerContext ctx;

    @Inject
    MqttIncomingQosHandler(
            final Provider<MqttIncomingPublishService> incomingPublishServiceLazy, final MqttClientData clientData) {

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        this.incomingPublishServiceLazy = incomingPublishServiceLazy;
        final int receiveMaximum = clientConnectionData.getReceiveMaximum();
        pubRecs = IntMap.range(1, receiveMaximum);
        pubRels = IntMap.range(1, receiveMaximum);
        pubComps = IntMap.range(1, receiveMaximum);
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
                handlePublishQos1(publish);
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
        getIncomingPublishService().onPublish(publish);
    }

    private void handlePublishQos1(@NotNull final MqttStatefulPublish publish) {
        getIncomingPublishService().onPublish(publish);
    }

    private void handlePublishQos2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttStatefulPublish publish) {

        final MqttPubRec pubRec = pubRecs.get(publish.getPacketIdentifier());
        if (pubRec == null) {
            handleNewPublishQos2(ctx, publish);
        } else {
            handleDupPublishQos2(ctx, publish, pubRec);
        }
    }

    private void handleNewPublishQos2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttStatefulPublish publish) {

        final MqttPubRecBuilder pubRecBuilder = new MqttPubRecBuilder(publish);

        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5IncomingQos2ControlProvider control = advanced.getIncomingQos2ControlProvider();
            if (control != null) {
                control.onPublish(publish.getStatelessMessage(), pubRecBuilder);
            }
        }

        final MqttPubRec pubRec = pubRecBuilder.build();
        pubRecs.put(pubRec.getPacketIdentifier(), pubRec);
        getIncomingPublishService().onPublish(publish);
        ctx.writeAndFlush(pubRec);
    }

    private void handleDupPublishQos2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttStatefulPublish publish,
            @NotNull final MqttPubRec pubRec) {

        if (!publish.isDup()) {
            // TODO
            return;
        }
        ctx.writeAndFlush(pubRec);
    }

    private void handlePubRel(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRel pubRel) {
        final int packetIdentifier = pubRel.getPacketIdentifier();
        pubRecs.remove(packetIdentifier);
        if (pubComps.remove(packetIdentifier) == null) {
            pubRels.put(packetIdentifier, pubRel);
        } else {
            writePubComp(ctx, pubRel);
            ctx.flush();
        }
    }

    @CallByThread("Netty EventLoop")
    void ack(@NotNull final MqttStatefulPublish publish) {
        switch (publish.getStatelessMessage().getQos()) {
            case AT_LEAST_ONCE:
                ackQos1(publish);
                break;
            case EXACTLY_ONCE:
                ackQos2(publish);
                break;
        }
    }

    private void ackQos1(@NotNull final MqttStatefulPublish publish) {
        final MqttPubAckBuilder pubAckBuilder = new MqttPubAckBuilder(publish);

        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5IncomingQos1ControlProvider control = advanced.getIncomingQos1ControlProvider();
            if (control != null) {
                control.onPublish(publish.getStatelessMessage(), pubAckBuilder);
            }
        }

        ctx.writeAndFlush(pubAckBuilder.build());
    }

    private void ackQos2(@NotNull final MqttStatefulPublish publish) {
        final int packetIdentifier = publish.getPacketIdentifier();
        final MqttPubRel pubRel = pubRels.remove(packetIdentifier);
        if (pubRel == null) {
            pubComps.put(packetIdentifier, Boolean.TRUE);
        } else {
            writePubComp(ctx, pubRel);
        }
    }

    private void writePubComp(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRel pubRel) {
        final MqttPubCompBuilder pubCompBuilder = new MqttPubCompBuilder(pubRel);

        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5IncomingQos2ControlProvider control = advanced.getIncomingQos2ControlProvider();
            if (control != null) {
                control.onPubRel(pubRel, pubCompBuilder);
            }
        }

        ctx.writeAndFlush(pubCompBuilder.build());
    }

}
