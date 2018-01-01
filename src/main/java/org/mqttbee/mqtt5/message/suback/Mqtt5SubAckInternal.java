package org.mqttbee.mqtt5.message.suback;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubAckInternal {

    private final Mqtt5SubAckImpl subAck;
    private int packetIdentifier;

    public Mqtt5SubAckInternal(@NotNull final Mqtt5SubAckImpl subAck) {
        this.subAck = subAck;
    }

}
