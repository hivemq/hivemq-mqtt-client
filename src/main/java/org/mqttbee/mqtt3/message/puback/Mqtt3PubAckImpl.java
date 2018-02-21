package org.mqttbee.mqtt3.message.puback;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.publish.puback.Mqtt3PubAck;
import org.mqttbee.mqtt3.codec.encoder.Mqtt3PubAckEncoder;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PubAckImpl implements Mqtt3PubAck, Mqtt3Message {

    private final int packetId;

    public Mqtt3PubAckImpl(final int packetId) {
        this.packetId = packetId;
    }

    @Override
    public void encode(
            @NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt3PubAckEncoder.INSTANCE.encode(this, channel, out);

    }

    @Override
    public int encodedLength() {

        return 4;
    }

    public int getPacketId() {
        return packetId;
    }

}
