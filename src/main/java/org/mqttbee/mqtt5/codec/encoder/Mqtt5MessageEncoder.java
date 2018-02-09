package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.exceptions.Mqtt5MaximumPacketSizeExceededException;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

import static org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithReasonString;
import static org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithUserProperties;
import static org.mqttbee.mqtt5.message.Mqtt5Property.REASON_STRING;

/**
 * Base class for encoders of MQTT messages.
 *
 * @author Silvio Giebl
 */
public abstract class Mqtt5MessageEncoder<T extends Mqtt5Message> {

    protected final T message;

    public Mqtt5MessageEncoder(@NotNull final T message) {
        this.message = message;
    }

    /**
     * Encodes the MQTT message of this encoder.
     *
     * @param channel the channel where the given byte buffer will be written to.
     * @param out     the byte buffer to encode to.
     */
    public abstract void encode(@NotNull Channel channel, @NotNull ByteBuf out);

    /**
     * Creates a byte buffer with the correct size for the MQTT message of this encoder.
     *
     * @param channel the channel where the allocated byte buffer will be written to.
     * @return the allocated byte buffer.
     */
    @NotNull
    public ByteBuf allocateBuffer(@NotNull final Channel channel) {
        final int maximumPacketSize = Mqtt5ServerData.getMaximumPacketSize(channel);
        final int encodedLength = encodedLength(maximumPacketSize);
        if (encodedLength < 0) {
            throw new Mqtt5MaximumPacketSizeExceededException(message, maximumPacketSize);
        }
        return channel.alloc().ioBuffer(encodedLength, encodedLength);
    }

    /**
     * Returns the byte count of the MQTT message of this encoder respecting the given maximum packet size. Calculation
     * is only performed if necessary.
     *
     * @param maxPacketSize the maximum packet size.
     * @return the encoded length of the MQTT message respecting the maximum packet size or -1 if the minimal encoded
     * length of the MQTT message is bigger than the maximum packet size.
     */
    public abstract int encodedLength(final int maxPacketSize);


    /**
     * Base class for encoders of MQTT messages with omissible User Properties.
     */
    static abstract class Mqtt5MessageWithPropertiesEncoder<T extends Mqtt5Message> extends Mqtt5MessageEncoder<T> {

        private int encodedLength = -1;
        private int remainingLength = -1;
        private int propertyLength = -1;

        Mqtt5MessageWithPropertiesEncoder(@NotNull final T message) {
            super(message);
        }

        @Override
        public final int encodedLength(final int maxPacketSize) {
            int encodedLength = maxEncodedLength();
            if (encodedLength <= maxPacketSize) {
                return encodedLength;
            }
            encodedLength = minEncodedLength();
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
        private int maxEncodedLength() {
            if (encodedLength == -1) {
                encodedLength = Mqtt5MessageEncoderUtil.encodedPacketLength(maxEncodedRemainingLength());
            }
            return encodedLength;
        }

        /**
         * Returns the minimal byte count of the MQTT message. All properties which can be omitted are omitted.
         *
         * @return the minimal encoded length of the MQTT message.
         */
        private int minEncodedLength() {
            return Mqtt5MessageEncoderUtil.encodedPacketLength(minEncodedRemainingLength());
        }

        /**
         * Returns the remaining length byte count of this MQTT message respecting the given maximum packet size.
         * <p>
         * If the minimal encoded length of the MQTT message is bigger than the given maximum packet size the returned
         * value is unspecified.
         *
         * @param maxPacketSize the maximum packet size.
         * @return the encoded remaining length of the MQTT message respecting the maximum packet size.
         */
        final int encodedRemainingLength(final int maxPacketSize) {
            return mustOmitProperties(maxPacketSize) ? minEncodedRemainingLength() : maxEncodedRemainingLength();
        }

        /**
         * Returns the remaining length byte count of the MQTT message without omitting any properties. Calculation is
         * only performed if necessary.
         *
         * @return the encoded remaining length of the MQTT message without omitting any properties.
         */
        private int maxEncodedRemainingLength() {
            if (remainingLength == -1) {
                remainingLength =
                        calculateEncodedRemainingLength() + encodedPropertyLengthWithHeader(maxEncodedPropertyLength());
            }
            return remainingLength;
        }

        /**
         * Returns the minimal remaining length byte count of the MQTT message. All properties which can be omitted are
         * omitted.
         *
         * @return the minimal encoded remaining length of the MQTT message.
         */
        private int minEncodedRemainingLength() {
            return maxEncodedRemainingLength() - encodedPropertyLengthWithHeader(maxEncodedPropertyLength()) +
                    encodedPropertyLengthWithHeader(minEncodedPropertyLength());
        }

        /**
         * Calculates the remaining length byte count without the properties of the MQTT message.
         *
         * @return the encoded remaining length without the properties of the MQTT message.
         */
        abstract int calculateEncodedRemainingLength();

        /**
         * Returns the property length byte count of the MQTT message respecting the given maximum packet size.
         * <p>
         * If the minimal encoded length of the MQTT message is bigger than the given maximum packet size the returned
         * value is unspecified.
         *
         * @param maxPacketSize the maximum packet size.
         * @return the encoded property length of the MQTT message respecting the maximum packet size.
         */
        final int encodedPropertyLength(final int maxPacketSize) {
            return mustOmitProperties(maxPacketSize) ? minEncodedPropertyLength() : maxEncodedPropertyLength();
        }

        /**
         * Returns the property length byte count of the MQTT message without omitting any properties. Calculation is
         * only performed if necessary.
         *
         * @return the encoded property length of the MQTT message without omitting any properties.
         */
        private int maxEncodedPropertyLength() {
            if (propertyLength == -1) {
                propertyLength = calculateEncodedPropertyLength();
            }
            return propertyLength;
        }

        /**
         * Returns the minimal property length byte count of the MQTT message. All properties which can be omitted are
         * omitted.
         *
         * @return the minimal encoded property length of the MQTT message.
         */
        int minEncodedPropertyLength() {
            return maxEncodedPropertyLength() - getUserProperties().encodedLength();
        }

        /**
         * Calculates the property length byte count of the MQTT message.
         *
         * @return the encoded property length of the MQTT message.
         */
        abstract int calculateEncodedPropertyLength();

        /**
         * Checks whether properties of the MQTT message must be omitted to fit the given maximum packet size.
         *
         * @param maxPacketSize the maximum packet size.
         * @return whether properties of this MQTT message must be omitted.
         */
        final boolean mustOmitProperties(final int maxPacketSize) {
            return maxEncodedLength() > maxPacketSize;
        }

        /**
         * Calculates the encoded property length with a prefixed header.
         *
         * @param propertyLength the encoded property length.
         * @return the encoded property length with a prefixed header.
         */
        int encodedPropertyLengthWithHeader(final int propertyLength) {
            return Mqtt5MessageEncoderUtil.encodedLengthWithHeader(propertyLength);
        }

        abstract Mqtt5UserPropertiesImpl getUserProperties();

        /**
         * Encodes the user properties of the MQTT message if they must not be omitted due to the given maximum packet
         * size.
         *
         * @param maxPacketSize the maximum packet size.
         * @param out           the byte buffer to encode to.
         */
        final void encodeUserProperties(final int maxPacketSize, @NotNull final ByteBuf out) {
            if (!mustOmitProperties(maxPacketSize)) {
                getUserProperties().encode(out);
            }
        }

    }


