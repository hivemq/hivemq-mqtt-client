package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5MessageEncoder<T extends Mqtt5Message> {

    static int encodedLength(final int remainingLength) {
        return 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(remainingLength) + remainingLength;
    }

    void encode(@NotNull T message, @NotNull Channel channel, @NotNull ByteBuf out);

}
