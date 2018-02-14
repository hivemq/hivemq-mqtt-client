package org.mqttbee.mqtt5.message.suback;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt5.message.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5Message.Mqtt5MessageWithIdAndReasonCodes;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubAckImpl extends Mqtt5MessageWithIdAndReasonCodes<Mqtt5SubAckImpl, Mqtt5SubAckReasonCode>
        implements Mqtt5SubAck {

    public Mqtt5SubAckImpl(
            final int packetIdentifier, @NotNull final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes,
            @Nullable final Mqtt5UTF8StringImpl reasonString, @NotNull final Mqtt5UserPropertiesImpl userProperties) {

        super(packetIdentifier, reasonCodes, reasonString, userProperties, null);
    }

    @Override
    protected Mqtt5SubAckImpl getCodable() {
        return this;
    }

}
