package org.mqttbee.mqtt3.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.pubcomp.Mqtt3PubCompImpl;

import javax.inject.Singleton;


/**
 * @author Daniel Krüger
 */
@Singleton
public class Mqtt3PubCompEncoder implements Mqtt3MessageEncoder<Mqtt3PubCompImpl> {

    public static final Mqtt3PubCompEncoder INSTANCE = new Mqtt3PubCompEncoder();

    private static final int FIXED_HEADER = Mqtt3MessageType.PUBCOMP.getCode() << 4;
    private static final int FIXED_LENGTH = 2;
    private static final int REMAINING_LENGTH = 2;

    @Override
    public void encode(
            @NotNull final Mqtt3PubCompImpl pubComp, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        encodeFixedHeader(out);
        encodeVariableHeader(pubComp, out);
    }

    public int encodedRemainingLength() {
        return REMAINING_LENGTH;
    }


    private void encodeFixedHeader(@NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        out.writeByte(FIXED_LENGTH);
    }

    private void encodeVariableHeader(@NotNull final Mqtt3PubCompImpl pubComp, @NotNull final ByteBuf out) {
        out.writeShort(pubComp.getPacketId());
    }
}
