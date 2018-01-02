package org.mqttbee.mqtt5.message.suback;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5SubAck;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubAckImpl implements Mqtt5SubAck {

    private final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes;
    private final Mqtt5UTF8String reasonString;
    private final ImmutableList<Mqtt5UserProperty> userProperties;

    public Mqtt5SubAckImpl(
            @NotNull final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes,
            @Nullable final Mqtt5UTF8String reasonString,
            @NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
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
        return userProperties;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.SUBACK;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getSubAckEncoder().encode(this, channel, out);
    }

}
