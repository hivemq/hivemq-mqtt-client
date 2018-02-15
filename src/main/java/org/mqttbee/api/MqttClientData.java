package org.mqttbee.api;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5ClientIdentifier;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface MqttClientData {

    @NotNull
    Optional<Mqtt5ClientIdentifier> getClientIdentifier();

    @NotNull
    String getServerHost();

    int getServerPort();

    boolean isConnected();

}
