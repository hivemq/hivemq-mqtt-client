package org.mqttbee.mqtt3.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.codec.Mqtt3DataTypes;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * Encoder for a MQTT message according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt3MessageEncoder<T extends Mqtt3Message> {

    /**
     * Calculates the encoded length of a MQTT message with the given remaining length.
     *
     * @param remainingLength the remaining length of the MQTT message.
     * @return the encoded length of the MQTT message.
     */
    static int encodedLength(final int remainingLength) {
        return 1 + Mqtt3DataTypes.encodedVariableByteIntegerLength(remainingLength) + remainingLength;
    }

    /**
     * Encodes the given MQTT message to the given byte buffer which will be written to the given channel.
     *
     * @param message the MQTT message to encode.
     * @param channel the channel where the byte buffer will be written to.
     * @param out     the byte buffer to encode to.
     */
    void encode(@NotNull T message, @NotNull Channel channel, @NotNull ByteBuf out);

}
