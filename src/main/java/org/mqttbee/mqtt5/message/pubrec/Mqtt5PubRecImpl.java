package org.mqttbee.mqtt5.message.pubrec;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5PubRec;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecImpl implements Mqtt5PubRec {

    @NotNull
    public static final Mqtt5PubRecReasonCode DEFAULT_REASON_CODE = Mqtt5PubRecReasonCode.SUCCESS;

    private final Mqtt5PubRecReasonCode reasonCode;
    private final Mqtt5UTF8String reasonString;
    private final ImmutableList<Mqtt5UserProperty> userProperties;

    public Mqtt5PubRecImpl(
            @NotNull final Mqtt5PubRecReasonCode reasonCode, @Nullable final Mqtt5UTF8String reasonString,
            @NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
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

    @NotNull
    @Override
    public ImmutableList<Mqtt5UserProperty> getUserProperties() {
        return userProperties;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.PUBREC;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getPubRecEncoder().encode(this, channel, out);
    }

}
