package org.mqttbee.mqtt5.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5MessageWrapperEncoder.Mqtt5WrappedMessageEncoder;

import java.util.function.Function;

import static org.mqttbee.mqtt5.message.Mqtt5MessageWrapper.Mqtt5WrappedMessage;

/**
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

    @NotNull
    public T getWrapped() {
        return wrapped;
    }


    public static abstract class Mqtt5WrappedMessage<T extends Mqtt5WrappedMessage<T, W>, W extends Mqtt5MessageWrapper<W, T>> {

        private final Mqtt5UserPropertiesImpl userProperties;
        private final Function<T, ? extends Mqtt5WrappedMessageEncoder<T, W>> encoderProvider;
        private Mqtt5WrappedMessageEncoder<T, W> encoder;

        public Mqtt5WrappedMessage(
                @NotNull final Mqtt5UserPropertiesImpl userProperties,
                @Nullable final Function<T, ? extends Mqtt5WrappedMessageEncoder<T, W>> encoderProvider) {

            this.userProperties = userProperties;
            this.encoderProvider = encoderProvider;
        }

        @NotNull
        public Mqtt5UserPropertiesImpl getUserProperties() {
            return userProperties;
        }

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

        protected abstract T getCodable();

    }

}
