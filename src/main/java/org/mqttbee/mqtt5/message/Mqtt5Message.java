package org.mqttbee.mqtt5.message;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt5Message<T extends Mqtt5Message<T>> {

    private final Function<T, ? extends Mqtt5MessageEncoder<T>> encoderProvider;
    private Mqtt5MessageEncoder<T> encoder;

    public Mqtt5Message(@Nullable final Function<T, ? extends Mqtt5MessageEncoder<T>> encoderProvider) {
        this.encoderProvider = encoderProvider;
    }

    @NotNull
    public Mqtt5MessageEncoder<T> getEncoder() {
        if (encoder != null) {
            return encoder;
        }
        if (encoderProvider != null) {
            return encoder = encoderProvider.apply(getCodable());
        }
        throw new UnsupportedOperationException();
    }

    protected abstract T getCodable();


    public static abstract class Mqtt5MessageWithUserProperties<T extends Mqtt5MessageWithUserProperties<T>>
            extends Mqtt5Message<T> {

        private final Mqtt5UserPropertiesImpl userProperties;

        public Mqtt5MessageWithUserProperties(
                @NotNull final Mqtt5UserPropertiesImpl userProperties,
                @Nullable final Function<T, ? extends Mqtt5MessageEncoder<T>> encoderProvider) {

            super(encoderProvider);
            this.userProperties = userProperties;
        }

        @NotNull
        public Mqtt5UserPropertiesImpl getUserProperties() {
            return userProperties;
        }

    }


    public static abstract class Mqtt5MessageWithReasonString<T extends Mqtt5MessageWithReasonString<T>>
            extends Mqtt5MessageWithUserProperties<T> {

        private final Mqtt5UTF8StringImpl reasonString;

        public Mqtt5MessageWithReasonString(
                @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
                @Nullable final Function<T, ? extends Mqtt5MessageEncoder<T>> encoderProvider) {

            super(userProperties, encoderProvider);
            this.reasonString = reasonString;
        }

        @NotNull
        public Optional<Mqtt5UTF8String> getReasonString() {
            return Optional.ofNullable(reasonString);
        }

        @Nullable
        public Mqtt5UTF8StringImpl getRawReasonString() {
            return reasonString;
        }

    }

}
