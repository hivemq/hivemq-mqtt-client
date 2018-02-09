package org.mqttbee.mqtt5.message.pubrec;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.pubrec.Mqtt5PubRec;
import org.mqttbee.api.mqtt5.message.pubrec.Mqtt5PubRecReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithReasonString;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecImpl extends Mqtt5MessageWithReasonString<Mqtt5PubRecImpl> implements Mqtt5PubRec {

    @NotNull
    public static final Mqtt5PubRecReasonCode DEFAULT_REASON_CODE = Mqtt5PubRecReasonCode.SUCCESS;

    private final int packetIdentifier;
    private final Mqtt5PubRecReasonCode reasonCode;

    public Mqtt5PubRecImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubRecReasonCode reasonCode,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5PubRecImpl, ? extends Mqtt5MessageEncoder<Mqtt5PubRecImpl>> encoderProvider) {

        super(reasonString, userProperties, encoderProvider);
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
    protected Mqtt5PubRecImpl getCodable() {
        return this;
    }

}
