package org.mqttbee.mqtt3.message.unsuback;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3UnsubAck;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt5.message.unsuback.Mqtt5UnsubAckReasonCode;

public class Mqtt3UnsubAckImpl implements Mqtt3UnsubAck, Mqtt3Message {

    private final int packetId;

    public Mqtt3UnsubAckImpl(int packetId) {
        this.packetId = packetId;
    }

    @Override
    public void encode(@NotNull Channel channel, @NotNull ByteBuf out) {

    }

    @Override
    public int encodedLength() {
        return 0;
    }
}
