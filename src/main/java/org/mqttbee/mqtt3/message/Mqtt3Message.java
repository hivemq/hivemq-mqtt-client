package org.mqttbee.mqtt3.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;

public interface Mqtt3Message {


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

}
