package org.mqttbee.mqtt5.message.puback;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PubAckEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckInternal extends Mqtt5Message.Mqtt5MessageWithProperties {

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

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5PubAckEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5PubAckEncoder.INSTANCE.encodedRemainingLength(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5PubAckEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
