package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil;
import org.mqttbee.mqtt5.exceptions.Mqtt5MaximumPacketSizeExceededException;
import org.mqttbee.mqtt5.handler.Mqtt5ServerData;

import static org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithReasonString;
import static org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithUserProperties;
import static org.mqttbee.mqtt5.message.Mqtt5Property.REASON_STRING;

/**
 * Interface for MQTT messages that can be encoded according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public abstract class Mqtt5MessageEncoder<T extends Mqtt5Message> {

    protected final T message;

    public Mqtt5MessageEncoder(@NotNull final T message) {
        this.message = message;
    }

    /**
     * Encodes this MQTT message.
     *
     * @param channel the channel where the given byte buffer will be written to.
     * @param out     the byte buffer to encode to.
     */
    public abstract void encode(@NotNull Channel channel, @NotNull ByteBuf out);

    /**
     * Creates a byte buffer with the correct size for this MQTT message.
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
     * Returns the byte count of this MQTT message respecting the given maximum packet size. Calculation is only
     * performed if necessary.
     *
     * @param maxPacketSize the maximum packet size.
     * @return the encoded length of this MQTT message respecting the maximum packet size or -1 if the minimal encoded
     * length of this message is bigger than the maximum packet size.
     */
    public abstract int encodedLength(final int maxPacketSize);


    /**
     * Base class for MQTT messages with a properties field with omissible user properties in its variable header.
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
         * Returns the byte count of this MQTT message without omitting any properties. Calculation is only performed if
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
         * Returns the minimal byte count of this MQTT message. All properties which can be omitted are omitted.
         *
         * @return the minimal encoded length of this MQTT message.
         */
        private int minEncodedLength() {
            return Mqtt5MessageEncoderUtil.encodedPacketLength(minEncodedRemainingLength());
        }

        /**
         * Returns the remaining length byte count of this MQTT message respecting the given maximum packet size.
         * <p>
         * If the minimal encoded length of this MQTT message is bigger than the given maximum packet size the returned
         * value is unspecified.
         *
         * @param maxPacketSize the maximum packet size.
         * @return the encoded remaining length of this MQTT message respecting the maximum packet size.
         */
        protected final int encodedRemainingLength(final int maxPacketSize) {
            return mustOmitProperties(maxPacketSize) ? minEncodedRemainingLength() : maxEncodedRemainingLength();
        }

        /**
         * Returns the remaining length byte count of this MQTT message without omitting any properties. Calculation is
         * only performed if necessary.
         *
         * @return the encoded remaining length of this MQTT message without omitting any properties.
         */
        private int maxEncodedRemainingLength() {
            if (remainingLength == -1) {
                remainingLength =
                        calculateEncodedRemainingLength() + encodedPropertyLengthWithHeader(maxEncodedPropertyLength());
            }
            return remainingLength;
        }

        /**
         * Returns the minimal remaining length byte count of this MQTT message. All properties which can be omitted are
         * omitted.
         *
         * @return the minimal encoded remaining length of this MQTT message.
         */
        private int minEncodedRemainingLength() {
            return maxEncodedRemainingLength() - encodedPropertyLengthWithHeader(maxEncodedPropertyLength()) +
                    encodedPropertyLengthWithHeader(minEncodedPropertyLength());
        }

        /**
         * Calculates the remaining length byte count of this MQTT message.
         *
         * @return the encoded remaining length of this MQTT message.
         */
        protected abstract int calculateEncodedRemainingLength();

        /**
         * Returns the property length byte count of this MQTT message respecting the given maximum packet size.
         * <p>
         * If the minimal encoded length of this MQTT message is bigger than the given maximum packet size the returned
         * value is unspecified.
         *
         * @param maxPacketSize the maximum packet size.
         * @return the encoded property length of this MQTT message respecting the maximum packet size.
         */
        protected final int encodedPropertyLength(final int maxPacketSize) {
            return mustOmitProperties(maxPacketSize) ? minEncodedPropertyLength() : maxEncodedPropertyLength();
        }

        /**
         * Returns the property length byte count of this MQTT message without omitting any properties. Calculation is
         * only performed if necessary.
         *
         * @return the encoded property length of this MQTT message without omitting any properties.
         */
        private int maxEncodedPropertyLength() {
            if (propertyLength == -1) {
                propertyLength = calculateEncodedPropertyLength();
            }
            return propertyLength;
        }

        /**
         * Returns the minimal property length byte count of this MQTT message. All properties which can be omitted are
         * omitted.
         *
         * @return the minimal encoded property length of this MQTT message.
         */
        int minEncodedPropertyLength() {
            return maxEncodedPropertyLength() - getUserProperties().encodedLength();
        }

        /**
         * Calculates the property length byte count of this MQTT message.
         *
         * @return the encoded property length of this MQTT message.
         */
        protected abstract int calculateEncodedPropertyLength();

        /**
         * Checks whether properties of this MQTT message must be omitted to fit the given maximum packet size.
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
         * Encodes the user properties of this MQTT message if they must not be omitted due to the given maximum packet
         * size.
         *
         * @param maxPacketSize the maximum packet size.
         * @param out           the byte buffer to encode to.
         */
        protected final void encodeUserProperties(final int maxPacketSize, @NotNull final ByteBuf out) {
            if (!mustOmitProperties(maxPacketSize)) {
                getUserProperties().encode(out);
            }
        }

    }


    /**
     * Base class for MQTT messages with a properties field with a reason string property in its variable header.
     */
    public static abstract class Mqtt5MessageWithUserPropertiesEncoder<T extends Mqtt5MessageWithUserProperties>
            extends Mqtt5MessageWithPropertiesEncoder<T> {

        public Mqtt5MessageWithUserPropertiesEncoder(@NotNull final T message) {
            super(message);
        }

        @Override
        Mqtt5UserPropertiesImpl getUserProperties() {
            return message.getUserProperties();
        }

    }


    /**
     * Base class for MQTT messages with a properties field with a reason string property in its variable header.
     */
    public static abstract class Mqtt5MessageWithReasonStringEncoder<T extends Mqtt5MessageWithReasonString>
            extends Mqtt5MessageWithUserPropertiesEncoder<T> {

        public Mqtt5MessageWithReasonStringEncoder(@NotNull final T message) {
            super(message);
        }

        @Override
        int minEncodedPropertyLength() {
            return super.minEncodedPropertyLength() -
                    Mqtt5MessageEncoderUtil.nullablePropertyEncodedLength(message.getRawReasonString());
        }

        /**
         * Encodes the reason string of this MQTT message if it must not be omitted due to the given maximum packet
         * size.
         *
         * @param maxPacketSize the maximum packet size.
         * @param out           the byte buffer to encode to.
         */
        protected void encodeReasonString(final int maxPacketSize, @NotNull final ByteBuf out) {
            if (!mustOmitProperties(maxPacketSize)) {
                Mqtt5MessageEncoderUtil.encodeNullableProperty(REASON_STRING, message.getRawReasonString(), out);
            }
        }

    }


    /**
     * Base class for MQTT messages with a omissible properties field in its variable header.
     */
    public static abstract class Mqtt5MessageWithOmissibleReasonCodeEncoder<T extends Mqtt5MessageWithReasonString>
            extends Mqtt5MessageWithReasonStringEncoder<T> {

        public Mqtt5MessageWithOmissibleReasonCodeEncoder(@NotNull final T message) {
            super(message);
        }

        protected abstract boolean canOmitReasonCode();

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
