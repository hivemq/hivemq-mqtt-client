package org.mqttbee.mqtt5.message.puback;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.puback.Mqtt5PubAck;
import org.mqttbee.api.mqtt5.message.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PubAckEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckImpl extends Mqtt5Message.Mqtt5MessageWithOmissibleProperties implements Mqtt5PubAck {

    @NotNull
    public static final Mqtt5PubAckReasonCode DEFAULT_REASON_CODE = Mqtt5PubAckReasonCode.SUCCESS;

    private final int packetIdentifier;
    private final Mqtt5PubAckReasonCode reasonCode;

    public Mqtt5PubAckImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubAckReasonCode reasonCode,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties) {
        super(reasonString, userProperties);
        this.packetIdentifier = packetIdentifier;
        this.reasonCode = reasonCode;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    @NotNull
    @Override
    public Mqtt5PubAckReasonCode getReasonCode() {
        return reasonCode;
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
