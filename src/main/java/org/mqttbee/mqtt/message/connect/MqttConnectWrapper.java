package org.mqttbee.mqtt.message.connect;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuthImpl;

/**
 * @author Silvio Giebl
 */
public class MqttConnectWrapper extends
        MqttMessageWrapper<MqttConnectWrapper, MqttConnectImpl, MqttMessageEncoderProvider<MqttConnectWrapper>> {

    private final MqttClientIdentifierImpl clientIdentifier;
    private final MqttEnhancedAuthImpl enhancedAuth;

    MqttConnectWrapper(
            @NotNull final MqttConnectImpl wrapped, @NotNull final MqttClientIdentifierImpl clientIdentifier,
            @Nullable final MqttEnhancedAuthImpl enhancedAuth) {

        super(wrapped);
        this.clientIdentifier = clientIdentifier;
        this.enhancedAuth = enhancedAuth;
    }

    @NotNull
    public MqttClientIdentifierImpl getClientIdentifier() {
        return clientIdentifier;
    }

    @Nullable
    public MqttEnhancedAuthImpl getEnhancedAuth() {
        return enhancedAuth;
    }

    @NotNull
    @Override
    protected MqttConnectWrapper getCodable() {
        return this;
    }

}
