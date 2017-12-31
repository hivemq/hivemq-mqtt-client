package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5MessageDecoder {

    @Nullable
    Mqtt5Message decode(int flags, @NotNull Channel channel, @NotNull ByteBuf in);

}
