package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.unsuback.Mqtt3UnsubAckImpl;

public class Mqtt3UnsubAckDecoder implements Mqtt3MessageDecoder{


    private static final int FLAGS = 0b0000;
    private static final int REMAINING_LENGTH =2; // 2 for the packetId


    @Nullable
    @Override
    public Mqtt3UnsubAckImpl decode(
            int flags, @NotNull Channel channel, @NotNull ByteBuf in) {
        if(flags != FLAGS){

            return null;
        }

        if(in.readableBytes() < REMAINING_LENGTH){

            return null;
        }


        final int packetId = in.readShort();
        return new Mqtt3UnsubAckImpl(packetId);
    }
}
