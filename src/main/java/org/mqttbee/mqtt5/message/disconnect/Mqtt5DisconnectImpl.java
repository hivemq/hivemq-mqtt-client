package org.mqttbee.mqtt5.message.disconnect;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5DisconnectEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectImpl extends Mqtt5Message.Mqtt5MessageWithProperties implements Mqtt5Disconnect {

    @NotNull
    public static final Mqtt5DisconnectReasonCode DEFAULT_REASON_CODE = Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;
    public static final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;

    private final Mqtt5DisconnectReasonCode reasonCode;
    private final long sessionExpiryInterval;
    private final Mqtt5UTF8String serverReference;
    private final Mqtt5UTF8String reasonString;
    private final Mqtt5UserProperties userProperties;

    public Mqtt5DisconnectImpl(
            @NotNull final Mqtt5DisconnectReasonCode reasonCode, final long sessionExpiryInterval,
            @Nullable final Mqtt5UTF8String serverReference, @Nullable final Mqtt5UTF8String reasonString,
            @NotNull final Mqtt5UserProperties userProperties) {
        this.reasonCode = reasonCode;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.serverReference = serverReference;
        this.reasonString = reasonString;
        this.userProperties = userProperties;
    }

    @NotNull
    @Override
    public Mqtt5DisconnectReasonCode getReasonCode() {
        return reasonCode;
    }

    @NotNull
    @Override
    public Optional<Long> getSessionExpiryInterval() {
        return (sessionExpiryInterval == SESSION_EXPIRY_INTERVAL_FROM_CONNECT) ? Optional.empty() :
                Optional.of(sessionExpiryInterval);
    }

    public long getRawSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getServerReference() {
        return Optional.ofNullable(serverReference);
    }

    @Nullable
    public Mqtt5UTF8String getRawServerReference() {
        return serverReference;
    }

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getReasonString() {
        return Optional.ofNullable(reasonString);
    }

    @Nullable
    public Mqtt5UTF8String getRawReasonString() {
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
        Mqtt5DisconnectEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5DisconnectEncoder.INSTANCE.encodedRemainingLength(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5DisconnectEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
