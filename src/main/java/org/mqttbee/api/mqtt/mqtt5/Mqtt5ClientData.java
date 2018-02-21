package org.mqttbee.api.mqtt.mqtt5;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientData;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5ClientData extends MqttClientData {

    @NotNull
    Optional<Mqtt5ClientConnectionData> getClientConnectionData();

    @NotNull
    Optional<Mqtt5ServerConnectionData> getServerConnectionData();

    boolean followsRedirects();

    boolean allowsServerReAuth();

}
