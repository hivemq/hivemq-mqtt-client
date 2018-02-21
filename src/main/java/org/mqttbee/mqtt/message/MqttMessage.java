package org.mqttbee.mqtt.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderApplier;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;

/**
 * MQTT message.
 *
 * @param <M> the type of the MQTT message.
 * @param <P> the type of the encoder provider for the MQTT message.
 * @author Silvio Giebl
 */
public abstract class MqttMessage<M extends MqttMessage<M, P>, P extends MqttMessageEncoderProvider<M>> {

    protected final P encoderProvider;
    private MqttMessageEncoderApplier<M> encoderApplier;

    protected MqttMessage(@Nullable final P encoderProvider) {
        this.encoderProvider = encoderProvider;
    }

    /**
     * @return the encoder for this MQTT message.
     */
    @NotNull
    public MqttMessageEncoder<M> getEncoder() {
        if (encoderApplier == null) {
            if (encoderProvider == null) {
                throw new UnsupportedOperationException();
            }
            encoderApplier = encoderProvider.get();
        }
        return encoderApplier.apply(getCodable());
    }

    /**
     * @return the codable MQTT message.
     */
    @NotNull
    protected abstract M getCodable();

}
