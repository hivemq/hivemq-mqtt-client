package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.pubrel.Mqtt3PubRelImpl;

public class Mqtt3PubRelDecoder implements Mqtt3MessageDecoder {

    private static final int FLAGS = 0b0010;
    private static final int REMAINING_LENGTH = 2;


    @Nullable
    @Override
    public Mqtt3PubRelImpl decode(
            int flags, @NotNull Channel channel, @NotNull ByteBuf in) {

        if (flags != FLAGS) {
            return null;
        }


        if (in.readableBytes() != REMAINING_LENGTH) {
            return null;
        }

        final int packetID = in.readShort();

        return new Mqtt3PubRelImpl(packetID);
    }
}
