package org.mqttbee.mqtt5.message.pubrec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecInternal implements Mqtt5Message {

    private final Mqtt5PubRecImpl pubRec;
    private int packetIdentifier;

    public Mqtt5PubRecInternal(@NotNull final Mqtt5PubRecImpl pubRec) {
        this.pubRec = pubRec;
    }

    @NotNull
    public Mqtt5PubRecImpl getPubRec() {
        return pubRec;
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
        return Mqtt5MessageType.PUBREC;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getPubRecEncoder().encode(this, channel, out);
    }

}
