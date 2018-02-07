package org.mqttbee.mqtt5.message.auth;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Auth;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5AuthEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.util.ByteBufUtil;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5AuthImpl extends Mqtt5Message.Mqtt5MessageWithReasonString implements Mqtt5Auth {

    private final Mqtt5AuthReasonCode reasonCode;
    private final Mqtt5UTF8StringImpl method;
    private final byte[] data;

    public Mqtt5AuthImpl(
            @NotNull final Mqtt5AuthReasonCode reasonCode, @NotNull final Mqtt5UTF8StringImpl method,
            @Nullable final byte[] data, @Nullable final Mqtt5UTF8StringImpl reasonString,
            @NotNull final Mqtt5UserPropertiesImpl userProperties) {
        super(reasonString, userProperties);
        this.reasonCode = reasonCode;
        this.method = method;
        this.data = data;
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
    public Optional<ByteBuf> getData() {
        return ByteBufUtil.optionalReadOnly(data);
    }

    @Nullable
    public byte[] getRawData() {
        return data;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5AuthEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5AuthEncoder.INSTANCE.encodedRemainingLength();
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5AuthEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
