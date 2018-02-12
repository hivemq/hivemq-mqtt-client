package org.mqttbee.mqtt3.message.pubrec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3PubRec;
import org.mqttbee.mqtt3.codec.encoder.Mqtt3PubRecEncoder;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PubRecImpl implements Mqtt3PubRec, Mqtt3Message {

    private final int packetId;

    public Mqtt3PubRecImpl(final int packetId) {
        this.packetId = packetId;
    }

    @Override
    public void encode(
            @NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt3PubRecEncoder.INSTANCE.encode(this, channel, out);

    }

    @Override
    public int encodedLength() {
        return 4;
    }


    public int getPacketId() {
        return packetId;
    }

}
