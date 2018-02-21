package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessage;

/**
 * Applies a {@link MqttMessage} to a {@link MqttMessageEncoder}.
 *
 * @param <M> the type of the MQTT message.
 * @author Silvio Giebl
 */
public interface MqttMessageEncoderApplier<M extends MqttMessage> {

    /**
     * Returns a encoder for a MQTT message that is applied to the given MQTT message.
     *
     * @param message the MQTT message.
     * @return the encoder for the MQTT message encoder.
     */
    @NotNull
    MqttMessageEncoder apply(@NotNull M message);

}
