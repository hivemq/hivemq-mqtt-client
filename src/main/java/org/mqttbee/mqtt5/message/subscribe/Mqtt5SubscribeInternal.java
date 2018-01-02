package org.mqttbee.mqtt5.message.subscribe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeInternal implements Mqtt5Message {

    private final Mqtt5SubscribeImpl subscribe;
    private int packetIdentfier;
    private int subscriptionIdentifier;

    public Mqtt5SubscribeInternal(@NotNull final Mqtt5SubscribeImpl subscribe) {
        this.subscribe = subscribe;
    }

    @NotNull
    public Mqtt5SubscribeImpl getSubscribe() {
        return subscribe;
    }

    public int getPacketIdentfier() {
        return packetIdentfier;
    }

    public void setPacketIdentfier(final int packetIdentfier) {
        this.packetIdentfier = packetIdentfier;
    }

    public int getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    public void setSubscriptionIdentifier(final int subscriptionIdentifier) {
        this.subscriptionIdentifier = subscriptionIdentifier;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.SUBSCRIBE;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getSubscribeEncoder().encode(this, channel, out);
    }

}
