package org.mqttbee.mqtt.message.subscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessageWrapper.Mqtt5MessageWrapperWithId;

/**
 * @author Silvio Giebl
 */
public class MqttSubscribeWrapper extends
        Mqtt5MessageWrapperWithId<MqttSubscribeWrapper, MqttSubscribeImpl, MqttMessageEncoderProvider<MqttSubscribeWrapper>> {

    public static final int DEFAULT_NO_SUBSCRIPTION_IDENTIFIER = -1;

    private final int subscriptionIdentifier;

    MqttSubscribeWrapper(
            @NotNull final MqttSubscribeImpl subscribe, final int packetIdentifier, final int subscriptionIdentifier) {

        super(subscribe, packetIdentifier);
        this.subscriptionIdentifier = subscriptionIdentifier;
    }

    public int getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    @NotNull
    @Override
    protected MqttSubscribeWrapper getCodable() {
        return this;
    }

}
