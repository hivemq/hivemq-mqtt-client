package org.mqttbee.mqtt3.message.subscribe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3Subscribe;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3SubscribeImpl implements Mqtt3Subscribe, Mqtt3Message {




    @Override
    public void encode(
            @NotNull Channel channel, @NotNull ByteBuf out) {
        //TODO

    }

    @Override
    public int encodedLength() {
        //TODO
        return 0;
    }
}
