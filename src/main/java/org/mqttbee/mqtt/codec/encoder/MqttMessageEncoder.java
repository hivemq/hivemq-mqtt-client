package org.mqttbee.mqtt.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderApplier;
import org.mqttbee.mqtt.exceptions.MqttMaximumPacketSizeExceededException;
import org.mqttbee.mqtt.message.MqttMessage;
import org.mqttbee.mqtt5.Mqtt5ServerConnectionDataImpl;

/**
 * Encoder for a MQTT message.
 *
 * @param <M> the type of the MQTT message.
 * @author Silvio Giebl
 */
public abstract class MqttMessageEncoder<M extends MqttMessage<M, ?>> implements MqttMessageEncoderApplier<M> {

    protected M message;

    @NotNull
    @Override
    public MqttMessageEncoder<M> apply(@NotNull final M message) {
        this.message = message;
        return this;
    }

    /**
     * Encodes the given MQTT message.
     *
     * @param out     the byte buffer to encode to.
     * @param channel the channel where the given byte buffer will be written to.
     */
    public abstract void encode(@NotNull ByteBuf out, @NotNull Channel channel);

    /**
     * Allocates a byte buffer with the correct size for the given MQTT message.
     *
     * @param channel the channel where the allocated byte buffer will be written to.
     * @return the allocated byte buffer.
     */
    @NotNull
    public ByteBuf allocateBuffer(@NotNull final Channel channel) {
        final int maximumPacketSize = Mqtt5ServerConnectionDataImpl.getMaximumPacketSize(channel);
        final int encodedLength = encodedLength(maximumPacketSize);
        if (encodedLength < 0) {
            throw new MqttMaximumPacketSizeExceededException(message, maximumPacketSize);
        }
        return channel.alloc().ioBuffer(encodedLength, encodedLength);
    }

    /**
     * Returns the byte count of the given MQTT message respecting the given maximum packet size. Calculation
     * is only performed if necessary.
     *
     * @param maxPacketSize the maximum packet size.
     * @return the encoded length of the MQTT message respecting the maximum packet size or -1 if the minimal encoded
     * length of the MQTT message is bigger than the maximum packet size.
     */
    public abstract int encodedLength(final int maxPacketSize);

}
