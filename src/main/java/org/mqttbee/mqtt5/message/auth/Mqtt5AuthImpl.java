package org.mqttbee.mqtt5.message.auth;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Auth;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5AuthEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5AuthImpl extends Mqtt5Message.Mqtt5MessageWithProperties implements Mqtt5Auth {

    private final Mqtt5AuthReasonCode reasonCode;
    private final Mqtt5UTF8StringImpl method;
    private final byte[] data;
    private final Mqtt5UTF8StringImpl reasonString;
    private final Mqtt5UserProperties userProperties;

    public Mqtt5AuthImpl(
            @NotNull final Mqtt5AuthReasonCode reasonCode, @NotNull final Mqtt5UTF8StringImpl method,
            @Nullable final byte[] data, @Nullable final Mqtt5UTF8StringImpl reasonString,
            @NotNull final Mqtt5UserProperties userProperties) {
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
    public Mqtt5UTF8StringImpl getMethod() {
        return method;
    }

    @NotNull
    @Override
    public Optional<byte[]> getData() {
        return Optional.ofNullable(data);
    }

    @Nullable
    public byte[] getRawData() {
        return data;
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

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5AuthEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5AuthEncoder.INSTANCE.encodedRemainingLength(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5AuthEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
