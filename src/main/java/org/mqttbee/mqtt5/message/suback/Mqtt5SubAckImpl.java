package org.mqttbee.mqtt5.message.suback;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5SubAck;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubAckImpl extends Mqtt5Message.Mqtt5MessageWithReasonString implements Mqtt5SubAck {

    private final int packetIdentifier;
    private final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes;

    public Mqtt5SubAckImpl(
            final int packetIdentifier, @NotNull final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties) {
        super(reasonString, userProperties);
        this.packetIdentifier = packetIdentifier;
        this.reasonCodes = reasonCodes;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5SubAckReasonCode> getReasonCodes() {
        return reasonCodes;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int calculateEncodedRemainingLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int calculateEncodedPropertyLength() {
        throw new UnsupportedOperationException();
    }

}
