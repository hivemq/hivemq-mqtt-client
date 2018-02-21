package org.mqttbee.api.mqtt;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public interface MqttClient<T extends MqttClientData> {

    static MqttClientBuilder builder() {
        return new MqttClientBuilder();
    }

    @NotNull
    T getClientData();

}
