package org.mqttbee.mqtt3.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.pubrel.Mqtt3PubRelImpl;

import javax.inject.Singleton;


/**
 * @author Daniel Krüger
 */
@Singleton
public class Mqtt3PubRelEncoder implements Mqtt3MessageEncoder<Mqtt3PubRelImpl> {

    public static final Mqtt3PubRelEncoder INSTANCE = new Mqtt3PubRelEncoder();

    private static final int FIXED_HEADER = Mqtt3MessageType.PUBREL.getCode() << 4;
    private static final int FIXED_LENGTH = 2;
    private static final int REMAINING_LENGTH = 2;

    @Override
    public void encode(
            @NotNull final Mqtt3PubRelImpl pubRel, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        encodeFixedHeader(out);
        encodeVariableHeader(pubRel, out);
    }

    public int encodedRemainingLength() {
        return REMAINING_LENGTH;
    }


    private void encodeFixedHeader(@NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        out.writeByte(FIXED_LENGTH);
    }

    private void encodeVariableHeader(@NotNull final Mqtt3PubRelImpl pubRel, @NotNull final ByteBuf out) {
        out.writeShort(pubRel.getPacketId());
    }
}
