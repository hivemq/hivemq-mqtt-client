package org.mqttbee.api.mqtt;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public interface MqttClient {

    @NotNull
    static MqttClientBuilder builder() {
        return new MqttClientBuilder();
    }

    @NotNull
    MqttClientData getClientData();

}
