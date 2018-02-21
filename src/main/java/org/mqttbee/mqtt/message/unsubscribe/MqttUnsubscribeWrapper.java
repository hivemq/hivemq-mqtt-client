package org.mqttbee.mqtt.message.unsubscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessageWrapper.MqttMessageWrapperWithId;

/**
 * @author Silvio Giebl
 */
public class MqttUnsubscribeWrapper extends
        MqttMessageWrapperWithId<MqttUnsubscribeWrapper, MqttUnsubscribeImpl, MqttMessageEncoderProvider<MqttUnsubscribeWrapper>> {

    MqttUnsubscribeWrapper(@NotNull final MqttUnsubscribeImpl unsubscribe, final int packetIdentifier) {
        super(unsubscribe, packetIdentifier);
    }

    @NotNull
    @Override
    protected MqttUnsubscribeWrapper getCodable() {
        return this;
    }

}
