package org.mqttbee.mqtt5.message.publish;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5WillPublish;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

/**
 * @author Silvio Giebl
 */
public class Mqtt5WillPublishImpl extends Mqtt5PublishImpl implements Mqtt5WillPublish {

    @Nullable
    public static final Mqtt5WillPublishImpl DEFAULT_NO_WILL_PUBLISH = null;
    @NotNull
    private static final byte[] DEFAULT_NO_PAYLOAD = new byte[0];

    private final long delay;

    Mqtt5WillPublishImpl(
            @NotNull final Mqtt5Topic topic, @Nullable final byte[] payload, @NotNull final Mqtt5QoS qos,
            final boolean isRetain, final int messageExpiryInterval,
            @NotNull final Mqtt5PayloadFormatIndicator payloadFormatIndicator,
            @Nullable final Mqtt5UTF8String contentType, @Nullable final Mqtt5UTF8String responseTopic,
            @Nullable final byte[] correlationData, @NotNull final ImmutableList<Mqtt5UserProperty> userProperties,
            final long delay) {

        super(topic, payload, qos, isRetain, messageExpiryInterval, payloadFormatIndicator, contentType, responseTopic,
                correlationData, userProperties);
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
