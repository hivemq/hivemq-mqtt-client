package org.mqttbee.mqtt5.message.pubrec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PubRecEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecInternal extends Mqtt5Message.Mqtt5MessageWithProperties {

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

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5PubRecEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5PubRecEncoder.INSTANCE.encodedRemainingLength(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5PubRecEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
