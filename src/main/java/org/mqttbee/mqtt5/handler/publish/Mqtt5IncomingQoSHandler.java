package org.mqttbee.mqtt5.handler.publish;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQoS1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQoS2ControlProvider;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckBuilder;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompBuilder;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecBuilder;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.util.collections.IntMap;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class Mqtt5IncomingQoSHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "qos.incoming";

    private MqttIncomingPublishService incomingPublishService;
    private final Provider<MqttIncomingPublishService> incomingPublishServiceLazy; // TODO temp
    private final IntMap<MqttPubRec> pubRecs;
    private final IntMap<MqttPubRel> pubRels;
    private final IntMap<Boolean> pubComps;

    private ChannelHandlerContext ctx;

    @Inject
    Mqtt5IncomingQoSHandler(
            final Provider<MqttIncomingPublishService> incomingPublishServiceLazy, final MqttClientData clientData) {

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        this.incomingPublishServiceLazy = incomingPublishServiceLazy;
        final int receiveMaximum = clientConnectionData.getReceiveMaximum();
        pubRecs = new IntMap<>(receiveMaximum);
        pubRels = new IntMap<>(receiveMaximum);
        pubComps = new IntMap<>(receiveMaximum);
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttPublishWrapper) {
            handlePublish(ctx, (MqttPublishWrapper) msg);
        } else if (msg instanceof MqttPubRel) {
            handlePubRel(ctx, (MqttPubRel) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handlePublish(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWrapper publish) {
        switch (publish.getWrapped().getQos()) {
            case AT_MOST_ONCE:
                handlePublishQoS0(publish);
                break;
            case AT_LEAST_ONCE:
                handlePublishQoS1(publish);
                break;
            case EXACTLY_ONCE:
                handlePublishQoS2(ctx, publish);
                break;
        }
    }

    private MqttIncomingPublishService getIncomingPublishService() { // TODO temp
        if (incomingPublishService == null) {
            incomingPublishService = incomingPublishServiceLazy.get();
        }
        return incomingPublishService;
    }

    private void handlePublishQoS0(@NotNull final MqttPublishWrapper publish) {
        getIncomingPublishService().onPublish(publish);
    }

    private void handlePublishQoS1(@NotNull final MqttPublishWrapper publish) {
        getIncomingPublishService().onPublish(publish);
    }

    private void handlePublishQoS2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWrapper publish) {

        final MqttPubRec pubRec = pubRecs.get(publish.getPacketIdentifier());
        if (pubRec == null) {
            handleNewPublishQoS2(ctx, publish);
        } else {
            handleDupPublishQoS2(ctx, publish, pubRec);
        }
    }

    private void handleNewPublishQoS2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWrapper publish) {

        final MqttPubRecBuilder pubRecBuilder = new MqttPubRecBuilder(publish);

        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5IncomingQoS2ControlProvider control = advanced.getIncomingQoS2ControlProvider();
            if (control != null) {
                control.onPublish(publish.getWrapped(), pubRecBuilder);
            }
        }

        final MqttPubRec pubRec = pubRecBuilder.build();
        pubRecs.put(pubRec.getPacketIdentifier(), pubRec);
        getIncomingPublishService().onPublish(publish);
        ctx.writeAndFlush(pubRec);
    }

    private void handleDupPublishQoS2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWrapper publish,
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
        }
    }

    void ack(@NotNull final MqttPublishWrapper publishWrapper) {
        ctx.executor().execute(() -> {
            switch (publishWrapper.getWrapped().getQos()) {
                case AT_LEAST_ONCE:
                    ackQoS1(publishWrapper);
                    break;
                case EXACTLY_ONCE:
                    ackQoS2(publishWrapper);
                    break;
            }
        });
    }

    private void ackQoS1(@NotNull final MqttPublishWrapper publish) {
        final MqttPubAckBuilder pubAckBuilder = new MqttPubAckBuilder(publish);

        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5IncomingQoS1ControlProvider control = advanced.getIncomingQoS1ControlProvider();
            if (control != null) {
                control.onPublish(publish.getWrapped(), pubAckBuilder);
            }
        }

        ctx.writeAndFlush(pubAckBuilder.build());
    }

    private void ackQoS2(@NotNull final MqttPublishWrapper publishWrapper) {
        final int packetIdentifier = publishWrapper.getPacketIdentifier();
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
            final Mqtt5IncomingQoS2ControlProvider control = advanced.getIncomingQoS2ControlProvider();
            if (control != null) {
                control.onPubRel(pubRel, pubCompBuilder);
            }
        }

        ctx.writeAndFlush(pubCompBuilder.build());
    }

}
