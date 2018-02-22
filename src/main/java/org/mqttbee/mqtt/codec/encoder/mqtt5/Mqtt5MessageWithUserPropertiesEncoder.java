package org.mqttbee.mqtt.codec.encoder.mqtt5;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoderWithMessage;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithIdAndReasonCode;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithReasonCode;
import org.mqttbee.mqtt.message.MqttMessageWithUserProperties.MqttMessageWithReasonString;
import org.mqttbee.mqtt5.Mqtt5ServerConnectionDataImpl;

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
abstract class Mqtt5MessageWithUserPropertiesEncoder<M extends MqttMessage> extends MqttMessageEncoderWithMessage<M> {

    private int encodedLength = -1;
    private int remainingLength = -1;
    private int propertyLength = -1;

    @NotNull
    @Override
    public MqttMessageEncoder apply(@NotNull final M message) {
        if (this.message != message) {
            encodedLength = remainingLength = propertyLength = -1;
        }
        return super.apply(message);
    }

    @Override
    public final int encodedLength(final int maxPacketSize) {
        int encodedLength = encodedLength();
        if (encodedLength <= maxPacketSize) {
            return encodedLength;
        }
        encodedLength = encodedLengthFromOmittedProperties(omittedPropertiesLength(maxPacketSize));
        if (encodedLength <= maxPacketSize) {
            return encodedLength;
        }
        return -1;
    }

    /**
     * Returns the byte count of the MQTT message without omitting any properties. Calculation is only performed if
     * necessary.
     *
     * @return the encoded length of this MQTT message without omitting any properties.
     */
    private int encodedLength() {
        if (encodedLength == -1) {
            encodedLength = encodedPacketLength(remainingLength());
        }
        return encodedLength;
    }

    /**
     * Calculates the byte count of the MQTT message if properties with the given length are omitted.
     *
     * @param omittedPropertiesLength the length of the omitted properties.
     * @return the encoded length of the MQTT message if properties with the given length are omitted.
     */
    final int encodedLengthFromOmittedProperties(final int omittedPropertiesLength) {
        return encodedPacketLength(remainingLengthFromOmittedProperties(omittedPropertiesLength));
    }

    /**
     * Returns the remaining length byte count of this MQTT message respecting the given maximum packet size.
     * <p>
     * If the minimal encoded length of the MQTT message is bigger than the maximum packet size, the returned value is
     * unspecified.
     *
     * @param maxPacketSize the maximum packet size.
     * @return the remaining length of the MQTT message respecting the maximum packet size.
     */
    final int remainingLength(final int maxPacketSize) {
        return (mustOmitProperties(maxPacketSize)) ?
                remainingLengthFromOmittedProperties(omittedPropertiesLength(maxPacketSize)) : remainingLength();
    }

    /**
     * Returns the remaining length byte count of the MQTT message without omitting any properties. Calculation is only
     * performed if necessary.
     *
     * @return the remaining length of the MQTT message without omitting any properties.
     */
    private int remainingLength() {
        if (remainingLength == -1) {
            remainingLength = calculateRemainingLength() + encodedPropertyLengthWithHeader(propertyLength());
        }
        return remainingLength;
    }

    /**
     * Calculates the remaining length byte count without the properties of the MQTT message.
     *
     * @return the remaining length without the properties of the MQTT message.
     */
    abstract int calculateRemainingLength();

    /**
     * Calculates the remaining length byte count of the MQTT message if properties with the given length are omitted.
     *
     * @param omittedPropertiesLength the length of the omitted properties.
     * @return the remaining length of the MQTT message if properties with the given length are omitted.
     */
    private int remainingLengthFromOmittedProperties(final int omittedPropertiesLength) {
        return remainingLength() - encodedPropertyLengthWithHeader(propertyLength()) +
                encodedPropertyLengthWithHeader(propertyLengthFromOmittedProperties(omittedPropertiesLength));
    }

