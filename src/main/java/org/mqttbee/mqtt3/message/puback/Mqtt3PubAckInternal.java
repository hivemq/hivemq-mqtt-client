package org.mqttbee.mqtt3.message.puback;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.codec.encoder.Mqtt3PubAckEncoder;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PubAckEncoder;

public class Mqtt3PubAckInternal implements Mqtt3Message{

    private final Mqtt3PubAckImpl puback;
    private final int packetId;


    public Mqtt3PubAckInternal(Mqtt3PubAckImpl puback, int packetId) {
        this.puback = puback;
        this.packetId = packetId;
    }

    public Mqtt3PubAckImpl getPuback() {
        return puback;
    }

    public int getPacketId() {
        return packetId;
    }

    @Override
    public void encode(
            @NotNull Channel channel, @NotNull ByteBuf out) {
        Mqtt3PubAckEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    public int encodedLength() {
        return 4;
    }
}
