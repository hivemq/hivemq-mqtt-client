package org.mqttbee.mqtt5.handler.publish;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.exceptions.PacketIdentifiersExceededException;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQoS1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQoS2ControlProvider;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.publish.MqttTopicAliasMapping;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelBuilder;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.mqtt5.persistence.OutgoingQoSFlowPersistence;
import org.mqttbee.util.Ranges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static org.mqttbee.mqtt.message.publish.MqttPublishWrapper.*;

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
        if (msg instanceof MqttPublish) {
            handlePublish(ctx, (MqttPublish) msg, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private void handlePublish(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublish publish,
            @NotNull final ChannelPromise promise) {

        if (publish.getQos() == MqttQoS.AT_MOST_ONCE) {
            handlePublishQoS0(ctx, publish, promise);
        } else {
            handlePublishQoS1Or2(ctx, publish, promise);
        }
    }

    private void handlePublishQoS0(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublish publish,
            @NotNull final ChannelPromise promise) {

        final MqttPublishWrapper publishWrapper =
                wrapPublish(ctx.channel(), publish, NO_PACKET_IDENTIFIER_QOS_0, false);
        ctx.write(publishWrapper, promise);
    }

    private void handlePublishQoS1Or2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublish publish,
            @NotNull final ChannelPromise promise) {

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier < 0) {
            promise.setFailure(PacketIdentifiersExceededException.INSTANCE);
            return;
        }

        final MqttPublishWrapper publishWrapper = wrapPublish(ctx.channel(), publish, packetIdentifier, false);
        persistence.store(publishWrapper).whenCompleteAsync((aVoid, throwable) -> {
            ctx.writeAndFlush(publishWrapper, promise);
            if (throwable != null) {
                LOGGER.error("Unexpected exception while persisting PUBLISH in outgoing QoSFlowPersistence", throwable);
            }
        }, ctx.executor());
    }

    private MqttPublishWrapper wrapPublish(
            @NotNull final Channel channel, @NotNull final MqttPublish publish, final int packetIdentifier,
            final boolean isDup) {

        final MqttTopicAliasMapping topicAliasMapping = MqttServerConnectionData.getTopicAliasMapping(channel);
        int topicAlias;
        final boolean isNewTopicAlias;
        if (topicAliasMapping == null) {
            topicAlias = DEFAULT_NO_TOPIC_ALIAS;
            isNewTopicAlias = false;
        } else {
            final MqttTopicImpl topic = publish.getTopic();
            topicAlias = topicAliasMapping.get(topic);
            if (topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
                isNewTopicAlias = false;
            } else {
                topicAlias = topicAliasMapping.set(topic, publish.getTopicAliasUsage());
                isNewTopicAlias = topicAlias != DEFAULT_NO_TOPIC_ALIAS;
            }
        }
        return publish.wrap(packetIdentifier, isDup, topicAlias, isNewTopicAlias, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttPubAck) {
            handlePubAck(ctx, (MqttPubAck) msg);
        } else if (msg instanceof MqttPubRec) {
            handlePubRec(ctx, (MqttPubRec) msg);
        } else if (msg instanceof MqttPubComp) {
            handlePubComp(ctx, (MqttPubComp) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handlePubAck(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubAck pubAck) {
        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5OutgoingQoS1ControlProvider control = advanced.getOutgoingQoS1ControlProvider();
            if (control != null) {
                control.onPubAck(pubAck);
            }
        }

        finish(pubAck.getPacketIdentifier());
    }

    private void handlePubRec(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRec pubRec) {
        if (pubRec.getReasonCode().isError()) {
            handlePubRecError(ctx, pubRec);
        } else {
            handlePubRecSuccess(ctx, pubRec);
        }
    }

    private void handlePubRecError(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRec pubRec) {
        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5OutgoingQoS2ControlProvider control = advanced.getOutgoingQoS2ControlProvider();
            if (control != null) {
                control.onPubRecError(pubRec);
            }
        }

        finish(pubRec.getPacketIdentifier());
    }

    private void handlePubRecSuccess(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRec pubRec) {
        final MqttPubRelBuilder pubRelBuilder = new MqttPubRelBuilder(pubRec);

        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5OutgoingQoS2ControlProvider control = advanced.getOutgoingQoS2ControlProvider();
            if (control != null) {
                control.onPubRec(pubRec, pubRelBuilder);
            }
        }

        final MqttPubRel pubRel = pubRelBuilder.build();
        persistence.store(pubRel).whenCompleteAsync((aVoid, throwable) -> {
            ctx.writeAndFlush(pubRel);
            if (throwable != null) {
                LOGGER.error("Unexpected exception while persisting PUBREL in outgoing QoSFlowPersistence", throwable);
            }
        }, ctx.executor());
    }

    private void handlePubComp(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubComp pubComp) {
        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5OutgoingQoS2ControlProvider control = advanced.getOutgoingQoS2ControlProvider();
            if (control != null) {
                control.onPubComp(pubComp);
            }
        }

        finish(pubComp.getPacketIdentifier());
    }

    private void finish(final int packetIdentifier) {
        persistence.discard(packetIdentifier);
        packetIdentifiers.returnId(packetIdentifier);
    }

}
