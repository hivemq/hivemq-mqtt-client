package org.mqttbee.mqtt3.message.pubcomp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PubCompInternal implements Mqtt3Message {


    private final Mqtt3PubCompImpl pubComp;
    private final int packetId;

    public Mqtt3PubCompInternal(Mqtt3PubCompImpl pubComp, int packetId) {
        this.pubComp = pubComp;
        this.packetId = packetId;
    }

    public Mqtt3PubCompImpl getPubComp() {
        return pubComp;
    }

    public int getPacketId() {
        return packetId;
    }

    @Override
    public void encode(
            @NotNull Channel channel, @NotNull ByteBuf out) {
        //TODO
    }

    @Override
    public int encodedLength() {
        return 0;
        //TODO
    }
}
