package org.mqttbee.mqtt.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;

/**
 * Encoder for a MQTT message.
 *
 * @author Silvio Giebl
 */
public interface MqttMessageEncoder {

    /**
     * Encodes the given MQTT message.
     *
     * @param out     the byte buffer to encode to.
     * @param channel the channel where the given byte buffer will be written to.
     */
    void encode(@NotNull ByteBuf out, @NotNull Channel channel);

    /**
     * Allocates a byte buffer with the correct size for the given MQTT message.
     *
     * @param channel the channel where the allocated byte buffer will be written to.
     * @return the allocated byte buffer.
     */
    @NotNull
    ByteBuf allocateBuffer(@NotNull final Channel channel);

    /**
     * Returns the byte count of the given MQTT message respecting the given maximum packet size. Calculation
     * is only performed if necessary.
     *
     * @param maxPacketSize the maximum packet size.
     * @return the encoded length of the MQTT message respecting the maximum packet size or -1 if the minimal encoded
     * length of the MQTT message is bigger than the maximum packet size.
     */
    int encodedLength(final int maxPacketSize);

}
