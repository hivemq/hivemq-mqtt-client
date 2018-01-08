package org.mqttbee.mqtt5.message.pubrel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PubRelEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRelInternal extends Mqtt5Message.Mqtt5MessageWithProperties {

    private final Mqtt5PubRelImpl pubRel;
    private final int packetIdentifier;

    public Mqtt5PubRelInternal(@NotNull final Mqtt5PubRelImpl pubRel, final int packetIdentifier) {
        this.pubRel = pubRel;
        this.packetIdentifier = packetIdentifier;
    }

    @NotNull
    public Mqtt5PubRelImpl getPubRel() {
        return pubRel;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5PubRelEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        return Mqtt5PubRelEncoder.INSTANCE.encodedRemainingLength(this);
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        return Mqtt5PubRelEncoder.INSTANCE.encodedPropertyLength(this);
    }

}
