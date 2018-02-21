package org.mqttbee.mqtt.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;

/**
 * MQTT message.
 *
 * @author Silvio Giebl
 */
public interface MqttMessage {

    /**
     * @return the encoder for this MQTT message.
     */
    @NotNull
    MqttMessageEncoder getEncoder();

}
