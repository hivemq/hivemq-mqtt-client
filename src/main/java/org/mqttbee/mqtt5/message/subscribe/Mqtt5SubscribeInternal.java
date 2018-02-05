package org.mqttbee.mqtt5.message.subscribe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5SubscribeEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeInternal extends Mqtt5Message.Mqtt5MessageWithUserPropertiesWrapper<Mqtt5SubscribeImpl> {

    public static final int DEFAULT_NO_SUBSCRIPTION_IDENTIFIER = -1;

    private final int packetIdentifier;
    private final int subscriptionIdentifier;

    public Mqtt5SubscribeInternal(@NotNull final Mqtt5SubscribeImpl subscribe, final int packetIdentifier) {
        this(subscribe, packetIdentifier, DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);
    }

    public Mqtt5SubscribeInternal(
            @NotNull final Mqtt5SubscribeImpl subscribe, final int packetIdentifier, final int subscriptionIdentifier) {
        super(subscribe);
        this.packetIdentifier = packetIdentifier;
        this.subscriptionIdentifier = subscriptionIdentifier;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public int getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5SubscribeEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int additionalRemainingLength() {
        return 0;
    }

    @Override
    protected int additionalPropertyLength() {
        return Mqtt5SubscribeEncoder.INSTANCE.additionalPropertyLength(this);
    }

}
