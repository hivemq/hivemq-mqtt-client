package org.mqttbee.mqtt5.message.unsuback;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.Mqtt5UnsubAck;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubAckImpl implements Mqtt5UnsubAck {

    private final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes;
    private final Mqtt5UTF8StringImpl reasonString;
    private final Mqtt5UserProperties userProperties;

    public Mqtt5UnsubAckImpl(
            @NotNull final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserProperties userProperties) {
        this.reasonCodes = reasonCodes;
        this.reasonString = reasonString;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5UnsubAckReasonCode> getReasonCodes() {
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
