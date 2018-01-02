package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.ping.Mqtt5PingRespImpl;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.ping.Mqtt5PingRespImpl.INSTANCE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PingRespDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;

    @Override
    public Mqtt5PingRespImpl decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
        if (flags != FLAGS) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        if (in.readableBytes() != 0) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        return INSTANCE;
    }

}
