package org.mqttbee.api.mqtt5.message.publish;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;
import org.mqttbee.util.UnsignedDataTypes;

import static org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl.DEFAULT_DELAY_INTERVAL;

/**
 * @author Silvio Giebl
 */
public class Mqtt5WillPublishBuilder extends Mqtt5PublishBuilder {

    private long delayInterval = DEFAULT_DELAY_INTERVAL;

    @NotNull
    @Override
    @Deprecated
    public Mqtt5PublishBuilder withTopicAliasUsage(@NotNull final TopicAliasUsage topicAliasUsage) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public Mqtt5PublishBuilder withDelayInterval(final long delayInterval) {
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(delayInterval));
        this.delayInterval = delayInterval;
        return this;
    }

    @NotNull
    @Override
    public Mqtt5WillPublish build() {
        return new Mqtt5WillPublishImpl(topic, payload, qos, retain, messageExpiryInterval, payloadFormatIndicator,
                contentType, responseTopic, correlationData, userProperties, delayInterval);
    }

}
