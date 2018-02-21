package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.message.MqttMessageWrapper;
import org.mqttbee.mqtt.message.MqttWrappedMessage;

/**
 * Provider and applier for a {@link MqttMessageEncoder} for a {@link MqttMessageWrapper}.
 *
 * @param <W> the type of the MQTT message wrapper.
 * @param <M> the type of the wrapped MQTT message.
 * @param <P> the type of the encoder provider of the MQTT message wrapper.
 * @author Silvio Giebl
 */
public class MqttMessageWrapperEncoderProvider< //
        W extends MqttMessageWrapper<W, M, P>, //
        M extends MqttWrappedMessage<M, W, P>, //
        P extends MqttMessageEncoderProvider<W>>
        implements MqttMessageEncoderProvider<W>, MqttMessageEncoderApplier<W> {

    @Override
    public MqttMessageEncoderApplier<W> get() {
        return this;
    }

    @NotNull
    @Override
    public MqttMessageEncoder apply(@NotNull final W message) {
        return message.getWrapped().getEncoder().wrap(message);
    }

}
