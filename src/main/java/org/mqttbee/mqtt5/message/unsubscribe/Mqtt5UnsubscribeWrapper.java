package org.mqttbee.mqtt5.message.unsubscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage.Mqtt5MessageWrapperWithPacketId;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeWrapper
        extends Mqtt5MessageWrapperWithPacketId<Mqtt5UnsubscribeWrapper, Mqtt5UnsubscribeImpl> {

    Mqtt5UnsubscribeWrapper(@NotNull final Mqtt5UnsubscribeImpl unsubscribe, final int packetIdentifier) {
        super(unsubscribe, packetIdentifier);
    }

    @Override
    protected Mqtt5UnsubscribeWrapper getCodable() {
        return this;
    }

}
