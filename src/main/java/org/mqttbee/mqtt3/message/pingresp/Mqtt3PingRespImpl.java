package org.mqttbee.mqtt3.message.pingresp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3PingResp;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PingRespImpl implements Mqtt3PingResp, Mqtt3Message {

    @Override
    public void encode(
            @NotNull Channel channel, @NotNull ByteBuf out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int encodedLength() {
        return 0;
    }
}
