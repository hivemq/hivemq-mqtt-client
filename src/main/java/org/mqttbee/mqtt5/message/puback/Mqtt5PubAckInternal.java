package org.mqttbee.mqtt5.message.puback;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5PubAck;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckInternal {

    private final Mqtt5PubAck pubAck;
    private int packetIdentifier;

    public Mqtt5PubAckInternal(@NotNull final Mqtt5PubAck pubAck) {
        this.pubAck = pubAck;
    }

}
