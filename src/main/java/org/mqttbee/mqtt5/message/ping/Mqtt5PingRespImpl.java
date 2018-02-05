package org.mqttbee.mqtt5.message.ping;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5PingResp;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingRespImpl implements Mqtt5PingResp, Mqtt5Message {

    public static final Mqtt5PingRespImpl INSTANCE = new Mqtt5PingRespImpl();

    private Mqtt5PingRespImpl() {
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int encodedLength(final int maxPacketSize) {
        throw new UnsupportedOperationException();
    }

}
