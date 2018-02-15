package org.mqttbee.api;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public interface MqttClient {

    @NotNull
    MqttClientData getClientData();

}
