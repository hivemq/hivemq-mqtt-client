package org.mqttbee.mqtt5.message.pubcomp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PubCompEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompInternal extends Mqtt5Message.Mqtt5MessageWithProperties {

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

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5PubCompEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5PubCompEncoder.INSTANCE.encodedRemainingLength(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5PubCompEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
