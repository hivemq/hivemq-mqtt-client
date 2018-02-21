package org.mqttbee.mqtt3.message.pubrel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.publish.pubrel.Mqtt3PubRel;
import org.mqttbee.mqtt3.codec.encoder.Mqtt3PubRelEncoder;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3PubRelImpl implements Mqtt3PubRel, Mqtt3Message {

    private final int packetId;

    public Mqtt3PubRelImpl(final int packetId) {
        this.packetId = packetId;
    }

    public int getPacketId() {
        return packetId;
    }

    @Override
    public void encode(
            @NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt3PubRelEncoder.INSTANCE.encode(this, channel, out);

    }

    @Override
    public int encodedLength() {
        return 4;
    }

}
