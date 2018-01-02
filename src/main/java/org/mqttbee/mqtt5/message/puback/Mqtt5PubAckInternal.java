package org.mqttbee.mqtt5.message.puback;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckInternal implements Mqtt5Message {

    private final Mqtt5PubAckImpl pubAck;
    private int packetIdentifier;

    public Mqtt5PubAckInternal(@NotNull final Mqtt5PubAckImpl pubAck) {
        this.pubAck = pubAck;
    }

    @NotNull
    public Mqtt5PubAckImpl getPubAck() {
        return pubAck;
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
        return Mqtt5MessageType.PUBACK;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getPubAckEncoder().encode(this, channel, out);
    }

}
