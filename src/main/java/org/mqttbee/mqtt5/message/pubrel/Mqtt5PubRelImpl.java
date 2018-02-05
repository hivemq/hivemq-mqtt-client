package org.mqttbee.mqtt5.message.pubrel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.Mqtt5PubRel;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PubRelEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRelImpl extends Mqtt5Message.Mqtt5MessageWithOmissibleProperties implements Mqtt5PubRel {

    @NotNull
    public static final Mqtt5PubRelReasonCode DEFAULT_REASON_CODE = Mqtt5PubRelReasonCode.SUCCESS;

    private final int packetIdentifier;
    private final Mqtt5PubRelReasonCode reasonCode;

    public Mqtt5PubRelImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubRelReasonCode reasonCode,
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
    public Mqtt5PubRelReasonCode getReasonCode() {
        return reasonCode;
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
