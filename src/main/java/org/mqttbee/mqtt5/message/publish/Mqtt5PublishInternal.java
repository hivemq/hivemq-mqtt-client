package org.mqttbee.mqtt5.message.publish;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishInternal implements Mqtt5Message {

    public static final int NO_PACKET_IDENTIFIER_QOS_0 = -1;
    public static final ImmutableIntArray DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS = ImmutableIntArray.of();

    private final Mqtt5PublishImpl publish;
    private int packetIdentifier;
    private boolean isDup;
    private ImmutableIntArray subscriptionIdentifiers = DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS;

    public Mqtt5PublishInternal(@NotNull final Mqtt5PublishImpl publish) {
        this.publish = publish;
    }

    @NotNull
    public Mqtt5PublishImpl getPublish() {
        return publish;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.PUBLISH;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getPublishEncoder().encode(this, channel, out);
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public void setPacketIdentifier(final int packetIdentifier) {
        this.packetIdentifier = packetIdentifier;
    }

    public boolean isDup() {
        return isDup;
    }

    public void setDup(final boolean dup) {
        isDup = dup;
    }

    @NotNull
    public ImmutableIntArray getSubscriptionIdentifiers() {
        return subscriptionIdentifiers;
    }

    public void setSubscriptionIdentifiers(@NotNull final ImmutableIntArray subscriptionIdentifiers) {
        this.subscriptionIdentifiers = subscriptionIdentifiers;
    }

}
