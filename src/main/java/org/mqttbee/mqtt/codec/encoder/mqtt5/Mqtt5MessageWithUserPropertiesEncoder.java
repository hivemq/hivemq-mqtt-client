/*
 * Copyright 2018 The MQTT Bee project
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
 *
 */

package org.mqttbee.mqtt.codec.encoder.mqtt5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.MqttMaximumPacketSizeExceededException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCode;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithReasonCode;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithReasonString;

import static org.mqttbee.mqtt.codec.encoder.MqttMessageEncoderUtil.encodedLengthWithHeader;
import static org.mqttbee.mqtt.codec.encoder.MqttMessageEncoderUtil.encodedPacketLength;
import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.encodeNullableProperty;
import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength;
import static org.mqttbee.mqtt.message.MqttProperty.REASON_STRING;

/**
 * Base class for encoders of MQTT messages with omissible User Properties.
 *
 * @author Silvio Giebl
 */
abstract class Mqtt5MessageWithUserPropertiesEncoder<M extends MqttMessage> extends MqttMessageEncoder<M> {

    @NotNull
    @Override
    protected ByteBuf encode(
            @NotNull final M message, @NotNull final ByteBufAllocator allocator, final int maximumPacketSize) {

        int propertyLength = calculatePropertyLength(message);
        final int remainingLengthWithoutProperties = calculateRemainingLength(message);
        int remainingLength = remainingLength(message, remainingLengthWithoutProperties, propertyLength);
        int encodedLength = encodedPacketLength(remainingLength);
        int omittedProperties = 0;
        while (encodedLength > maximumPacketSize) {
            omittedProperties++;
            propertyLength = propertyLength(message, propertyLength, omittedProperties);
            if (propertyLength < 0) {
                throw new MqttMaximumPacketSizeExceededException(message, encodedLength, maximumPacketSize);
            }
            remainingLength = remainingLength(message, remainingLengthWithoutProperties, propertyLength);
            encodedLength = encodedPacketLength(remainingLength);
        }
        return encode(message, allocator, encodedLength, remainingLength, propertyLength, omittedProperties);
    }

    @NotNull
    ByteBuf encode(
            @NotNull final M message, @NotNull final ByteBufAllocator allocator, final int encodedLength,
            final int remainingLength, final int propertyLength, final int omittedProperties) {

        final ByteBuf out = allocator.ioBuffer(encodedLength, encodedLength);
        encode(message, out, remainingLength, propertyLength, omittedProperties);
        return out;
    }

    abstract void encode(
            @NotNull final M message, @NotNull final ByteBuf out, final int remainingLength, final int propertyLength,
            final int omittedProperties);

    private int remainingLength(
            @NotNull final M message, final int remainingLengthWithoutProperties, final int propertyLength) {

        return remainingLengthWithoutProperties + encodedPropertyLengthWithHeader(message, propertyLength);
    }

    /**
     * Calculates the remaining length byte count without the properties of the MQTT message.
     *
     * @return the remaining length without the properties of the MQTT message.
     */
    abstract int calculateRemainingLength(@NotNull final M message);

    /**
     * Calculates the property length byte count of the MQTT message.
     *
     * @return the property length of the MQTT message.
     */
    abstract int calculatePropertyLength(@NotNull final M message);

    int propertyLength(@NotNull final M message, final int propertyLength, final int omittedProperties) {
        switch (omittedProperties) {
            case 0:
                return propertyLength;
            case 1:
                return propertyLength - getUserProperties(message).encodedLength();
            default:
                return -1;
        }
    }

    /**
     * Calculates the encoded property length with a prefixed header.
     *
     * @param propertyLength the encoded property length.
     * @return the encoded property length with a prefixed header.
     */
    int encodedPropertyLengthWithHeader(@NotNull final M message, final int propertyLength) {
        return encodedLengthWithHeader(propertyLength);
    }

    /**
     * @return the length of the omissible properties of the MQTT message.
     */
    int omissiblePropertiesLength(@NotNull final M message) {
        return getUserProperties(message).encodedLength();
    }

    /**
     * Encodes the omissible properties of the MQTT message if they must not be omitted due to the given maximum packet
     * size.
     *
     * @param out the byte buffer to encode to.
     */
    void encodeOmissibleProperties(@NotNull final M message, @NotNull final ByteBuf out, final int omittedProperties) {
        if (omittedProperties == 0) {
            getUserProperties(message).encode(out);
        }
    }

