package org.mqttbee.mqtt3.message.pubrel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3PubRel;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PubRelImpl implements Mqtt3PubRel, Mqtt3Message {


    private final int packetId;

    public Mqtt3PubRelImpl(int packetId) {
        this.packetId = packetId;
    }

    public int getPacketId() {
        return packetId;
    }

    @Override
    public void encode(
            @NotNull Channel channel, @NotNull ByteBuf out) {
        //todo

    }

    @Override
    public int encodedLength() {
        //todo
        return 0;
    }
}
