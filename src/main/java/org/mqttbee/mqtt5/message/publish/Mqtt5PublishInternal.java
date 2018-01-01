package org.mqttbee.mqtt5.message.publish;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PublishInternal {

    private final Mqtt5PublishImpl publish;
    private int packetIdentifier;
    private boolean isDup;
    private int[] subscriptionIdentifiers;

    public Mqtt5PublishInternal(@NotNull final Mqtt5PublishImpl publish) {
        this.publish = publish;
    }

}