    abstract MqttUserPropertiesImpl getUserProperties(@NotNull final M message);


    /**
     * Base class for encoders of MQTT messages with an omissible Reason String and omissible User Properties.
     */
    static abstract class Mqtt5MessageWithReasonStringEncoder<M extends MqttMessageWithReasonString>
            extends Mqtt5MessageWithUserPropertiesEncoder<M> {

        @Override
        int propertyLength(@NotNull final M message, final int propertyLength, final int omittedProperties) {
            switch (omittedProperties) {
                case 0:
                    return propertyLength;
                case 1:
                    return propertyLength - reasonStringLength(message);
                case 2:
                    return propertyLength - getUserProperties(message).encodedLength();
                default:
                    return -1;
            }
        }

        @Override
        final int omissiblePropertiesLength(@NotNull final M message) {
            return reasonStringLength(message) + getUserProperties(message).encodedLength();
        }

        @Override
        void encodeOmissibleProperties(
                @NotNull final M message, @NotNull final ByteBuf out, final int omittedProperties) {

            if (omittedProperties == 0) {
                encodeNullableProperty(REASON_STRING, message.getRawReasonString(), out);
            }
            if (omittedProperties <= 1) {
                getUserProperties(message).encode(out);
            }
        }

        private int reasonStringLength(@NotNull final M message) {
            return nullablePropertyEncodedLength(message.getRawReasonString());
        }

        @Override
        MqttUserPropertiesImpl getUserProperties(@NotNull final M message) {
            return message.getUserProperties();
        }

    }


    /**
     * Base class for encoders of MQTT messages with an omissible Reason Code, an omissible Reason String and omissible
     * User Properties. The Reason Code is omitted if it is the default and the property length is 0.
     */
    static abstract class Mqtt5MessageWithOmissibleReasonCodeEncoder<M extends MqttMessageWithReasonCode<R>, R extends Mqtt5ReasonCode>
            extends Mqtt5MessageWithReasonStringEncoder<M> {

        abstract int getFixedHeader();

        abstract R getDefaultReasonCode();

        @Override
        final int calculateRemainingLength(@NotNull final M message) {
            return 1 + additionalRemainingLength(message); // reason code (1)
        }

        int additionalRemainingLength(@NotNull final M message) {
            return 0;
        }

        @Override
        final int calculatePropertyLength(@NotNull final M message) {
            return omissiblePropertiesLength(message) + additionalPropertyLength(message);
        }

        int additionalPropertyLength(@NotNull final M message) {
            return 0;
        }

        @Override
        protected void encode(
                @NotNull final M message, @NotNull final ByteBuf out, final int remainingLength,
                final int propertyLength, final int omittedProperties) {

            encodeFixedHeader(out, remainingLength);
            encodeVariableHeader(message, out, propertyLength, omittedProperties);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out, final int remainingLength) {
            out.writeByte(getFixedHeader());
            MqttVariableByteInteger.encode(remainingLength, out);
        }

        private void encodeVariableHeader(
                @NotNull final M message, @NotNull final ByteBuf out, final int propertyLength,
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

        void encodeAdditionalVariableHeader(@NotNull final M message, @NotNull final ByteBuf out) {
        }

        void encodeAdditionalProperties(@NotNull final M message, @NotNull final ByteBuf out) {
        }

        @Override
        final int encodedPropertyLengthWithHeader(@NotNull final M message, final int propertyLength) {
            if (propertyLength == 0) {
                return (message.getReasonCode() == getDefaultReasonCode()) ? -1 : 0;
            }
            return super.encodedPropertyLengthWithHeader(message, propertyLength);
        }

    }


    /**
     * Base class for encoders of MQTT messages with an Packet Identifier, an omissible Reason Code, an omissible Reason
     * String and omissible User Properties. The Reason Code is omitted if it is the default and the property length is
     * 0.
     */
    static abstract class Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<M extends MqttMessageWithIdAndReasonCode<R>, R extends Mqtt5ReasonCode>
            extends Mqtt5MessageWithOmissibleReasonCodeEncoder<M, R> {

        @Override
        int additionalRemainingLength(@NotNull final M message) {
            return 2; // packet identifier (2)
        }

        @Override
        void encodeAdditionalVariableHeader(@NotNull final M message, @NotNull final ByteBuf out) {
            out.writeShort(message.getPacketIdentifier());
        }

    }

}
