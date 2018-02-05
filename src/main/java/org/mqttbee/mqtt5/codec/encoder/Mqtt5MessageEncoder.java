package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * Encoder for a MQTT message according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5MessageEncoder<T extends Mqtt5Message> {

    /**
     * Encodes the given MQTT message to the given byte buffer which will be written to the given channel.
     *
     * @param message the MQTT message to encode.
     * @param channel the channel where the byte buffer will be written to.
     * @param out     the byte buffer to encode to.
     */
    void encode(@NotNull T message, @NotNull Channel channel, @NotNull ByteBuf out);

}
