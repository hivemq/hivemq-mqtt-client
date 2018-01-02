package org.mqttbee.mqtt5.message.suback;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubAckInternal implements Mqtt5Message {

    private final Mqtt5SubAckImpl subAck;
    private int packetIdentifier;

    public Mqtt5SubAckInternal(@NotNull final Mqtt5SubAckImpl subAck) {
        this.subAck = subAck;
    }

    @NotNull
    public Mqtt5SubAckImpl getSubAck() {
        return subAck;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public void setPacketIdentifier(final int packetIdentifier) {
        this.packetIdentifier = packetIdentifier;
    }

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.SUBACK;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getSubAckEncoder().encode(this, channel, out);
    }

}
