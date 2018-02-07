package org.mqttbee.mqtt5.message.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5WillPublish;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5TopicImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5WillPublishImpl extends Mqtt5PublishImpl implements Mqtt5WillPublish {

    @NotNull
    private static final byte[] DEFAULT_NO_PAYLOAD = new byte[0];

    private final long delay;

    public Mqtt5WillPublishImpl(
            @NotNull final Mqtt5TopicImpl topic, @Nullable final byte[] payload, @NotNull final Mqtt5QoS qos,
            final boolean isRetain, final long messageExpiryInterval,
            @Nullable final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            @Nullable final Mqtt5UTF8StringImpl contentType, @Nullable final Mqtt5TopicImpl responseTopic,
            @Nullable final byte[] correlationData, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            final long delay) {

        super(topic, payload, qos, isRetain, messageExpiryInterval, payloadFormatIndicator, contentType, responseTopic,
                correlationData, TopicAliasUsage.MUST_NOT, userProperties);
        this.delay = delay;
    }

    @NotNull
    @Override
    public byte[] getRawPayload() {
        final byte[] payload = super.getRawPayload();
        return (payload == null) ? DEFAULT_NO_PAYLOAD : payload;
    }

    @Override
    public long getDelayInterval() {
        return delay;
    }

}
