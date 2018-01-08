package org.mqttbee.mqtt5.message.suback;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubAckInternal implements Mqtt5Message {

    private final Mqtt5SubAckImpl subAck;
    private final int packetIdentifier;

    public Mqtt5SubAckInternal(@NotNull final Mqtt5SubAckImpl subAck, final int packetIdentifier) {
        this.subAck = subAck;
        this.packetIdentifier = packetIdentifier;
    }

    @NotNull
    public Mqtt5SubAckImpl getSubAck() {
        return subAck;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int encodedLength() {
        throw new UnsupportedOperationException();
    }

}
