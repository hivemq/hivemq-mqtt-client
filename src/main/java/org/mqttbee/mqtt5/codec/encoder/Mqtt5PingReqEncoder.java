package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingReqImpl;

import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PingReqEncoder implements Mqtt5MessageEncoder<Mqtt5PingReqImpl> {

    private final byte[] PACKET = {(byte) (Mqtt5MessageType.PINGREQ.getCode() << 4), 0};

    public void encode(
            @NotNull final Mqtt5PingReqImpl pingReq, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        out.writeBytes(PACKET);
    }

}
