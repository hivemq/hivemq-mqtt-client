package org.mqttbee.mqtt5.message.publish;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.handler.Mqtt5ServerDataImpl;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage.Mqtt5MessageWrapperWithPacketId;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishWrapper extends Mqtt5MessageWrapperWithPacketId<Mqtt5PublishWrapper, Mqtt5PublishImpl>
        implements Mqtt5QoSMessage {

    public static final int NO_PACKET_IDENTIFIER_QOS_0 = -1;
    public static final int DEFAULT_NO_TOPIC_ALIAS = -1;
    @NotNull
    public static final ImmutableIntArray DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS = ImmutableIntArray.of();

    private final boolean isDup;
    private final int topicAlias;
    private final boolean isNewTopicAlias;
    private final ImmutableIntArray subscriptionIdentifiers;

    Mqtt5PublishWrapper(
            @NotNull final Mqtt5PublishImpl publish, final int packetIdentifier, final boolean isDup,
            final int topicAlias, final boolean isNewTopicAlias,
            @NotNull final ImmutableIntArray subscriptionIdentifiers) {

        super(publish, packetIdentifier);
        this.isDup = isDup;
        this.topicAlias = topicAlias;
        this.isNewTopicAlias = isNewTopicAlias;
        this.subscriptionIdentifiers = subscriptionIdentifiers;
    }

    public Mqtt5PublishWrapper( // TODO
            @NotNull final Mqtt5PublishImpl publish, final int packetIdentifier, final boolean isDup,
            @NotNull final Channel channel, @NotNull final ImmutableIntArray subscriptionIdentifiers) {

        super(publish, packetIdentifier);
        this.isDup = isDup;

        final Mqtt5TopicAliasMapping topicAliasMapping = Mqtt5ServerDataImpl.get(channel).getTopicAliasMapping();
        if (topicAliasMapping == null) {
            this.topicAlias = DEFAULT_NO_TOPIC_ALIAS;
            this.isNewTopicAlias = false;
        } else {
            final Mqtt5TopicImpl topic = publish.getTopic();
            final int topicAlias = topicAliasMapping.get(topic);
            if (topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
                this.topicAlias = topicAlias;
                this.isNewTopicAlias = false;
            } else {
                this.topicAlias = topicAliasMapping.set(topic, publish.getTopicAliasUsage());
                this.isNewTopicAlias = this.topicAlias != DEFAULT_NO_TOPIC_ALIAS;
            }
        }

        this.subscriptionIdentifiers = subscriptionIdentifiers;
    }

    public boolean isDup() {
        return isDup;
    }

    public int getTopicAlias() {
        return topicAlias;
    }

    public boolean isNewTopicAlias() {
        return isNewTopicAlias;
    }

    @NotNull
    public ImmutableIntArray getSubscriptionIdentifiers() {
        return subscriptionIdentifiers;
    }

    @Override
    protected Mqtt5PublishWrapper getCodable() {
        return this;
    }

}
