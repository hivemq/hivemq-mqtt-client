package org.mqttbee.mqtt5.message.pubrec;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5PubRec;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecImpl implements Mqtt5PubRec {

    @NotNull
    public static final Mqtt5PubRecReasonCode DEFAULT_REASON_CODE = Mqtt5PubRecReasonCode.SUCCESS;

    private final Mqtt5PubRecReasonCode reasonCode;
    private final Mqtt5UTF8StringImpl reasonString;
    private final Mqtt5UserProperties userProperties;

    public Mqtt5PubRecImpl(
            @NotNull final Mqtt5PubRecReasonCode reasonCode, @Nullable final Mqtt5UTF8StringImpl reasonString,
            @NotNull final Mqtt5UserProperties userProperties) {
        this.reasonCode = reasonCode;
        this.reasonString = reasonString;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public Mqtt5PubRecReasonCode getReasonCode() {
        return reasonCode;
    }

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getReasonString() {
        return Optional.ofNullable(reasonString);
    }

    @Nullable
    public Mqtt5UTF8StringImpl getRawReasonString() {
        return reasonString;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5UserProperty> getUserProperties() {
        return userProperties.asList();
    }

    @NotNull
    public Mqtt5UserProperties getRawUserProperties() {
        return userProperties;
    }

}
