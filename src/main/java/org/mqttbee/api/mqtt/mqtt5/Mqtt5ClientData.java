package org.mqttbee.api.mqtt.mqtt5;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientData;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientData;
import org.mqttbee.api.mqtt.MqttClientExecutorConfig;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5ClientData extends MqttClientData {

    boolean followsRedirects();

    boolean allowsServerReAuth();

    @NotNull
    MqttClientExecutorConfig getExecutorConfig();

    @NotNull
    Optional<Mqtt5AdvancedClientData> getAdvancedClientData();

    @NotNull
    Optional<Mqtt5ClientConnectionData> getClientConnectionData();

    @NotNull
    Optional<Mqtt5ServerConnectionData> getServerConnectionData();

}
