package org.mqttbee.mqtt5.message.unsuback;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.unsuback.Mqtt5UnsubAck;
import org.mqttbee.api.mqtt5.message.unsuback.Mqtt5UnsubAckReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithReasonString;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubAckImpl extends Mqtt5MessageWithReasonString<Mqtt5UnsubAckImpl> implements Mqtt5UnsubAck {

    private final int packetIdentifier;
    private final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes;

    public Mqtt5UnsubAckImpl(
            final int packetIdentifier, @NotNull final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties) {

        super(reasonString, userProperties, null);
        this.packetIdentifier = packetIdentifier;
        this.reasonCodes = reasonCodes;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    @NotNull
    @Override
    public ImmutableList<Mqtt5UnsubAckReasonCode> getReasonCodes() {
        return reasonCodes;
    }

    @Override
    protected Mqtt5UnsubAckImpl getCodable() {
        return this;
    }

}
