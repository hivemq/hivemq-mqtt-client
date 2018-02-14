package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.Mqtt5ServerDataImpl;
import org.mqttbee.mqtt5.exceptions.Mqtt5MaximumPacketSizeExceededException;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * Base class for encoders of MQTT messages.
 *
 * @author Silvio Giebl
 */
public abstract class Mqtt5MessageEncoder<T extends Mqtt5Message> {

    final T message;

    Mqtt5MessageEncoder(@NotNull final T message) {
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
        final int maximumPacketSize = Mqtt5ServerDataImpl.getMaximumPacketSize(channel);
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

}
