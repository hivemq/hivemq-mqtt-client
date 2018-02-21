package org.mqttbee.mqtt5.handler.publish;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt.message.publish.MqttPublishImpl;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAckImpl;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompImpl;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRecImpl;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.mqtt5.persistence.OutgoingQoSFlowPersistence;
import org.mqttbee.util.Ranges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class Mqtt5OutgoingQoSHandler extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mqtt5OutgoingQoSHandler.class);

    private final Ranges packetIdentifiers;
    private final OutgoingQoSFlowPersistence persistence;

    @Inject
    Mqtt5OutgoingQoSHandler(
            final Ranges packetIdentifiers, @NotNull final OutgoingQoSFlowPersistence persistence) {

        this.packetIdentifiers = packetIdentifiers;
        this.persistence = persistence;
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof MqttPublishImpl) {
            handlePublish(ctx, (MqttPublishImpl) msg, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private void handlePublish(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishImpl publish,
            @NotNull final ChannelPromise promise) {

        if (publish.getQos() == Mqtt5QoS.AT_MOST_ONCE) {
            handlePublishQoS0(ctx, publish, promise);
        } else {
            handlePublishQoS1Or2(ctx, publish, promise);
        }
    }

    private void handlePublishQoS0(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishImpl publish,
            @NotNull final ChannelPromise promise) {

        final int packetIdentifier = MqttPublishWrapper.NO_PACKET_IDENTIFIER_QOS_0;
        final MqttPublishWrapper publishWrapper =
                publish.wrap(packetIdentifier, false, MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS, false,
                        MqttPublishWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS); // TODO topic alias

        ctx.write(publishWrapper, promise);
    }

    private void handlePublishQoS1Or2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishImpl publish,
            @NotNull final ChannelPromise promise) {

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier < 0) {
            // TODO
        }

        final MqttPublishWrapper publishWrapper =
                publish.wrap(packetIdentifier, false, MqttPublishWrapper.DEFAULT_NO_TOPIC_ALIAS, false,
                        MqttPublishWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS); // TODO topic alias

        persistence.persist(publishWrapper).whenCompleteAsync((aVoid, throwable) -> {
            if (throwable == null) {
                ctx.writeAndFlush(publishWrapper, promise);
            } else {
                LOGGER.error("Unexpected exception while persisting PUBLISH in outgoing QoSFlowPersistence", throwable);
                promise.setFailure(throwable);
            }
        }, ctx.executor());
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttPubAckImpl) {
            handlePubAck(ctx, (MqttPubAckImpl) msg);
        } else if (msg instanceof MqttPubRecImpl) {
            handlePubRec(ctx, (MqttPubRecImpl) msg);
        } else if (msg instanceof MqttPubCompImpl) {
            handlePubComp(ctx, (MqttPubCompImpl) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handlePubAck(final ChannelHandlerContext ctx, final MqttPubAckImpl pubAck) {
        persistence.remove(pubAck.getPacketIdentifier());
    }

    private void handlePubRec(final ChannelHandlerContext ctx, final MqttPubRecImpl pubRec) {

    }

    private void handlePubComp(final ChannelHandlerContext ctx, final MqttPubCompImpl pubComp) {

    }

}
