package org.mqttbee.mqtt3.message.pubrec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3PubRec;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt3.message.pubcomp.Mqtt3PubCompImpl;

public class Mqtt3PubRecInternal implements Mqtt3Message {


    private final Mqtt3PubRecImpl pubRec;
    private final int packetId;

    public Mqtt3PubRecInternal(Mqtt3PubRecImpl pubRec, int packetId) {
        this.pubRec = pubRec;
        this.packetId = packetId;
    }

    public Mqtt3PubRecImpl getPubComp() {
        return pubRec;
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
