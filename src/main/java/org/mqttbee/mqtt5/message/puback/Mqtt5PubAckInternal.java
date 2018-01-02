package org.mqttbee.mqtt5.message.puback;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckInternal {

    private final Mqtt5PubAckImpl pubAck;
    private int packetIdentifier;

    public Mqtt5PubAckInternal(@NotNull final Mqtt5PubAckImpl pubAck) {
        this.pubAck = pubAck;
    }

    @NotNull
    public Mqtt5PubAckImpl getPubAck() {
        return pubAck;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public void setPacketIdentifier(final int packetIdentifier) {
        this.packetIdentifier = packetIdentifier;
    }

}
