package org.mqttbee.mqtt5.message.puback;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5PubAck;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckImpl implements Mqtt5PubAck {

    @NotNull
    public static final Mqtt5PubAckReasonCode DEFAULT_REASON_CODE = Mqtt5PubAckReasonCode.SUCCESS;

    private final Mqtt5PubAckReasonCode reasonCode;
    private final Mqtt5UTF8String reasonString;
    private final ImmutableList<Mqtt5UserProperty> userProperties;

    public Mqtt5PubAckImpl(
            @NotNull final Mqtt5PubAckReasonCode reasonCode, @Nullable final Mqtt5UTF8String reasonString,
            @NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
        this.reasonCode = reasonCode;
        this.reasonString = reasonString;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public Mqtt5PubAckReasonCode getReasonCode() {
        return reasonCode;
    }

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getReasonString() {
        return Optional.ofNullable(reasonString);
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

}
