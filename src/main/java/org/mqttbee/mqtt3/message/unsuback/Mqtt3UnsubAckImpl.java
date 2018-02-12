package org.mqttbee.mqtt3.message.unsuback;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3UnsubAck;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3UnsubAckImpl implements Mqtt3UnsubAck, Mqtt3Message {

    private final int packetId;

    public Mqtt3UnsubAckImpl(final int packetId) {
        this.packetId = packetId;
    }

    public int getPacketId() {
        return packetId;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {

    }

    @Override
    public int encodedLength() {
        return 0;
    }

}
