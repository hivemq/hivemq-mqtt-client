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
    public Mqtt3PubRelImpl decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {

        if (flags != FLAGS) {
            channel.close();
            return null;
        }

        if (in.readableBytes() != REMAINING_LENGTH) {
            channel.close();
            return null;
        }
        final int packetID = in.readUnsignedShort();
        return new Mqtt3PubRelImpl(packetID);
    }

}
