package org.mqttbee.mqtt5.message.publish;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PublishEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishInternal extends Mqtt5Message.Mqtt5MessageWithProperties {

    public static final int NO_PACKET_IDENTIFIER_QOS_0 = -1;
    public static final int DEFAULT_NO_TOPIC_ALIAS = -1;
    @NotNull
    public static final ImmutableIntArray DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS = ImmutableIntArray.of();

    private final Mqtt5PublishImpl publish;
    private int packetIdentifier;
    private boolean isDup;
    private int topicAlias = DEFAULT_NO_TOPIC_ALIAS;
    private ImmutableIntArray subscriptionIdentifiers = DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS;

    public Mqtt5PublishInternal(@NotNull final Mqtt5PublishImpl publish) {
        this.publish = publish;
    }

    @NotNull
    public Mqtt5PublishImpl getPublish() {
        return publish;
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

    public int getTopicAlias() {
        return topicAlias;
    }

    public void setTopicAlias(final int topicAlias) {
        this.topicAlias = topicAlias;
    }

    @NotNull
    public ImmutableIntArray getSubscriptionIdentifiers() {
        return subscriptionIdentifiers;
    }

    public void setSubscriptionIdentifiers(@NotNull final ImmutableIntArray subscriptionIdentifiers) {
        this.subscriptionIdentifiers = subscriptionIdentifiers;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5PublishEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5PublishEncoder.INSTANCE.encodedRemainingLength(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5PublishEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
