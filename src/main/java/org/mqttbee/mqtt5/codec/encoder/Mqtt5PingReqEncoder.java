package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReqImpl;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingReqEncoder extends Mqtt5MessageEncoder<Mqtt5PingReqImpl> {

    public static final Function<Mqtt5PingReqImpl, Mqtt5PingReqEncoder> PROVIDER = Mqtt5PingReqEncoder::new;

    private static final int ENCODED_LENGTH = 2;
    private static final ByteBuf PACKET =
            Unpooled.directBuffer(ENCODED_LENGTH).writeByte(Mqtt5MessageType.PINGREQ.getCode() << 4).writeByte(0);

    private Mqtt5PingReqEncoder(@NotNull final Mqtt5PingReqImpl message) {
        super(message);
    }

    @Override
    public int encodedLength(final int maxPacketSize) {
        return ENCODED_LENGTH;
    }

    @NotNull
    @Override
    public ByteBuf allocateBuffer(@NotNull final Channel channel) {
        return PACKET;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        // no op
    }

}
