package org.mqttbee.mqtt5.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageWrapperEncoder;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageWrapperEncoder.Mqtt5WrappedMessageEncoder;

import java.util.function.Function;

import static org.mqttbee.mqtt5.message.Mqtt5MessageWrapper.Mqtt5WrappedMessage;

/**
 * Base class for wrappers around MQTT messages with additional state-specific data.
 *
 * @param <W> the type of the MQTT message wrapper.
 * @param <T> the type of the wrapped MQTT message.
 * @author Silvio Giebl
 */
public abstract class Mqtt5MessageWrapper<W extends Mqtt5MessageWrapper<W, T>, T extends Mqtt5WrappedMessage<T, W>>
        extends Mqtt5Message<W> {

    private final T wrapped;

    public Mqtt5MessageWrapper(
            @NotNull final T wrapped,
            @Nullable final Function<W, ? extends Mqtt5MessageWrapperEncoder<W>> encoderProvider) {

        super(encoderProvider);
        this.wrapped = wrapped;
    }

    /**
     * @return the wrapped MQTT message.
     */
    @NotNull
    public T getWrapped() {
        return wrapped;
    }


    /**
     * Base class for wrapped MQTT messages.
     *
     * @param <T> the type of the wrapped MQTT message.
     * @param <W> the type of the MQTT message wrapper.
     */
    public static abstract class Mqtt5WrappedMessage<T extends Mqtt5WrappedMessage<T, W>, W extends Mqtt5MessageWrapper<W, T>> {

        private final Mqtt5UserPropertiesImpl userProperties;
        private final Function<T, ? extends Mqtt5WrappedMessageEncoder<T, W>> encoderProvider;
        private Mqtt5WrappedMessageEncoder<T, W> encoder;

        public Mqtt5WrappedMessage(
                @NotNull final Mqtt5UserPropertiesImpl userProperties, @Nullable final Function<T, ? extends Mqtt5WrappedMessageEncoder<T, W>> encoderProvider) {

            this.userProperties = userProperties;
            this.encoderProvider = encoderProvider;
        }

        @NotNull
        public Mqtt5UserPropertiesImpl getUserProperties() {
            return userProperties;
        }

        /**
         * @return the encoder of this wrapped MQTT message.
         */
        @NotNull
        public Mqtt5WrappedMessageEncoder<T, W> getEncoder() {
            if (encoder != null) {
                return encoder;
            }
            if (encoderProvider != null) {
                return encoder = encoderProvider.apply(getCodable());
            }
            throw new UnsupportedOperationException();
        }

        /**
         * @return the codable object. This is usually the wrapped MQTT message object itself.
         */
        protected abstract T getCodable();

    }

}
