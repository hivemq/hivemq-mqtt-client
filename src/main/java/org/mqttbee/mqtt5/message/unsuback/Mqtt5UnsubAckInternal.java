package org.mqttbee.mqtt5.message.unsuback;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UnsubAckInternal {

    private final Mqtt5UnsubAckImpl unsubAck;
    private int packetIdentifier;

    public Mqtt5UnsubAckInternal(@NotNull final Mqtt5UnsubAckImpl unsubAck) {
        this.unsubAck = unsubAck;
    }

}
