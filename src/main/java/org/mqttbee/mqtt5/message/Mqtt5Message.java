package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;

import java.util.Optional;
import java.util.function.Function;

/**
 * Base class for MQTT messages.
 *
 * @param <T> the type of the codable MQTT message. This is usually the MQTT message type itself.
 * @author Silvio Giebl
 */
public abstract class Mqtt5Message<T extends Mqtt5Message<T>> {

    private final Function<T, ? extends Mqtt5MessageEncoder<T>> encoderProvider;
    private Mqtt5MessageEncoder<T> encoder;

    public Mqtt5Message(@Nullable final Function<T, ? extends Mqtt5MessageEncoder<T>> encoderProvider) {
        this.encoderProvider = encoderProvider;
    }

    /**
     * @return the encoder of this MQTT message.
     */
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

    /**
     * @return the codable object. This is usually the MQTT message object itself.
     */
    protected abstract T getCodable();


    /**
     * Base class for MQTT messages with optional User Properties.
     *
     * @param <T> the type of the codable MQTT message. This is usually the MQTT message type itself.
     */
    public static abstract class Mqtt5MessageWithUserProperties<T extends Mqtt5MessageWithUserProperties<T>>
            extends Mqtt5Message<T> {

        private final Mqtt5UserPropertiesImpl userProperties;

        Mqtt5MessageWithUserProperties(
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


    /**
     * Base class for MQTT messages with an optional Reason String and optional User Properties.
     *
     * @param <T> the type of the codable MQTT message. This is usually the MQTT message type itself.
     */
    public static abstract class Mqtt5MessageWithReasonString<T extends Mqtt5MessageWithReasonString<T>>
            extends Mqtt5MessageWithUserProperties<T> {

        private final Mqtt5UTF8StringImpl reasonString;

        Mqtt5MessageWithReasonString(
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


    /**
     * Base class for MQTT messages with a Reason Code, an optional Reason String and optional User Properties.
     *
     * @param <T> the type of the codable MQTT message. This is usually the MQTT message type itself.
     * @param <R> the type of the Reason Code.
     */
    public static abstract class Mqtt5MessageWithReasonCode<T extends Mqtt5MessageWithReasonCode<T, R>, R extends Mqtt5ReasonCode>
            extends Mqtt5MessageWithReasonString<T> {

        private final R reasonCode;

        public Mqtt5MessageWithReasonCode(
                @NotNull final R reasonCode, @Nullable final Mqtt5UTF8StringImpl reasonString,
                @NotNull final Mqtt5UserPropertiesImpl userProperties,
                @Nullable final Function<T, ? extends Mqtt5MessageEncoder<T>> encoderProvider) {

            super(reasonString, userProperties, encoderProvider);
            this.reasonCode = reasonCode;
        }

        @NotNull
        public R getReasonCode() {
            return reasonCode;
        }

    }


    /**
     * Base class for MQTT messages with a Packet Identifier, a Reason Code, an optional Reason String and optional User
     * Properties.
     *
     * @param <T> the type of the codable MQTT message. This is usually the MQTT message type itself.
     * @param <R> the type of the Reason Code.
     */
    public static abstract class Mqtt5MessageWithIdAndReasonCode<T extends Mqtt5MessageWithIdAndReasonCode<T, R>, R extends Mqtt5ReasonCode>
            extends Mqtt5MessageWithReasonCode<T, R> {

        private final int packetIdentifier;

        public Mqtt5MessageWithIdAndReasonCode(
                final int packetIdentifier, @NotNull final R reasonCode,
                @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
                @Nullable final Function<T, ? extends Mqtt5MessageEncoder<T>> encoderProvider) {

            super(reasonCode, reasonString, userProperties, encoderProvider);
            this.packetIdentifier = packetIdentifier;
        }

        public int getPacketIdentifier() {
            return packetIdentifier;
        }

    }


    /**
     * Base class for MQTT messages with a Reason Codes, an optional Reason String and optional User Properties.
     *
     * @param <T> the type of the codable MQTT message. This is usually the MQTT message type itself.
     * @param <R> the type of the Reason Codes.
     */
    public static abstract class Mqtt5MessageWithReasonCodes<T extends Mqtt5MessageWithReasonCodes<T, R>, R extends Mqtt5ReasonCode>
            extends Mqtt5MessageWithReasonString<T> {

        private final ImmutableList<R> reasonCodes;

        Mqtt5MessageWithReasonCodes(
                @NotNull final ImmutableList<R> reasonCodes, @Nullable final Mqtt5UTF8StringImpl reasonString,
                @NotNull final Mqtt5UserPropertiesImpl userProperties,
                @Nullable final Function<T, ? extends Mqtt5MessageEncoder<T>> encoderProvider) {

            super(reasonString, userProperties, encoderProvider);
            this.reasonCodes = reasonCodes;
        }

        @NotNull
        public ImmutableList<R> getReasonCodes() {
            return reasonCodes;
        }

    }


    /**
     * Base class for MQTT messages with a Packet Identifier, Reason Codes, an optional Reason String and optional User
     * Properties.
     *
     * @param <T> the type of the codable MQTT message. This is usually the MQTT message type itself.
     * @param <R> the type of the Reason Codes.
     */
    public static abstract class Mqtt5MessageWithIdAndReasonCodes<T extends Mqtt5MessageWithReasonCodes<T, R>, R extends Mqtt5ReasonCode>
            extends Mqtt5MessageWithReasonCodes<T, R> {

        private final int packetIdentifier;

        public Mqtt5MessageWithIdAndReasonCodes(
                final int packetIdentifier, @NotNull final ImmutableList<R> reasonCodes,
                @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
                @Nullable final Function<T, ? extends Mqtt5MessageEncoder<T>> encoderProvider) {

            super(reasonCodes, reasonString, userProperties, encoderProvider);
            this.packetIdentifier = packetIdentifier;
        }

        public int getPacketIdentifier() {
            return packetIdentifier;
        }

    }

}
