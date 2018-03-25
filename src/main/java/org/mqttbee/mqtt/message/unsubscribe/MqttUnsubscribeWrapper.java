package org.mqttbee.mqtt.message.unsubscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.MqttMessageWrapper.MqttMessageWrapperWithId;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttUnsubscribeWrapper extends
        MqttMessageWrapperWithId<MqttUnsubscribeWrapper, MqttUnsubscribe, MqttMessageEncoderProvider<MqttUnsubscribeWrapper>> {

    MqttUnsubscribeWrapper(@NotNull final MqttUnsubscribe unsubscribe, final int packetIdentifier) {
        super(unsubscribe, packetIdentifier);
    }

    @NotNull
    @Override
    protected MqttUnsubscribeWrapper getCodable() {
        return this;
    }

}
