package org.mqttbee.mqtt5.message.pubrel;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.pubrel.Mqtt5PubRel;
import org.mqttbee.api.mqtt5.message.pubrel.Mqtt5PubRelReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithReasonString;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRelImpl extends Mqtt5MessageWithReasonString<Mqtt5PubRelImpl> implements Mqtt5PubRel {

    @NotNull
    public static final Mqtt5PubRelReasonCode DEFAULT_REASON_CODE = Mqtt5PubRelReasonCode.SUCCESS;

    private final int packetIdentifier;
    private final Mqtt5PubRelReasonCode reasonCode;

    public Mqtt5PubRelImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubRelReasonCode reasonCode,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5PubRelImpl, ? extends Mqtt5MessageEncoder<Mqtt5PubRelImpl>> encoderProvider) {

        super(reasonString, userProperties, encoderProvider);
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
    protected Mqtt5PubRelImpl getCodable() {
        return this;
    }

}
