package org.mqttbee.mqtt5.message.pubcomp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompInternal implements Mqtt5Message {

    private final Mqtt5PubCompImpl pubComp;
    private int packetIdentifier;

    public Mqtt5PubCompInternal(@NotNull final Mqtt5PubCompImpl pubComp) {
        this.pubComp = pubComp;
    }

    @NotNull
    public Mqtt5PubCompImpl getPubComp() {
        return pubComp;
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
        return Mqtt5MessageType.PUBCOMP;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getPubCompEncoder().encode(this, channel, out);
    }

}
