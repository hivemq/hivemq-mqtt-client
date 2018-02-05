package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt3.message.puback.Mqtt3PubAckImpl;

public class Mqtt3PubAckDecoder implements Mqtt3MessageDecoder {


    private static final int FLAGS = 0b0000;
    private static final int REMAINING_LENGTH = 2;

    @Nullable
    @Override
    public Mqtt3Message decode(int flags, @NotNull Channel channel, @NotNull ByteBuf in) {

        if (flags != FLAGS) {
            return null;
        }


        if (in.readableBytes() != REMAINING_LENGTH) {
            return null;
        }

        final int packetId = in.readUnsignedShort();

        //TODO dummy austauschen
        return new Mqtt3PubAckImpl(packetId);
    }
}
