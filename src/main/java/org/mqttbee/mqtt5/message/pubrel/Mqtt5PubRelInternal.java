package org.mqttbee.mqtt5.message.pubrel;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5PubRel;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRelInternal {

    private final Mqtt5PubRel pubRel;
    private int packetIdentifier;

    public Mqtt5PubRelInternal(@NotNull final Mqtt5PubRel pubRel) {
        this.pubRel = pubRel;
    }

}
