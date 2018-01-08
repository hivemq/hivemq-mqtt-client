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
    private final int packetIdentifier;
    private final boolean isDup;
    private final int topicAlias;
    private final ImmutableIntArray subscriptionIdentifiers;

    public Mqtt5PublishInternal(@NotNull final Mqtt5PublishImpl publish) {
        this(publish, NO_PACKET_IDENTIFIER_QOS_0);
    }

    public Mqtt5PublishInternal(@NotNull final Mqtt5PublishImpl publish, final int packetIdentifier) {
        this(publish, packetIdentifier, false);
    }

    public Mqtt5PublishInternal(
            @NotNull final Mqtt5PublishImpl publish, final int packetIdentifier, final boolean isDup) {
        this(publish, packetIdentifier, isDup, DEFAULT_NO_TOPIC_ALIAS, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
    }

    public Mqtt5PublishInternal(
            @NotNull final Mqtt5PublishImpl publish, final int packetIdentifier, final boolean isDup,
            final int topicAlias, @NotNull final ImmutableIntArray subscriptionIdentifiers) {
        this.publish = publish;
        this.packetIdentifier = packetIdentifier;
        this.isDup = isDup;
        this.topicAlias = topicAlias;
        this.subscriptionIdentifiers = subscriptionIdentifiers;
    }

    @NotNull
    public Mqtt5PublishImpl getPublish() {
        return publish;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public boolean isDup() {
        return isDup;
    }

    public int getTopicAlias() {
        return topicAlias;
    }

    @NotNull
    public ImmutableIntArray getSubscriptionIdentifiers() {
        return subscriptionIdentifiers;
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
