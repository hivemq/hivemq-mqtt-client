package org.mqttbee.mqtt3.message.pubcomp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3PubComp;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PubCompImpl implements Mqtt3PubComp, Mqtt3Message {

    private final int packetId;

    public Mqtt3PubCompImpl(int packetId) {
        this.packetId = packetId;
    }




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


    public int getPacketId() {
        return packetId;
    }
}
