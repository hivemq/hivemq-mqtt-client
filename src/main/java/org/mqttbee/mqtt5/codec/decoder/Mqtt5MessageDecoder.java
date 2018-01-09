package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * Decoder for a MQTT message according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5MessageDecoder {

    /**
     * Decodes a MQTT message from the given byte buffer which was read from the given channel.
     *
     * @param flags   the flags of the fixed header.
     * @param channel the channel the byte buffer was read from.
     * @param in      the byte buffer which contains the encoded message without the fixed header.
     * @return the decoded MQTT message of null if there are not enough byte in the byte buffer or if the byte buffer
     * did not contain a valid encoded MQTT message.
     */
    @Nullable
    Mqtt5Message decode(int flags, @NotNull Channel channel, @NotNull ByteBuf in);

}
