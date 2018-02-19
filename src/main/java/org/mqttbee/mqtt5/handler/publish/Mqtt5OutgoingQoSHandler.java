package org.mqttbee.mqtt5.handler.publish;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5PublishWrapper;
import org.mqttbee.mqtt5.message.publish.puback.Mqtt5PubAckImpl;
import org.mqttbee.mqtt5.message.publish.pubcomp.Mqtt5PubCompImpl;
import org.mqttbee.mqtt5.message.publish.pubrec.Mqtt5PubRecImpl;
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
        if (msg instanceof Mqtt5PublishImpl) {
            handlePublish(ctx, (Mqtt5PublishImpl) msg, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private void handlePublish(
            @NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5PublishImpl publish, @NotNull final ChannelPromise promise) {

        if (publish.getQos() == Mqtt5QoS.AT_MOST_ONCE) {
            handlePublishQoS0(ctx, publish, promise);
        } else {
            handlePublishQoS1Or2(ctx, publish, promise);
        }
    }

    private void handlePublishQoS0(
            @NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5PublishImpl publish,
            @NotNull final ChannelPromise promise) {

        final int packetIdentifier = Mqtt5PublishWrapper.NO_PACKET_IDENTIFIER_QOS_0;
        final Mqtt5PublishWrapper publishWrapper =
                publish.wrap(packetIdentifier, false, Mqtt5PublishWrapper.DEFAULT_NO_TOPIC_ALIAS, false,
                        Mqtt5PublishWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS); // TODO topic alias

        ctx.write(publishWrapper, promise);
    }

    private void handlePublishQoS1Or2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final Mqtt5PublishImpl publish,
            @NotNull final ChannelPromise promise) {

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier < 0) {
            // TODO
        }

        final Mqtt5PublishWrapper publishWrapper =
                publish.wrap(packetIdentifier, false, Mqtt5PublishWrapper.DEFAULT_NO_TOPIC_ALIAS, false,
                        Mqtt5PublishWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS); // TODO topic alias

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
        if (msg instanceof Mqtt5PubAckImpl) {
            handlePubAck(ctx, (Mqtt5PubAckImpl) msg);
        } else if (msg instanceof Mqtt5PubRecImpl) {
            handlePubRec(ctx, (Mqtt5PubRecImpl) msg);
        } else if (msg instanceof Mqtt5PubCompImpl) {
            handlePubComp(ctx, (Mqtt5PubCompImpl) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handlePubAck(final ChannelHandlerContext ctx, final Mqtt5PubAckImpl pubAck) {
        persistence.remove(pubAck.getPacketIdentifier());
    }

    private void handlePubRec(final ChannelHandlerContext ctx, final Mqtt5PubRecImpl pubRec) {

    }

    private void handlePubComp(final ChannelHandlerContext ctx, final Mqtt5PubCompImpl pubComp) {

    }

}
