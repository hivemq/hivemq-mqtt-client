package org.mqttbee.mqtt5.message.pubrec;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5PubRec;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecInternal {

    private final Mqtt5PubRec pubRec;
    private int packetIdentifier;

    public Mqtt5PubRecInternal(@NotNull final Mqtt5PubRec pubRec) {
        this.pubRec = pubRec;
    }

}
