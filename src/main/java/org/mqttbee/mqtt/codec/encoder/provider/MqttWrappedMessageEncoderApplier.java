package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.MqttWrappedMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * Applies a {@link MqttWrappedMessage} to a {@link MqttWrappedMessageEncoder}.
 *
 * @param <M> the type of the wrapped MQTT message.
 * @param <W> the type of the MQTT message wrapper.
 * @author Silvio Giebl
 */
public interface MqttWrappedMessageEncoderApplier<M extends MqttWrappedMessage<M, W, ?>, W extends MqttMessageWrapper<W, M, ?>> {

    /**
     * Returns a encoder for a wrapped MQTT message that is applied to the given wrapped MQTT message.
     *
     * @param message the wrapped MQTT message.
     * @return the encoder for the wrapped MQTT message.
     */
    @NotNull
    MqttWrappedMessageEncoder<M, W> apply(@NotNull M message);

}
