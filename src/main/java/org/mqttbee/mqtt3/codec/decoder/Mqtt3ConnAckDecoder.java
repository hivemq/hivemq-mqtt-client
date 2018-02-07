package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt3.message.connack.Mqtt3ConnAckImpl;
import org.mqttbee.mqtt3.message.connack.Mqtt3ConnAckReasonCode;


/**
 * @author Daniel Krüger
 */
public class Mqtt3ConnAckDecoder implements Mqtt3MessageDecoder {


    private static final int FLAGS = 0b0000;
    private static final int REMAINING_LENGTH = 2;

    @Nullable
    @Override
    public Mqtt3Message decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {


        if (flags != FLAGS) {
            channel.close();
            return null;
        }

        if (in.readableBytes() != REMAINING_LENGTH) {
            channel.close();
            return null;
        }


        final byte firstByte = in.readByte();

        if ((firstByte & 0xfe) != 0) {  //all bits except the last ust be 0

            return null;
        }

        final boolean sessionPresent = (firstByte & 0b1) == 1;

        final int code = in.readByte();
        final Mqtt3ConnAckReasonCode reasonCode = Mqtt3ConnAckReasonCode.fromCode(code);
        if (reasonCode == null) {
            return null;
        }

        return new Mqtt3ConnAckImpl(reasonCode, sessionPresent);
    }
}
