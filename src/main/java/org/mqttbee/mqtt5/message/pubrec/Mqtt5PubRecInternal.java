package org.mqttbee.mqtt5.message.pubrec;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecInternal {

    private final Mqtt5PubRecImpl pubRec;
    private int packetIdentifier;

    public Mqtt5PubRecInternal(@NotNull final Mqtt5PubRecImpl pubRec) {
        this.pubRec = pubRec;
    }

    @NotNull
    public Mqtt5PubRecImpl getPubRec() {
        return pubRec;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public void setPacketIdentifier(final int packetIdentifier) {
        this.packetIdentifier = packetIdentifier;
    }

}
