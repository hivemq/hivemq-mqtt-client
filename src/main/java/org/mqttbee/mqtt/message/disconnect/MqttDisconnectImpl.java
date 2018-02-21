package org.mqttbee.mqtt.message.disconnect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithReasonCode;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class MqttDisconnectImpl extends
        MqttMessageWithReasonCode<MqttDisconnectImpl, Mqtt5DisconnectReasonCode, MqttMessageEncoderProvider<MqttDisconnectImpl>>
        implements Mqtt5Disconnect {

    @NotNull
    public static final Mqtt5DisconnectReasonCode DEFAULT_REASON_CODE = Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION;
    public static final long SESSION_EXPIRY_INTERVAL_FROM_CONNECT = -1;

    private final long sessionExpiryInterval;
    private final MqttUTF8StringImpl serverReference;

    public MqttDisconnectImpl(
            @NotNull final Mqtt5DisconnectReasonCode reasonCode, final long sessionExpiryInterval,
            @Nullable final MqttUTF8StringImpl serverReference, @Nullable final MqttUTF8StringImpl reasonString,
            @NotNull final MqttUserPropertiesImpl userProperties,
            @NotNull final MqttMessageEncoderProvider<MqttDisconnectImpl> encoderProvider) {

        super(reasonCode, reasonString, userProperties, encoderProvider);
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.serverReference = serverReference;
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
    public Optional<MqttUTF8String> getServerReference() {
        return Optional.ofNullable(serverReference);
    }

    @Nullable
    public MqttUTF8StringImpl getRawServerReference() {
        return serverReference;
    }

    @NotNull
    @Override
    protected MqttDisconnectImpl getCodable() {
        return this;
    }

}
