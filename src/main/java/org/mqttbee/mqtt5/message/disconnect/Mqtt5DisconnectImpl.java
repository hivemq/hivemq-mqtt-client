package org.mqttbee.mqtt5.message.disconnect;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5Disconnect;
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
public class Mqtt5DisconnectImpl implements Mqtt5Disconnect {

    public final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;

    private final Mqtt5DisconnectReasonCode reasonCode;
    private final long sessionExpiryInterval;
    private final Mqtt5UTF8String serverReference;
    private final Mqtt5UTF8String reasonString;
    private final List<Mqtt5UserProperty> userProperties;

    public Mqtt5DisconnectImpl(
            @NotNull final Mqtt5DisconnectReasonCode reasonCode, final long sessionExpiryInterval,
            @Nullable final Mqtt5UTF8String serverReference, @Nullable final Mqtt5UTF8String reasonString,
            @NotNull final List<Mqtt5UserProperty> userProperties) {
        this.reasonCode = reasonCode;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.serverReference = serverReference;
        this.reasonString = reasonString;
        this.userProperties = Collections.unmodifiableList(userProperties);
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

    @NotNull
    @Override
    public Optional<Mqtt5UTF8String> getServerReference() {
        return Optional.ofNullable(serverReference);
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
        return Mqtt5MessageType.DISCONNECT;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getDisconnectEncoder().encode(this, channel, out);
    }

}
