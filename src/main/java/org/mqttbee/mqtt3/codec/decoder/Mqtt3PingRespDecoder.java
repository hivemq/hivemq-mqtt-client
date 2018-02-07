package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PingRespDecoder implements Mqtt3MessageDecoder {


    @Nullable
    @Override
    public Mqtt3Message decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        return null;
    }

    //FIXME same as mqtt5


}

