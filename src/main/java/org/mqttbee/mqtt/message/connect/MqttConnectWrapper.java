package org.mqttbee.mqtt.message.connect;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuth;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttConnectWrapper extends
        MqttMessageWrapper<MqttConnectWrapper, MqttConnect, MqttMessageEncoderProvider<MqttConnectWrapper>> {

    private final MqttClientIdentifierImpl clientIdentifier;
    private final MqttEnhancedAuth enhancedAuth;

    MqttConnectWrapper(
            @NotNull final MqttConnect wrapped, @NotNull final MqttClientIdentifierImpl clientIdentifier,
            @Nullable final MqttEnhancedAuth enhancedAuth) {

        super(wrapped);
        this.clientIdentifier = clientIdentifier;
        this.enhancedAuth = enhancedAuth;
    }

    @NotNull
    public MqttClientIdentifierImpl getClientIdentifier() {
        return clientIdentifier;
    }

    @Nullable
    public MqttEnhancedAuth getEnhancedAuth() {
        return enhancedAuth;
    }

    @NotNull
    @Override
    protected MqttConnectWrapper getCodable() {
        return this;
    }

}
