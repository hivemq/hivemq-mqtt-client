/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5;

import com.hivemq.client2.internal.mqtt.codec.encoder.MqttEncoderContext;
import com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoder;
import com.hivemq.client2.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client2.internal.mqtt.message.MqttMessage;
import com.hivemq.client2.internal.mqtt.message.MqttMessageWithUserProperties;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5ReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoderUtil.*;
import static com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.encodeNullableProperty;
import static com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength;
import static com.hivemq.client2.internal.mqtt.message.MqttProperty.REASON_STRING;

/**
 * Base class of encoders for MQTT messages with omissible User Properties.
 *
 * @author Silvio Giebl
 */
abstract class Mqtt5MessageWithUserPropertiesEncoder<M extends MqttMessage.WithUserProperties>
        extends MqttMessageEncoder<M> {

    @Override
    protected @NotNull ByteBuf encode(final @NotNull M message, final @NotNull MqttEncoderContext context) {
        int propertyLength = propertyLength(message);
        final int remainingLengthWithoutProperties = remainingLengthWithoutProperties(message);
        int remainingLength = remainingLength(message, remainingLengthWithoutProperties, propertyLength);
        int encodedLength = encodedPacketLength(remainingLength);
        int omittedProperties = 0;
        while (encodedLength > context.getMaximumPacketSize()) {
            omittedProperties++;
            propertyLength = propertyLength(message, propertyLength, omittedProperties);
            if (propertyLength < 0) {
                throw maximumPacketSizeExceeded(message, encodedLength, context.getMaximumPacketSize());
            }
            remainingLength = remainingLength(message, remainingLengthWithoutProperties, propertyLength);
            encodedLength = encodedPacketLength(remainingLength);
        }
        return encode(message, context, encodedLength, remainingLength, propertyLength, omittedProperties);
    }

    /**
     * Encodes the given MQTT message.
     *
     * @param message           the MQTT message to encode.
     * @param context           the encoder context.
     * @param encodedLength     the encoded length the MQTT message.
     * @param remainingLength   the remaining length of the MQTT message.
     * @param propertyLength    the property length of the MQTT message.
     * @param omittedProperties the count of omitted properties of the MQTT message.
     * @return the byte buffer the MQTT message is encoded to.
     */
    @NotNull ByteBuf encode(
            final @NotNull M message,
            final @NotNull MqttEncoderContext context,
            final int encodedLength,
            final int remainingLength,
            final int propertyLength,
            final int omittedProperties) {

        final ByteBuf out = context.getAllocator().ioBuffer(encodedLength, encodedLength);
        encode(message, out, remainingLength, propertyLength, omittedProperties);
        return out;
    }

    /**
     * Encodes the given MQTT message.
     *
     * @param message           the MQTT message to encode.
     * @param out               the byte buffer the MQTT message is encoded to.
     * @param remainingLength   the remaining length of the MQTT message.
     * @param propertyLength    the property length of the MQTT message.
     * @param omittedProperties the count of omitted properties of the MQTT message.
     */
    abstract void encode(
            final @NotNull M message,
            final @NotNull ByteBuf out,
            final int remainingLength,
            final int propertyLength,
            final int omittedProperties);

    /**
     * Calculates the remaining length of the given MQTT message.
     *
     * @param message                          the MQTT message to encode.
     * @param remainingLengthWithoutProperties the already calculated remaining length without the properties.
     * @param propertyLength                   the already calculated property length.
     * @return the remaining length of the MQTT message.
     */
    private int remainingLength(
            final @NotNull M message, final int remainingLengthWithoutProperties, final int propertyLength) {

        return remainingLengthWithoutProperties + propertyLengthWithHeader(message, propertyLength);
    }

    /**
     * Calculates the remaining length without the properties of the given MQTT message.
     *
     * @param message the MQTT message to encode.
     * @return the remaining length without the properties of the MQTT message.
     */
    abstract int remainingLengthWithoutProperties(final @NotNull M message);

    /**
     * Calculates the property length of the given MQTT message.
     *
     * @param message the MQTT message to encode.
     * @return the property length of the MQTT message.
     */
    abstract int propertyLength(final @NotNull M message);

    /**
     * Calculates the property length of the given MQTT message with omitting the given count of properties.
     *
     * @param message           the MQTT message to encode.
     * @param propertyLength    the already calculated property length with a count of omitted properties of {@code
     *                          omittedProperties - 1}.
     * @param omittedProperties the count of omitted properties.
     * @return the property length of the MQTT message with omitting the given count of properties or -1 if no more
     *         properties can be omitted.
     */
    int propertyLength(final @NotNull M message, final int propertyLength, final int omittedProperties) {
        switch (omittedProperties) {
            case 0:
                return propertyLength;
            case 1:
                return propertyLength - message.getUserProperties().encodedLength();
            default:
                return -1;
        }
    }

    /**
     * Calculates the encoded length of the properties of the given MQTT message with a prefixed header.
     *
     * @param message        the MQTT message to encode.
     * @param propertyLength the already calculated property length.
     * @return the encoded length of the properties of the MQTT message with a prefixed header.
     */
    int propertyLengthWithHeader(final @NotNull M message, final int propertyLength) {
        return encodedLengthWithHeader(propertyLength);
    }

    /**
     * Calculates the encoded length of omissible properties of the given MQTT message.
     *
     * @param message the MQTT message to encode.
     * @return the encoded length of the omissible properties of the MQTT message.
     */
    int omissiblePropertyLength(final @NotNull M message) {
        return message.getUserProperties().encodedLength();
    }

    /**
     * Encodes the omissible properties of the given MQTT message if they must not be omitted due to the given count of
     * omitted properties.
     *
     * @param message           the MQTT message to encode.
     * @param out               the byte buffer to encode to.
     * @param omittedProperties the count of properties of the MQTT message.
     */
    void encodeOmissibleProperties(final @NotNull M message, final @NotNull ByteBuf out, final int omittedProperties) {
        if (omittedProperties == 0) {
            message.getUserProperties().encode(out);
        }
    }

    /**
     * Base class of encoders for MQTT messages with an omissible Reason String and omissible User Properties.
     */
    static abstract class WithReason<M extends MqttMessageWithUserProperties.WithReason>
            extends Mqtt5MessageWithUserPropertiesEncoder<M> {

        @Override
        int propertyLength(final @NotNull M message, final int propertyLength, final int omittedProperties) {
            switch (omittedProperties) {
                case 0:
                    return propertyLength;
                case 1:
                    return propertyLength - reasonStringLength(message);
                case 2:
                    return propertyLength - message.getUserProperties().encodedLength();
                default:
                    return -1;
            }
        }

        @Override
        final int omissiblePropertyLength(final @NotNull M message) {
            return reasonStringLength(message) + message.getUserProperties().encodedLength();
        }

        @Override
        void encodeOmissibleProperties(
                final @NotNull M message, final @NotNull ByteBuf out, final int omittedProperties) {

            if (omittedProperties == 0) {
                encodeNullableProperty(REASON_STRING, message.getRawReasonString(), out);
            }
            if (omittedProperties <= 1) {
                message.getUserProperties().encode(out);
            }
        }

        private int reasonStringLength(final @NotNull M message) {
            return nullablePropertyEncodedLength(message.getRawReasonString());
        }

        /**
         * Base class of encoders for MQTT messages with an omissible Reason Code, an omissible Reason String and
         * omissible User Properties. The Reason Code is omitted if it is the default and the property length is 0.
         */
        static abstract class WithOmissibleCode< //
                M extends MqttMessageWithUserProperties.WithReason.WithCode<R>, R extends Mqtt5ReasonCode>
                extends WithReason<M> {

            abstract int getFixedHeader();

            abstract @NotNull R getDefaultReasonCode();

            @Override
            final int remainingLengthWithoutProperties(final @NotNull M message) {
                return 1 + additionalRemainingLength(message); // reason code (1)
            }

            int additionalRemainingLength(final @NotNull M message) {
                return 0;
            }

            @Override
            final int propertyLength(final @NotNull M message) {
                return omissiblePropertyLength(message) + additionalPropertyLength(message);
            }

            int additionalPropertyLength(final @NotNull M message) {
                return 0;
            }

            @Override
            protected void encode(
                    final @NotNull M message,
                    final @NotNull ByteBuf out,
                    final int remainingLength,
                    final int propertyLength,
                    final int omittedProperties) {

                encodeFixedHeader(out, remainingLength);
                encodeVariableHeader(message, out, propertyLength, omittedProperties);
            }

            private void encodeFixedHeader(final @NotNull ByteBuf out, final int remainingLength) {
                out.writeByte(getFixedHeader());
                MqttVariableByteInteger.encode(remainingLength, out);
            }

            private void encodeVariableHeader(
                    final @NotNull M message,
                    final @NotNull ByteBuf out,
                    final int propertyLength,
                    final int omittedProperties) {

                encodeAdditionalVariableHeader(message, out);
                final R reasonCode = message.getReasonCode();
                if (propertyLength == 0) {
                    if (reasonCode != getDefaultReasonCode()) {
                        out.writeByte(reasonCode.getCode());
                    }
                } else {
                    out.writeByte(reasonCode.getCode());
                    MqttVariableByteInteger.encode(propertyLength, out);
                    encodeAdditionalProperties(message, out);
                    encodeOmissibleProperties(message, out, omittedProperties);
                }
            }

            void encodeAdditionalVariableHeader(final @NotNull M message, final @NotNull ByteBuf out) {}

            void encodeAdditionalProperties(final @NotNull M message, final @NotNull ByteBuf out) {}

            @Override
            final int propertyLengthWithHeader(final @NotNull M message, final int propertyLength) {
                if (propertyLength == 0) {
                    return (message.getReasonCode() == getDefaultReasonCode()) ? -1 : 0;
                }
                return super.propertyLengthWithHeader(message, propertyLength);
            }

            /**
             * Base class of encoders for MQTT messages with a Packet Identifier, an omissible Reason Code, an omissible
             * Reason String and omissible User Properties. The Reason Code is omitted if it is the default and the
             * property length is 0.
             */
            static abstract class WithId< //
                    M extends MqttMessageWithUserProperties.WithReason.WithCode.WithId<R>, R extends Mqtt5ReasonCode>
                    extends WithOmissibleCode<M, R> {

                @Override
                int additionalRemainingLength(final @NotNull M message) {
                    return 2; // packet identifier (2)
                }

                @Override
                void encodeAdditionalVariableHeader(final @NotNull M message, final @NotNull ByteBuf out) {
                    out.writeShort(message.getPacketIdentifier());
                }
            }
        }
    }
}
