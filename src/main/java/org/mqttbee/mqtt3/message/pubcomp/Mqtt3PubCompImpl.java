package org.mqttbee.mqtt3.message.pubcomp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3PubComp;
import org.mqttbee.mqtt3.codec.encoder.Mqtt3PubCompEncoder;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PubCompImpl implements Mqtt3PubComp, Mqtt3Message {

    private final int packetId;

    public Mqtt3PubCompImpl(final int packetId) {
        this.packetId = packetId;
    }


    @Override
    public void encode(
            @NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt3PubCompEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    public int encodedLength() {
        return 4;
    }


    public int getPacketId() {
        return packetId;
    }
}
