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

    @NotNull
    public Mqtt5PublishImpl getPublish() {
        return publish;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public void setPacketIdentifier(final int packetIdentifier) {
        this.packetIdentifier = packetIdentifier;
    }

    public boolean isDup() {
        return isDup;
    }

    public void setDup(final boolean dup) {
        isDup = dup;
    }

    public int[] getSubscriptionIdentifiers() {
        return subscriptionIdentifiers;
    }

    public void setSubscriptionIdentifiers(final int[] subscriptionIdentifiers) {
        this.subscriptionIdentifiers = subscriptionIdentifiers;
    }

}
