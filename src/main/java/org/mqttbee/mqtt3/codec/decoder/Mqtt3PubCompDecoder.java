package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.pubcomp.Mqtt3PubCompImpl;

public class Mqtt3PubCompDecoder implements Mqtt3MessageDecoder {


    private static final int FLAGS = 0b0000;
    private static final int REMAINING_LENGTH = 2;

    @Nullable
    @Override
    public Mqtt3PubCompImpl decode(
            int flags, @NotNull Channel channel, @NotNull ByteBuf in) {

        if (flags != FLAGS) {
            return null;
        }

        if (in.readableBytes() != REMAINING_LENGTH) {
            return null;
        }


        final int packetId = in.readShort();


        return null;
    }
}
