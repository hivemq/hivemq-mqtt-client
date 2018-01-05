package org.mqttbee.mqtt5.message.subscribe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5SubscribeEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeInternal extends Mqtt5Message.Mqtt5MessageWithProperties {

    public static final int DEFAULT_NO_SUBSCRIPTION_IDENTIFIER = -1;

    private final Mqtt5SubscribeImpl subscribe;
    private int packetIdentfier;
    private int subscriptionIdentifier = DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

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

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5SubscribeEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5SubscribeEncoder.INSTANCE.encodedRemainingLength(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5SubscribeEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
