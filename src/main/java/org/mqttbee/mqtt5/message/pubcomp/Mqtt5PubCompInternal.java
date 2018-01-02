package org.mqttbee.mqtt5.message.pubcomp;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompInternal {

    private final Mqtt5PubCompImpl pubComp;
    private int packetIdentifier;

    public Mqtt5PubCompInternal(@NotNull final Mqtt5PubCompImpl pubComp) {
        this.pubComp = pubComp;
    }

    @NotNull
    public Mqtt5PubCompImpl getPubComp() {
        return pubComp;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public void setPacketIdentifier(final int packetIdentifier) {
        this.packetIdentifier = packetIdentifier;
    }

}
