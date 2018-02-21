package org.mqttbee.api;

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

    boolean isConnecting();

    boolean isConnected();

}
