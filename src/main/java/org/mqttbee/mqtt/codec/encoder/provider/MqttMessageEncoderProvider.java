package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.mqtt.message.MqttMessage;

import java.util.function.Supplier;

/**
 * Provider for a {@link MqttMessageEncoderApplier}.
 *
 * @param <M> the type of the MQTT message.
 * @author Silvio Giebl
 */
public interface MqttMessageEncoderProvider<M extends MqttMessage<M, ?>>
        extends Supplier<MqttMessageEncoderApplier<M>> {

}
