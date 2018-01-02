package org.mqttbee.mqtt5.message.pubrel;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRelInternal {

    private final Mqtt5PubRelImpl pubRel;
    private int packetIdentifier;

    public Mqtt5PubRelInternal(@NotNull final Mqtt5PubRelImpl pubRel) {
        this.pubRel = pubRel;
    }

    @NotNull
    public Mqtt5PubRelImpl getPubRel() {
        return pubRel;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public void setPacketIdentifier(final int packetIdentifier) {
        this.packetIdentifier = packetIdentifier;
    }

}
