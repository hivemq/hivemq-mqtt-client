package org.mqttbee.mqtt5.message.disconnect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5MessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

import java.util.Optional;
import java.util.function.Function;

import static org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithReasonString;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectImpl extends Mqtt5MessageWithReasonString<Mqtt5DisconnectImpl> implements Mqtt5Disconnect {

    @NotNull
    public static final Mqtt5DisconnectReasonCode DEFAULT_REASON_CODE = Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;
    public static final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;

    private final Mqtt5DisconnectReasonCode reasonCode;
    private final long sessionExpiryInterval;
    private final Mqtt5UTF8StringImpl serverReference;

    public Mqtt5DisconnectImpl(
            @NotNull final Mqtt5DisconnectReasonCode reasonCode, final long sessionExpiryInterval,
            @Nullable final Mqtt5UTF8StringImpl serverReference, @Nullable final Mqtt5UTF8StringImpl reasonString,
            @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5DisconnectImpl, ? extends Mqtt5MessageEncoder<Mqtt5DisconnectImpl>> encoderProvider) {

        super(reasonString, userProperties, encoderProvider);
        this.reasonCode = reasonCode;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.serverReference = serverReference;
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
    public Mqtt5UTF8StringImpl getRawServerReference() {
        return serverReference;
    }

    @Override
    protected Mqtt5DisconnectImpl getCodable() {
        return this;
    }

}
