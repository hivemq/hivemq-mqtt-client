package org.mqttbee.mqtt5.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Message {

    void encode(@NotNull Channel channel, @NotNull ByteBuf out);

    int encodedLength();


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

        public final int encodedRemainingLength() {
            if (remainingLength == -1) {
                remainingLength = calculateEncodedRemainingLength();
            }
            return remainingLength;
        }

        protected abstract int calculateEncodedRemainingLength();

        public final int encodedPropertyLength() {
            if (propertyLength == -1) {
                propertyLength = calculateEncodedPropertyLength();
            }
            return propertyLength;
        }

        protected abstract int calculateEncodedPropertyLength();

    }

}
