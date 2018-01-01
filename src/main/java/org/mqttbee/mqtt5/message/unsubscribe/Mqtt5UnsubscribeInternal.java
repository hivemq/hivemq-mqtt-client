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

}
