package org.mqttbee.mqtt5.message.ping;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5PingReq;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PingReqImpl implements Mqtt5PingReq {

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.PINGREQ;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getPingReqEncoder().encode(this, channel, out);
    }

}
