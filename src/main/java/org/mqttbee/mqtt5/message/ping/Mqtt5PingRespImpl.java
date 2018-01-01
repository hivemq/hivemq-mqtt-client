package org.mqttbee.mqtt5.message.ping;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5PingResp;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingRespImpl implements Mqtt5PingResp {

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.PINGRESP;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getPingRespEncoder().encode(this, channel, out);
    }

}
