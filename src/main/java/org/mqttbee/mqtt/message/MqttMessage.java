package org.mqttbee.mqtt.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;

/**
 * MQTT message.
 *
 * @author Silvio Giebl
 */
public interface MqttMessage extends Mqtt5Message {

    /**
     * @return the encoder for this MQTT message.
     */
    @NotNull
    MqttMessageEncoder getEncoder();

}
