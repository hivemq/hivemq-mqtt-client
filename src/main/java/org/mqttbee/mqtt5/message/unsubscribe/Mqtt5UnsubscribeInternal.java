package org.mqttbee.mqtt5.message.unsubscribe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5UnsubscribeEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeInternal implements Mqtt5Message.Mqtt5MessageWithProperties {

    private final Mqtt5UnsubscribeImpl unsubscribe;
    private final int packetIdentifier;

    public Mqtt5UnsubscribeInternal(@NotNull final Mqtt5UnsubscribeImpl unsubscribe, final int packetIdentifier) {
        this.unsubscribe = unsubscribe;
        this.packetIdentifier = packetIdentifier;
    }

    @NotNull
    public Mqtt5UnsubscribeImpl getWrapped() {
        return unsubscribe;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        Mqtt5UnsubscribeEncoder.INSTANCE.encode(this, channel, out);
    }

    @Override
    public int encodedLength(final int maxPacketSize) {
        return unsubscribe.encodedLength(maxPacketSize);
    }

    @Override
    public int encodedRemainingLength(final int maxPacketSize) {
        return unsubscribe.encodedRemainingLength(maxPacketSize);
    }

    @Override
    public int encodedPropertyLength(final int maxPacketSize) {
        return unsubscribe.encodedPropertyLength(maxPacketSize);
    }

}
