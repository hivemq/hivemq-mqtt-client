package org.mqttbee.mqtt5.message.pubrel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5PubRel;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRelImpl implements Mqtt5PubRel {

    @NotNull public static final Mqtt5PubRelReasonCode DEFAULT_REASON_CODE = Mqtt5PubRelReasonCode.SUCCESS;

    private final Mqtt5PubRelReasonCode reasonCode;
    private final Mqtt5UTF8String reasonString;
    private final List<Mqtt5UserProperty> userProperties;

    public Mqtt5PubRelImpl(
            @NotNull final Mqtt5PubRelReasonCode reasonCode, @Nullable final Mqtt5UTF8String reasonString,
            @NotNull final List<Mqtt5UserProperty> userProperties) {
        this.reasonCode = reasonCode;
        this.reasonString = reasonString;
        this.userProperties = Collections.unmodifiableList(userProperties);
    }

    @NotNull
    @Override
    public Mqtt5PubRelReasonCode getReasonCode() {
        return reasonCode;
    }

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getReasonString() {
        return Optional.ofNullable(reasonString);
    }

    @NotNull
    @Override
    public List<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.PUBREL;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getPubRelEncoder().encode(this, channel, out);
    }

}
