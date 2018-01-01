package org.mqttbee.mqtt5.message.pubcomp;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5PubComp;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompInternal {

    private final Mqtt5PubComp pubComp;
    private int packetIdentifier;

    public Mqtt5PubCompInternal(@NotNull final Mqtt5PubComp pubComp) {
        this.pubComp = pubComp;
    }

}
