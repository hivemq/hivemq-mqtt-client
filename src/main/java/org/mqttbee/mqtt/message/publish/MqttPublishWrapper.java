package org.mqttbee.mqtt.message.publish;

import com.google.common.primitives.ImmutableIntArray;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttServerConnectionDataImpl;
import org.mqttbee.mqtt.codec.encoder.provider.MqttPublishEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.message.MqttMessageWrapper.MqttMessageWrapperWithId;

/**
 * @author Silvio Giebl
 */
public class MqttPublishWrapper
        extends MqttMessageWrapperWithId<MqttPublishWrapper, MqttPublishImpl, MqttPublishEncoderProvider>
        implements MqttQoSMessage {

    public static final int NO_PACKET_IDENTIFIER_QOS_0 = -1;
    public static final int DEFAULT_NO_TOPIC_ALIAS = -1;
    @NotNull
    public static final ImmutableIntArray DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS = ImmutableIntArray.of();

    private final boolean isDup;
    private final int topicAlias;
    private final boolean isNewTopicAlias;
    private final ImmutableIntArray subscriptionIdentifiers;

    MqttPublishWrapper(
            @NotNull final MqttPublishImpl publish, final int packetIdentifier, final boolean isDup,
            final int topicAlias, final boolean isNewTopicAlias,
            @NotNull final ImmutableIntArray subscriptionIdentifiers) {

        super(publish, packetIdentifier);
        this.isDup = isDup;
        this.topicAlias = topicAlias;
        this.isNewTopicAlias = isNewTopicAlias;
        this.subscriptionIdentifiers = subscriptionIdentifiers;
    }

    public MqttPublishWrapper( // TODO
            @NotNull final MqttPublishImpl publish, final int packetIdentifier, final boolean isDup,
            @NotNull final Channel channel, @NotNull final ImmutableIntArray subscriptionIdentifiers) {

        super(publish, packetIdentifier);
        this.isDup = isDup;

        final MqttTopicAliasMapping topicAliasMapping = MqttServerConnectionDataImpl.getTopicAliasMapping(channel);
        if (topicAliasMapping == null) {
            this.topicAlias = DEFAULT_NO_TOPIC_ALIAS;
            this.isNewTopicAlias = false;
        } else {
            final MqttTopicImpl topic = publish.getTopic();
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

    @NotNull
    @Override
    protected MqttPublishWrapper getCodable() {
        return this;
    }

}
