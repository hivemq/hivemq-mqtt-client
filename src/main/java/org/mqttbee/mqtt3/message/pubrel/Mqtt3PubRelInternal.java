package org.mqttbee.mqtt3.message.pubrel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt3.message.pubrec.Mqtt3PubRecImpl;

public class Mqtt3PubRelInternal implements Mqtt3Message {


    private final Mqtt3PubRelImpl pubRel;
    private final int packetId;

    public Mqtt3PubRelInternal(Mqtt3PubRelImpl pubRel, int packetId) {
        this.pubRel = pubRel;
        this.packetId = packetId;
    }

    public Mqtt3PubRelImpl getPubComp() {
        return pubRel;
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