    /**
     * Base class for encoders of MQTT messages with omissible User Properties.
     */
    static abstract class Mqtt5MessageWithUserPropertiesEncoder<T extends Mqtt5MessageWithUserProperties>
            extends Mqtt5MessageWithPropertiesEncoder<T> {

        Mqtt5MessageWithUserPropertiesEncoder(@NotNull final T message) {
            super(message);
        }

        @Override
        Mqtt5UserPropertiesImpl getUserProperties() {
            return message.getUserProperties();
        }

    }


    /**
     * Base class for encoders of MQTT messages with an omissible Reason String and omissible User Properties.
     */
    static abstract class Mqtt5MessageWithReasonStringEncoder<T extends Mqtt5MessageWithReasonString>
            extends Mqtt5MessageWithUserPropertiesEncoder<T> {

        Mqtt5MessageWithReasonStringEncoder(@NotNull final T message) {
            super(message);
        }

        @Override
        int minEncodedPropertyLength() {
            return super.minEncodedPropertyLength() -
                    Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength(message.getRawReasonString());
        }

        /**
         * Encodes the Reason String of the MQTT message of this encoder if it must not be omitted due to the given
         * maximum packet size.
         *
         * @param maxPacketSize the maximum packet size.
         * @param out           the byte buffer to encode to.
         */
        void encodeReasonString(final int maxPacketSize, @NotNull final ByteBuf out) {
            if (!mustOmitProperties(maxPacketSize)) {
                Mqtt5MessageEncoderUtil.encodeNullableProperty(REASON_STRING, message.getRawReasonString(), out);
            }
        }

    }


    /**
     * Base class for encoders of MQTT messages with an omissible Reason Code, an omissible Reason String and omissible
     * User Properties. The Reason Code is omitted if it is the default and the property length is 0.
     */
    static abstract class Mqtt5MessageWithOmissibleReasonCodeEncoder<T extends Mqtt5MessageWithReasonString>
            extends Mqtt5MessageWithReasonStringEncoder<T> {

        Mqtt5MessageWithOmissibleReasonCodeEncoder(@NotNull final T message) {
            super(message);
        }

        /**
         * @return whether the Reason Code of the MQTT message of this encoder can be omitted if the property length is
         * 0.
         */
        abstract boolean canOmitReasonCode();

        @Override
        int encodedPropertyLengthWithHeader(final int propertyLength) {
            if (propertyLength == 0) {
                if (canOmitReasonCode()) {
                    return -1;
                }
                return 0;
            }
            return super.encodedPropertyLengthWithHeader(propertyLength);
        }

    }

}
