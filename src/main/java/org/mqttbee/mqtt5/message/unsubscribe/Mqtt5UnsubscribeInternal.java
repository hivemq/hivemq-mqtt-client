package org.mqttbee.mqtt5.message.unsubscribe;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubscribeInternal {

    private final Mqtt5UnsubscribeImpl unsubscribe;
    private int packetIdentifier;

    public Mqtt5UnsubscribeInternal(@NotNull final Mqtt5UnsubscribeImpl unsubscribe) {
        this.unsubscribe = unsubscribe;
    }

    @NotNull
    public Mqtt5UnsubscribeImpl getUnsubscribe() {
        return unsubscribe;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public void setPacketIdentifier(final int packetIdentifier) {
        this.packetIdentifier = packetIdentifier;
    }

}