    /**
     * Returns the property length byte count of the MQTT message respecting the given maximum packet size.
     * <p>
     * If the minimal encoded length of the MQTT message is bigger than the maximum packet, size the returned value is
     * unspecified.
     *
     * @param maxPacketSize the maximum packet size.
     * @return the property length of the MQTT message respecting the maximum packet size.
     */
    final int propertyLength(final int maxPacketSize) {
        return (mustOmitProperties(maxPacketSize)) ?
                propertyLengthFromOmittedProperties(omittedPropertiesLength(maxPacketSize)) : propertyLength();
    }

    /**
     * Returns the property length byte count of the MQTT message without omitting any properties. Calculation is only
     * performed if necessary.
     *
     * @return the property length of the MQTT message without omitting any properties.
     */
    private int propertyLength() {
        if (propertyLength == -1) {
            propertyLength = calculatePropertyLength();
        }
        return propertyLength;
    }

    /**
     * Calculates the property length byte count of the MQTT message.
     *
     * @return the property length of the MQTT message.
     */
    abstract int calculatePropertyLength();

    /**
     * Calculates the property length byte count of the MQTT message if properties with the given length are omitted.
     *
     * @param omittedPropertiesLength the length of the omitted properties.
     * @return the property length of the MQTT message if properties with the given length are omitted.
     */
    private int propertyLengthFromOmittedProperties(final int omittedPropertiesLength) {
        return propertyLength() - omittedPropertiesLength;
    }

    /**
     * Checks whether properties of the MQTT message must be omitted to fit the given maximum packet size.
     *
     * @param maxPacketSize the maximum packet size.
     * @return whether properties of this MQTT message must be omitted to fit the maximum packet size.
     */
    final boolean mustOmitProperties(final int maxPacketSize) {
        return encodedLength() > maxPacketSize;
    }

    /**
     * Calculates the length of properties which must be omitted to fit the given maximum packet size.
     * <p>
     * If the minimal encoded length of the MQTT message is bigger than the maximum packet size, the length of all
     * omissible properties is returned.
     *
     * @param maxPacketSize the maximum packet size.
     * @return the length of properties which must be omitted to fit the maximum packet size.
     */
    int omittedPropertiesLength(final int maxPacketSize) {
        return userPropertiesLength();
    }

    /**
     * Calculates the encoded property length with a prefixed header.
     *
     * @param propertyLength the encoded property length.
     * @return the encoded property length with a prefixed header.
     */
    int encodedPropertyLengthWithHeader(final int propertyLength) {
        return encodedLengthWithHeader(propertyLength);
    }

    /**
     * @return the length of the omissible properties of the MQTT message.
     */
    int omissiblePropertiesLength() {
        return userPropertiesLength();
    }

    /**
     * Encodes the omissible properties of the MQTT message if they must not be omitted due to the given maximum packet
     * size.
     *
     * @param maxPacketSize the maximum packet size.
     * @param out           the byte buffer to encode to.
     */
    void encodeOmissibleProperties(final int maxPacketSize, @NotNull final ByteBuf out) {
        if (!mustOmitProperties(maxPacketSize)) {
            getUserProperties().encode(out);
        }
    }

    final int userPropertiesLength() {
        return getUserProperties().encodedLength();
    }

    abstract MqttUserPropertiesImpl getUserProperties();


    /**
     * Base class for encoders of MQTT messages with an omissible Reason String and omissible User Properties.
     */
    static abstract class Mqtt5MessageWithReasonStringEncoder<T extends MqttMessageWithReasonString<T, P>, P extends MqttMessageEncoderProvider<T>>
            extends Mqtt5MessageWithUserPropertiesEncoder<T> {

        @Override
        final int omittedPropertiesLength(final int maxPacketSize) {
            final int reasonStringLength = reasonStringLength();
            if (encodedLengthFromOmittedProperties(reasonStringLength) <= maxPacketSize) {
                return reasonStringLength;
            }
            return reasonStringLength + userPropertiesLength();
        }

        @Override
        final int omissiblePropertiesLength() {
            return reasonStringLength() + userPropertiesLength();
        }

        @Override
        final void encodeOmissibleProperties(final int maxPacketSize, @NotNull final ByteBuf out) {
            if (!mustOmitProperties(maxPacketSize)) {
                encodeNullableProperty(REASON_STRING, message.getRawReasonString(), out);
                getUserProperties().encode(out);
            } else if (encodedLengthFromOmittedProperties(reasonStringLength()) <= maxPacketSize) {
                getUserProperties().encode(out);
            }
        }

        private int reasonStringLength() {
            return nullablePropertyEncodedLength(message.getRawReasonString());
        }

        @Override
        MqttUserPropertiesImpl getUserProperties() {
            return message.getUserProperties();
        }

    }


