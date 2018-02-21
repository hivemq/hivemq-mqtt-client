package org.mqttbee.mqtt.message;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.api.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;

import java.util.Optional;

/**
 * Base class for MQTT messages with optional User Properties.
 *
 * @param <M> the type of the MQTT message.
 * @param <P> the type of the encoder provider for the MQTT message.
 */
public abstract class MqttMessageWithUserProperties< //
        M extends MqttMessageWithUserProperties<M, P>, //
        P extends MqttMessageEncoderProvider<M>> //
        extends MqttMessage<M, P> {

    private final MqttUserPropertiesImpl userProperties;

    MqttMessageWithUserProperties(
            @NotNull final MqttUserPropertiesImpl userProperties, @Nullable final P encoderProvider) {

        super(encoderProvider);
        this.userProperties = userProperties;
    }

    @NotNull
    public MqttUserPropertiesImpl getUserProperties() {
        return userProperties;
    }


    /**
     * Base class for MQTT messages with an optional Reason String and optional User Properties.
     *
     * @param <M> the type of the MQTT message.
     * @param <P> the type of the encoder provider for the MQTT message.
     */
    public abstract static class MqttMessageWithReasonString< //
            M extends MqttMessageWithReasonString<M, P>, //
            P extends MqttMessageEncoderProvider<M>> //
            extends MqttMessageWithUserProperties<M, P> {

        private final MqttUTF8StringImpl reasonString;

        MqttMessageWithReasonString(
                @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties,
                @Nullable final P encoderProvider) {

            super(userProperties, encoderProvider);
            this.reasonString = reasonString;
        }

        @NotNull
        public Optional<Mqtt5UTF8String> getReasonString() {
            return Optional.ofNullable(reasonString);
        }

        @Nullable
        public MqttUTF8StringImpl getRawReasonString() {
            return reasonString;
        }

    }


    /**
     * Base class for MQTT messages with a Reason Code, an optional Reason String and optional User Properties.
     *
     * @param <M> the type of the MQTT message.
     * @param <R> the type of the Reason Code.
     * @param <P> the type of the encoder provider for the MQTT message.
     */
    public abstract static class MqttMessageWithReasonCode< //
            M extends MqttMessageWithReasonCode<M, R, P>, //
            R extends Mqtt5ReasonCode, //
            P extends MqttMessageEncoderProvider<M>> //
            extends MqttMessageWithReasonString<M, P> {

        private final R reasonCode;

        protected MqttMessageWithReasonCode(
                @NotNull final R reasonCode, @Nullable final MqttUTF8StringImpl reasonString,
                @NotNull final MqttUserPropertiesImpl userProperties, @Nullable final P encoderProvider) {

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
     * @param <M> the type of the MQTT message.
     * @param <R> the type of the Reason Code.
     * @param <P> the type of the encoder provider for the MQTT message.
     */
    public abstract static class MqttMessageWithIdAndReasonCode< //
            M extends MqttMessageWithIdAndReasonCode<M, R, P>, //
            R extends Mqtt5ReasonCode, //
            P extends MqttMessageEncoderProvider<M>> //
            extends MqttMessageWithReasonCode<M, R, P> {

        private final int packetIdentifier;

        protected MqttMessageWithIdAndReasonCode(
                final int packetIdentifier, @NotNull final R reasonCode,
                @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties,
                @Nullable final P encoderProvider) {

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
     * @param <M> the type of the MQTT message.
     * @param <R> the type of the Reason Codes.
     * @param <P> the type of the encoder provider for the MQTT message.
     */
    public abstract static class MqttMessageWithReasonCodes< //
            M extends MqttMessageWithReasonCodes<M, R, P>, //
            R extends Mqtt5ReasonCode, //
            P extends MqttMessageEncoderProvider<M>> //
            extends MqttMessageWithReasonString<M, P> {

        private final ImmutableList<R> reasonCodes;

        MqttMessageWithReasonCodes(
                @NotNull final ImmutableList<R> reasonCodes, @Nullable final MqttUTF8StringImpl reasonString,
                @NotNull final MqttUserPropertiesImpl userProperties, @Nullable final P encoderProvider) {

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
     * @param <M> the type of the MQTT message.
     * @param <R> the type of the Reason Codes.
     * @param <P> the type of the encoder provider for the MQTT message.
     */
    public abstract static class MqttMessageWithIdAndReasonCodes< //
            M extends MqttMessageWithReasonCodes<M, R, P>, //
            R extends Mqtt5ReasonCode, //
            P extends MqttMessageEncoderProvider<M>> //
            extends MqttMessageWithReasonCodes<M, R, P> {

        private final int packetIdentifier;

        protected MqttMessageWithIdAndReasonCodes(
                final int packetIdentifier, @NotNull final ImmutableList<R> reasonCodes,
                @Nullable final MqttUTF8StringImpl reasonString, @NotNull final MqttUserPropertiesImpl userProperties,
                @Nullable final P encoderProvider) {

            super(reasonCodes, reasonString, userProperties, encoderProvider);
            this.packetIdentifier = packetIdentifier;
        }

        public int getPacketIdentifier() {
            return packetIdentifier;
        }

    }

}
