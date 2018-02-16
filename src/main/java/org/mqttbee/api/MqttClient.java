package org.mqttbee.api;

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
