package org.mqttbee.mqtt5.message.unsubscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5MessageWrapper;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeInternal extends Mqtt5MessageWrapper<Mqtt5UnsubscribeInternal, Mqtt5UnsubscribeImpl> {

    private final int packetIdentifier;

    Mqtt5UnsubscribeInternal(@NotNull final Mqtt5UnsubscribeImpl unsubscribe, final int packetIdentifier) {
        super(unsubscribe, unsubscribe.getEncoder().wrap());
        this.packetIdentifier = packetIdentifier;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    @Override
    protected Mqtt5UnsubscribeInternal getCodable() {
        return this;
    }

}
