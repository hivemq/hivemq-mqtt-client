package org.mqttbee.mqtt5.message.auth;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Auth;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5AuthImpl implements Mqtt5Auth, Mqtt5Message {

    private final Mqtt5AuthReasonCode reasonCode;
    private final Mqtt5UTF8String method;
    private final byte[] data;
    private final Mqtt5UTF8String reasonString;
    private final ImmutableList<Mqtt5UserProperty> userProperties;

    public Mqtt5AuthImpl(
            @NotNull final Mqtt5AuthReasonCode reasonCode, @NotNull final Mqtt5UTF8String method,
            @Nullable final byte[] data, @Nullable final Mqtt5UTF8String reasonString,
            @NotNull final ImmutableList<Mqtt5UserProperty> userProperties) {
        this.reasonCode = reasonCode;
        this.method = method;
        this.data = data;
        this.reasonString = reasonString;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public Mqtt5AuthReasonCode getReasonCode() {
        return reasonCode;
    }

    @NotNull
    @Override
    public Mqtt5UTF8String getMethod() {
        return method;
    }

    @NotNull
    @Override
    public Optional<byte[]> getData() {
        return Optional.ofNullable(data);
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
        return Mqtt5MessageType.AUTH;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getAuthEncoder().encode(this, channel, out);
    }

}
