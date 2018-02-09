package org.mqttbee.mqtt5.message.pubcomp;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.pubcomp.Mqtt5PubComp;
import org.mqttbee.api.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithReasonString;
import org.mqttbee.mqtt5.message.Mqtt5MessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompImpl extends Mqtt5MessageWithReasonString<Mqtt5PubCompImpl> implements Mqtt5PubComp {

    @NotNull
    public static final Mqtt5PubCompReasonCode DEFAULT_REASON_CODE = Mqtt5PubCompReasonCode.SUCCESS;

    private final int packetIdentifier;
    private final Mqtt5PubCompReasonCode reasonCode;

    public Mqtt5PubCompImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubCompReasonCode reasonCode,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5PubCompImpl, ? extends Mqtt5MessageEncoder<Mqtt5PubCompImpl>> encoderProvider) {

        super(reasonString, userProperties, encoderProvider);
        this.packetIdentifier = packetIdentifier;
        this.reasonCode = reasonCode;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    @NotNull
    @Override
    public Mqtt5PubCompReasonCode getReasonCode() {
        return reasonCode;
    }

    @Override
    protected Mqtt5PubCompImpl getCodable() {
        return this;
    }

}
