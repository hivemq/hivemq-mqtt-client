package org.mqttbee.mqtt5.handler.publish;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQoS1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQoS2ControlProvider;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckBuilder;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompBuilder;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecBuilder;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.mqtt5.persistence.IncomingQoSFlowPersistence;

import javax.inject.Inject;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class Mqtt5IncomingQoSHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "qos.incoming";

    private final IncomingQoSFlowPersistence persistence;

    @Inject
    Mqtt5IncomingQoSHandler(final IncomingQoSFlowPersistence persistence) {
        this.persistence = persistence;
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
                handlePublishQoS0(ctx, publish);
                break;
            case AT_LEAST_ONCE:
                handlePublishQoS1(ctx, publish);
                break;
            case EXACTLY_ONCE:
                handlePublishQoS2(ctx, publish);
                break;
        }
    }

    private void handlePublishQoS0(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWrapper publish) {

        ctx.fireChannelRead(publish.getWrapped());
    }

    private void handlePublishQoS1(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWrapper publish) {

        ctx.fireChannelRead(publish.getWrapped());

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

    private void handlePublishQoS2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWrapper publish) {

        persistence.get(publish.getPacketIdentifier()).whenCompleteAsync((pubRec, throwable) -> {
            if (pubRec == null) {
                handleNewPublishQoS2(ctx, publish);
            } else {
                handleDupPublishQoS2(ctx, publish, pubRec);
            }
        }, ctx.executor());
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
        persistence.store(pubRec).whenCompleteAsync((aVoid, throwable) -> {
            ctx.fireChannelRead(publish.getWrapped());
            ctx.writeAndFlush(pubRec);
        }, ctx.executor());
    }

    private void handleDupPublishQoS2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWrapper publish,
            @NotNull final MqttPubRec pubRec) {

        // TODO validate dup flag
        ctx.writeAndFlush(pubRec);
    }

    private void handlePubRel(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRel pubRel) {
        persistence.discard(pubRel.getPacketIdentifier());

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
