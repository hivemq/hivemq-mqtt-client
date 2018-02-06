package org.mqttbee.mqtt3.message.suback;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt3.message.pubrel.Mqtt3PubRelImpl;

public class Mqtt3SubAckInternal implements Mqtt3Message {


    private final Mqtt3SubAckImpl subAck;
    private final int packetId;

    public Mqtt3SubAckInternal(Mqtt3SubAckImpl subAck, int packetId) {
        this.subAck = subAck;
        this.packetId = packetId;
    }

    public Mqtt3SubAckImpl getPubComp() {
        return subAck;
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
