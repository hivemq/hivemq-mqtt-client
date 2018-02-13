package org.mqttbee.mqtt5.message.subscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.message.Mqtt5WrappedMessage.Mqtt5MessageWrapperWithPacketId;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SubscribeWrapper extends Mqtt5MessageWrapperWithPacketId<Mqtt5SubscribeWrapper, Mqtt5SubscribeImpl> {

    public static final int DEFAULT_NO_SUBSCRIPTION_IDENTIFIER = -1;

    private final int subscriptionIdentifier;

    Mqtt5SubscribeWrapper(
            @NotNull final Mqtt5SubscribeImpl subscribe, final int packetIdentifier, final int subscriptionIdentifier) {

        super(subscribe, packetIdentifier);
        this.subscriptionIdentifier = subscriptionIdentifier;
    }

    public int getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    @Override
    protected Mqtt5SubscribeWrapper getCodable() {
        return this;
    }

}
