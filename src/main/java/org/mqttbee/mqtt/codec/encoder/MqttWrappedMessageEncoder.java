package org.mqttbee.mqtt.codec.encoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * Encoder for a wrapped MQTT message.
 *
 * @param <M> the type of the wrapped MQTT message.
 * @param <W> the type of the MQTT message wrapper.
 * @author Silvio Giebl
 */
public interface MqttWrappedMessageEncoder<M extends MqttWrappedMessage<M, W, ?>, W extends MqttMessageWrapper<W, M, ?>> {

    /**
     * Returns the encoder for the given wrapper around the MQTT message.
     *
     * @param wrapper the MQTT message wrapper.
     * @return the encoder for the MQTT message wrapper
     */
    @NotNull
    MqttMessageEncoder<W> wrap(@NotNull final W wrapper);

}
