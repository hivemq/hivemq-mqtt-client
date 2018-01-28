package org.mqttbee.mqtt5.message.suback;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5SubAck;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubAckImpl implements Mqtt5SubAck {

    private final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes;
    private final Mqtt5UTF8String reasonString;
    private final Mqtt5UserProperties userProperties;

    public Mqtt5SubAckImpl(
            @NotNull final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes,
            @Nullable final Mqtt5UTF8String reasonString, @NotNull final Mqtt5UserProperties userProperties) {
        this.reasonCodes = reasonCodes;
        this.reasonString = reasonString;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5SubAckReasonCode> getReasonCodes() {
        return reasonCodes;
    }

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getReasonString() {
        return Optional.ofNullable(reasonString);
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5UserProperty> getUserProperties() {
        return userProperties.asList();
    }

}
