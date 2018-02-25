package org.mqttbee.mqtt3.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt3.message.pingresp.Mqtt3PingRespImpl;

/**
 * Decoder for the MQTT Ping Resp message.
 * See the <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/errata01/os/mqtt-v3.1.1-errata01-os-complete.html#_Toc442180899">specification</a>
 * for details.
 */
public class Mqtt3PingRespDecoder implements Mqtt3MessageDecoder {

    private static final int FLAGS = 0b0000;

    @Nullable
    @Override
    public Mqtt3Message decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {

        if (flags != FLAGS) {
            channel.close();
            return null;
        }

        if (in.readableBytes() != 0) {
            channel.close();
            return null;
        }

        return new Mqtt3PingRespImpl();
    }

    //FIXME same as mqtt5

}

