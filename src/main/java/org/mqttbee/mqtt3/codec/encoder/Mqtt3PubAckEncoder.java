package org.mqttbee.mqtt3.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.puback.Mqtt3PubAckImpl;

import javax.inject.Singleton;


/**
 * @author Daniel Krüger
 */
@Singleton
public class Mqtt3PubAckEncoder implements Mqtt3MessageEncoder<Mqtt3PubAckImpl> {

    public static final Mqtt3PubAckEncoder INSTANCE = new Mqtt3PubAckEncoder();

    private static final byte FIXED_HEADER = (byte) (Mqtt3MessageType.PUBACK.getCode() << 4);
    private static final int FIXED_LENGTH = 2;
    private static final int REMAINING_LENGTH = 2;

    @Override
    public void encode(
            @NotNull final Mqtt3PubAckImpl pubAck, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        encodeFixedHeader(out);
        encodeVariableHeader(pubAck, out);
    }

    public int encodedRemainingLength() {
        return REMAINING_LENGTH;
    }


    private void encodeFixedHeader(@NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        out.writeByte(FIXED_LENGTH);
    }

    private void encodeVariableHeader(@NotNull final Mqtt3PubAckImpl pubAck, @NotNull final ByteBuf out) {
        out.writeShort(pubAck.getPacketId());
    }
}
