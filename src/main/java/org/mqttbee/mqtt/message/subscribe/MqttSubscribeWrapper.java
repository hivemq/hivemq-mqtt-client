package org.mqttbee.mqtt.message.subscribe;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessageWrapper.MqttMessageWrapperWithId;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSubscribeWrapper extends
        MqttMessageWrapperWithId<MqttSubscribeWrapper, MqttSubscribe, MqttMessageEncoderProvider<MqttSubscribeWrapper>> {

    public static final int DEFAULT_NO_SUBSCRIPTION_IDENTIFIER = -1;

    private final int subscriptionIdentifier;

    MqttSubscribeWrapper(
            @NotNull final MqttSubscribe subscribe, final int packetIdentifier, final int subscriptionIdentifier) {

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
