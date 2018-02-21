package org.mqttbee.mqtt3.message.suback;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAckReturnCode;
import org.mqttbee.mqtt3.message.Mqtt3Message;

public class Mqtt3SubAckImpl implements Mqtt3SubAck, Mqtt3Message {

    private final int packetId;
    private final ImmutableList<Mqtt3SubAckReturnCode> reasonCodes;

    public Mqtt3SubAckImpl(
            final int packetId, final ImmutableList<Mqtt3SubAckReturnCode> reasonCodes) {
        this.packetId = packetId;
        this.reasonCodes = reasonCodes;
    }

    public int getPacketId() {
        return packetId;
    }

    @Override
    public void encode(
            @NotNull final Channel channel, @NotNull final ByteBuf out) {
        //todo
        // Mqtt3SubAckEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    public int encodedLength() {
        //TODO
        return 0;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt3SubAckReturnCode> getReasonCodes() {
        return reasonCodes;
    }

}
