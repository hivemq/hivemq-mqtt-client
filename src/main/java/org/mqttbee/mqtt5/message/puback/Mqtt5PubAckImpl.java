package org.mqttbee.mqtt5.message.puback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.puback.Mqtt5PubAck;
import org.mqttbee.api.mqtt5.message.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoder;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithReasonString;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckImpl extends Mqtt5MessageWithReasonString<Mqtt5PubAckImpl> implements Mqtt5PubAck {

    @NotNull
    public static final Mqtt5PubAckReasonCode DEFAULT_REASON_CODE = Mqtt5PubAckReasonCode.SUCCESS;

    private final int packetIdentifier;
    private final Mqtt5PubAckReasonCode reasonCode;

    public Mqtt5PubAckImpl(
            final int packetIdentifier, @NotNull final Mqtt5PubAckReasonCode reasonCode,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties,
            @NotNull final Function<Mqtt5PubAckImpl, ? extends Mqtt5MessageEncoder<Mqtt5PubAckImpl>> encoderProvider) {

        super(reasonString, userProperties, encoderProvider);
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
    protected Mqtt5PubAckImpl getCodable() {
        return this;
    }

}
