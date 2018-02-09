package org.mqttbee.mqtt5.message.subscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5MessageWrapper;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeInternal extends Mqtt5MessageWrapper<Mqtt5SubscribeInternal, Mqtt5SubscribeImpl> {

    public static final int DEFAULT_NO_SUBSCRIPTION_IDENTIFIER = -1;

    private final int packetIdentifier;
    private final int subscriptionIdentifier;

    Mqtt5SubscribeInternal(
            @NotNull final Mqtt5SubscribeImpl subscribe, final int packetIdentifier, final int subscriptionIdentifier) {

        super(subscribe, subscribe.getEncoder().wrap());
        this.packetIdentifier = packetIdentifier;
        this.subscriptionIdentifier = subscriptionIdentifier;
    }

    public int getPacketIdentifier() {
        return packetIdentifier;
    }

    public int getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    @Override
    protected Mqtt5SubscribeInternal getCodable() {
        return this;
    }

}
