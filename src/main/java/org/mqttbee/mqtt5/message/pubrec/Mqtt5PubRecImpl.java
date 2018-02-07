package org.mqttbee.mqtt5.message.pubrec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.pubrec.Mqtt5PubRec;
import org.mqttbee.api.mqtt5.message.pubrec.Mqtt5PubRecReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5PubRecEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecImpl extends Mqtt5Message.Mqtt5MessageWithOmissibleProperties implements Mqtt5PubRec {

    @NotNull
    public static final Mqtt5PubRecReasonCode DEFAULT_REASON_CODE = Mqtt5PubRecReasonCode.SUCCESS;

    private final int packetIdentifier;
    private final Mqtt5PubRecReasonCode reasonCode;

    public Mqtt5PubRecImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubRecReasonCode reasonCode,
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
    public Mqtt5PubRecReasonCode getReasonCode() {
        return reasonCode;
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
