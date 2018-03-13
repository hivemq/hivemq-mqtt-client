package org.mqttbee.api.mqtt;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttClientIdentifier;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface MqttClientData {

    @NotNull
    Optional<MqttClientIdentifier> getClientIdentifier();

    @NotNull
    String getServerHost();

    int getServerPort();

    boolean usesSSL();

    @NotNull
    MqttClientExecutorConfig getExecutorConfig();

    boolean isConnecting();

    boolean isConnected();

}