    /**
     * Base class for encoders of MQTT messages with an omissible Reason Code, an omissible Reason String and omissible
     * User Properties. The Reason Code is omitted if it is the default and the property length is 0.
     */
    static abstract class Mqtt5MessageWithOmissibleReasonCodeEncoder<T extends MqttMessageWithReasonCode<T, R, P>, R extends Mqtt5ReasonCode, P extends MqttMessageEncoderProvider<T>>
            extends Mqtt5MessageWithReasonStringEncoder<T, P> {

        abstract int getFixedHeader();

        abstract R getDefaultReasonCode();

        @Override
        final int calculateRemainingLength() {
            return 1 + additionalRemainingLength(); // reason code (1)
        }

        int additionalRemainingLength() {
            return 0;
        }

        @Override
        final int calculatePropertyLength() {
            return omissiblePropertiesLength() + additionalPropertyLength();
        }

        int additionalPropertyLength() {
            return 0;
        }

        @Override
        public final void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
            final int maximumPacketSize = Mqtt5ServerConnectionDataImpl.getMaximumPacketSize(channel);

            encodeFixedHeader(out, maximumPacketSize);
            encodeVariableHeader(out, maximumPacketSize);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            out.writeByte(getFixedHeader());
            MqttVariableByteInteger.encode(remainingLength(maximumPacketSize), out);
        }

        private void encodeVariableHeader(@NotNull final ByteBuf out, final int maximumPacketSize) {
            encodeAdditionalVariableHeader(out);
            final R reasonCode = message.getReasonCode();
            final int propertyLength = propertyLength(maximumPacketSize);
            if (propertyLength == 0) {
                if (reasonCode != getDefaultReasonCode()) {
                    out.writeByte(reasonCode.getCode());
                }
            } else {
                out.writeByte(reasonCode.getCode());
                MqttVariableByteInteger.encode(propertyLength, out);
                encodeAdditionalProperties(out);
                encodeOmissibleProperties(maximumPacketSize, out);
            }
        }

        void encodeAdditionalVariableHeader(@NotNull final ByteBuf out) {
        }

        void encodeAdditionalProperties(@NotNull final ByteBuf out) {
        }

        @Override
        final int encodedPropertyLengthWithHeader(final int propertyLength) {
            if (propertyLength == 0) {
                if (message.getReasonCode() == getDefaultReasonCode()) {
                    return -1;
                }
                return 0;
            }
            return super.encodedPropertyLengthWithHeader(propertyLength);
        }

    }


    /**
     * Base class for encoders of MQTT messages with an Packet Identifier, an omissible Reason Code, an omissible Reason
     * String and omissible User Properties. The Reason Code is omitted if it is the default and the property length is
     * 0.
     */
    static abstract class Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<T extends MqttMessageWithIdAndReasonCode<T, R, P>, R extends Mqtt5ReasonCode, P extends MqttMessageEncoderProvider<T>>
            extends Mqtt5MessageWithOmissibleReasonCodeEncoder<T, R, P> {

        @Override
        int additionalRemainingLength() {
            return 2; // packet identifier (2)
        }

        @Override
        void encodeAdditionalVariableHeader(@NotNull final ByteBuf out) {
            out.writeShort(message.getPacketIdentifier());
        }

    }

}
