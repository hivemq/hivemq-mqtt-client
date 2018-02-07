package org.mqttbee.mqtt3.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.pubrec.Mqtt3PubRecImpl;

import javax.inject.Singleton;


/**
 * @author Daniel Krüger
 */
@Singleton
public class Mqtt3PubRecEncoder implements Mqtt3MessageEncoder<Mqtt3PubRecImpl> {

    public static final Mqtt3PubRecEncoder INSTANCE = new Mqtt3PubRecEncoder();

    private static final int FIXED_HEADER = Mqtt3MessageType.PUBREC.getCode() << 4;
    private static final int FIXED_LENGTH = 2;
    private static final int REMAINING_LENGTH = 2;

    @Override
    public void encode(
            @NotNull final Mqtt3PubRecImpl pubRec, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        encodeFixedHeader(out);
        encodeVariableHeader(pubRec, out);
    }

    public int encodedRemainingLength() {
        return REMAINING_LENGTH;
    }


    private void encodeFixedHeader(@NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        out.writeByte(FIXED_LENGTH);
    }

    private void encodeVariableHeader(@NotNull final Mqtt3PubRecImpl pubRec, @NotNull final ByteBuf out) {
        out.writeShort(pubRec.getPacketId());
    }
}
