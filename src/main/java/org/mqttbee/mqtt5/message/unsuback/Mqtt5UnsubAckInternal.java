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

    @NotNull
    public Mqtt5UnsubAckImpl getUnsubAck() {
        return unsubAck;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public void setPacketIdentifier(final int packetIdentifier) {
        this.packetIdentifier = packetIdentifier;
    }

}
