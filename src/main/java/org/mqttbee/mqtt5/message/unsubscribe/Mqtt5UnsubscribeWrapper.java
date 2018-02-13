package org.mqttbee.mqtt5.message.unsubscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage.Mqtt5MessageWrapper;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeWrapper extends Mqtt5MessageWrapper<Mqtt5UnsubscribeWrapper, Mqtt5UnsubscribeImpl> {

    private final int packetIdentifier;

    Mqtt5UnsubscribeWrapper(@NotNull final Mqtt5UnsubscribeImpl unsubscribe, final int packetIdentifier) {
        super(unsubscribe);
        this.packetIdentifier = packetIdentifier;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    @Override
    protected Mqtt5UnsubscribeWrapper getCodable() {
        return this;
    }

}
