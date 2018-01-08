package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;

/**
 * Interface for MQTT messages that can be encoded according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5Message {

    /**
     * Encodes this MQTT message.
     *
     * @param channel the channel where the given byte buffer will be written to.
     * @param out     the byte buffer to encode to.
     */
    void encode(@NotNull Channel channel, @NotNull ByteBuf out);

    /**
     * Returns the byte count of this MQTT message. Calculation is only performed if necessary.
     *
     * @return the encoded length of this MQTT message.
     */
    int encodedLength();


    /**
     * Base class for MQTT messages with a properties field in its variable header.
     */
    abstract class Mqtt5MessageWithProperties implements Mqtt5Message {

        private int encodedLength = -1;
        private int remainingLength = -1;
        private int propertyLength = -1;

        @Override
        public int encodedLength() {
            if (encodedLength == -1) {
                encodedLength = Mqtt5MessageEncoder.encodedLength(encodedRemainingLength());
            }
            return encodedLength;
        }

        /**
         * Returns the remaining length byte count (without the fixed header) of this MQTT message. Calculation is only
         * performed if necessary.
         *
         * @return the encoded remaining length of this MQTT message.
         */
        public final int encodedRemainingLength() {
            if (remainingLength == -1) {
                remainingLength = calculateEncodedRemainingLength();
            }
            return remainingLength;
        }

        /**
         * Calculates the remaining length byte count (without the fixed header) of this MQTT message.
         *
         * @return the encoded remaining length of this MQTT message.
         */
        protected abstract int calculateEncodedRemainingLength();

        /**
         * Returns the properties byte count of this MQTT message. Calculation is only performed if necessary.
         *
         * @return the encoded property length of this MQTT message.
         */
        public final int encodedPropertyLength() {
            if (propertyLength == -1) {
                propertyLength = calculateEncodedPropertyLength();
            }
            return propertyLength;
        }

        /**
         * Calculates the properties byte count of this MQTT message.
         *
         * @return the encoded property length of this MQTT message.
         */
        protected abstract int calculateEncodedPropertyLength();

    }

}
