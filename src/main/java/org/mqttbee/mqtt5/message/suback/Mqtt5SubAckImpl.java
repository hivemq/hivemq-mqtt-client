package org.mqttbee.mqtt5.message.suback;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt5.message.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithReasonString;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubAckImpl extends Mqtt5MessageWithReasonString<Mqtt5SubAckImpl> implements Mqtt5SubAck {

    private final int packetIdentifier;
    private final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes;

    public Mqtt5SubAckImpl(
            final int packetIdentifier, @NotNull final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes,
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
    public ImmutableList<Mqtt5SubAckReasonCode> getReasonCodes() {
        return reasonCodes;
    }

    @Override
    protected Mqtt5SubAckImpl getCodable() {
        return this;
    }

}
