package org.mqttbee.mqtt.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.codec.encoder.MqttWrappedMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderApplier;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;

/**
 * Base class for wrapped MQTT messages with User Properties.
 *
 * @param <M> the type of the wrapped MQTT message.
 * @param <W> the type of the MQTT message wrapper.
 * @param <P> the type of the encoder provider for the MQTT message wrapper.
 */
public abstract class MqttWrappedMessage< //
        M extends MqttWrappedMessage<M, W, P>, //
        W extends MqttMessageWrapper<W, M, P>, //
        P extends MqttMessageEncoderProvider<W>> {

    final MqttWrappedMessageEncoderProvider<M, W, P> encoderProvider;
    private MqttWrappedMessageEncoderApplier<M, W> encoderApplier;

    private final MqttUserPropertiesImpl userProperties;

    protected MqttWrappedMessage(
            @NotNull final MqttUserPropertiesImpl userProperties,
            @Nullable final MqttWrappedMessageEncoderProvider<M, W, P> encoderProvider) {

        this.encoderProvider = encoderProvider;
        this.userProperties = userProperties;
    }

    /**
     * @return the encoder for this wrapped MQTT message.
     */
    @NotNull
    public MqttWrappedMessageEncoder<M, W> getEncoder() {
        if (encoderApplier == null) {
            if (encoderProvider == null) {
                throw new UnsupportedOperationException();
            }
            encoderApplier = encoderProvider.get();
        }
        return encoderApplier.apply(getCodable());
    }

    @NotNull
    public MqttUserPropertiesImpl getUserProperties() {
        return userProperties;
    }

    /**
     * @return the codable MQTT message.
     */
    @NotNull
    protected abstract M getCodable();

}
