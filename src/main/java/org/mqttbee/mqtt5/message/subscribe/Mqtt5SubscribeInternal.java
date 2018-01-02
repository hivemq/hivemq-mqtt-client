package org.mqttbee.mqtt5.message.subscribe;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeInternal {

    private final Mqtt5SubscribeImpl subscribe;
    private int packetIdentfier;
    private int subscriptionIdentifier;

    public Mqtt5SubscribeInternal(@NotNull final Mqtt5SubscribeImpl subscribe) {
        this.subscribe = subscribe;
    }

    @NotNull
    public Mqtt5SubscribeImpl getSubscribe() {
        return subscribe;
    }

    public int getPacketIdentfier() {
        return packetIdentfier;
    }

    public void setPacketIdentfier(final int packetIdentfier) {
        this.packetIdentfier = packetIdentfier;
    }

    public int getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    public void setSubscriptionIdentifier(final int subscriptionIdentifier) {
        this.subscriptionIdentifier = subscriptionIdentifier;
    }

}
