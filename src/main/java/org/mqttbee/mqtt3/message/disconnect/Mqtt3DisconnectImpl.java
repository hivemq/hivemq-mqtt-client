package org.mqttbee.mqtt3.message.disconnect;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3Disconnect;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3DisconnectImpl implements Mqtt3Disconnect, Mqtt3Message {

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        //TODO
    }

    @Override
    public int encodedLength() {
        //TODO
        return 0;
    }

}
