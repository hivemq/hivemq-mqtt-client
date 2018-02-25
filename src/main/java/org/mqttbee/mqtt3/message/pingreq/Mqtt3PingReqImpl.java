package org.mqttbee.mqtt3.message.pingreq;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3PingReq;
import org.mqttbee.mqtt3.codec.encoder.Mqtt3PingReqEncoder;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PingReqImpl implements Mqtt3PingReq, Mqtt3Message {

    @Override
    public void encode(
            @NotNull Channel channel, @NotNull ByteBuf out) {
        Mqtt3PingReqEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    public int encodedLength() {
        return 2;
    }

}
