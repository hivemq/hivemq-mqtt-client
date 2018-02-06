package org.mqttbee.mqtt3.message.publish;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PublishInternal implements Mqtt3Message{



    private final Mqtt3PublishImpl publish;
    private final int packetId;

    public Mqtt3PublishInternal(Mqtt3PublishImpl publish, int packetId){
        this.packetId = packetId;
        this.publish = publish;
    }

    public Mqtt3PublishImpl getPublish() {
        return publish;
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
        //TODO
        return 0;
    }
}
