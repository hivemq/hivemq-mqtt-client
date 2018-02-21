package org.mqttbee.mqtt.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;

/**
 * Base class for wrappers around MQTT messages with additional state-specific data.
 *
 * @param <W> the type of the MQTT message wrapper.
 * @param <M> the type of the wrapped MQTT message.
 * @param <P> the type of the encoder provider for the MQTT message wrapper.
 * @author Silvio Giebl
 */
public abstract class MqttMessageWrapper< //
        W extends MqttMessageWrapper<W, M, P>, //
        M extends MqttWrappedMessage<M, W, P>, //
        P extends MqttMessageEncoderProvider<W>> //
        extends MqttMessageWithEncoder<W, P> {

    private final M wrapped;

    protected MqttMessageWrapper(@NotNull final M wrapped) {
        super(wrapped.encoderProvider.getWrapperEncoderProvider());
        this.wrapped = wrapped;
    }

    @Override
    public Mqtt5MessageType getType() {
        return wrapped.getType();
    }

    /**
     * @return the wrapped MQTT message.
     */
    @NotNull
    public M getWrapped() {
        return wrapped;
    }


    /**
     * Base class for wrappers around MQTT messages with a packet identifier and additional state-specific data.
     *
     * @param <W> the type of the MQTT message wrapper.
     * @param <M> the type of the wrapped MQTT message.
     * @param <P> the type of the encoder provider for the MQTT message wrapper.
     * @author Silvio Giebl
     */
    public abstract static class MqttMessageWrapperWithId< //
            W extends MqttMessageWrapper<W, M, P>, //
            M extends MqttWrappedMessage<M, W, P>, //
            P extends MqttMessageEncoderProvider<W>> //
            extends MqttMessageWrapper<W, M, P> {

        private final int packetIdentifier;

        protected MqttMessageWrapperWithId(@NotNull final M wrapped, final int packetIdentifier) {
            super(wrapped);
            this.packetIdentifier = packetIdentifier;
        }

        public int getPacketIdentifier() {
            return packetIdentifier;
        }

    }

}
