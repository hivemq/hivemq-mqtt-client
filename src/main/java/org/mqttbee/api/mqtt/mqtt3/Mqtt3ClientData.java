package org.mqttbee.api.mqtt.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientData;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public interface Mqtt3ClientData extends MqttClientData {

    @NotNull
    Optional<Mqtt3ClientConnectionData> getClientConnectionData();

    @NotNull
    Optional<Mqtt3ServerConnectionData> getServerConnectionData();

}
