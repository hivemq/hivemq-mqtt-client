package org.mqttbee.mqtt5.message.ping;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5PingReq;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PingReqEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingReqImpl implements Mqtt5PingReq, Mqtt5Message {

    public static final Mqtt5PingReqImpl INSTANCE = new Mqtt5PingReqImpl();

    private Mqtt5PingReqImpl() {
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        // no op as the same packet is used always.
    }

    @Override
    public ByteBuf allocateBuffer(@NotNull final Channel channel) {
        return Mqtt5PingReqEncoder.PACKET;
    }

    @Override
    public int encodedLength(final int maxPacketSize) {
        return Mqtt5PingReqEncoder.ENCODED_LENGTH;
    }

}
