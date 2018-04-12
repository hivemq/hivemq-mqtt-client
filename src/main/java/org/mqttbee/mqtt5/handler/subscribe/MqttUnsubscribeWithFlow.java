package org.mqttbee.mqtt5.handler.subscribe;

import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribeWrapper;

/**
 * @author Silvio Giebl
 */
public class MqttUnsubscribeWithFlow {

    private final MqttUnsubscribe unsubscribe;
    private final SingleEmitter<Mqtt5UnsubAck> flow;

    public MqttUnsubscribeWithFlow(
            @NotNull final MqttUnsubscribe unsubscribe, @NotNull final SingleEmitter<Mqtt5UnsubAck> flow) {

        this.unsubscribe = unsubscribe;
        this.flow = flow;
    }

    @NotNull
    public MqttUnsubscribe getUnsubscribe() {
        return unsubscribe;
    }

    @NotNull
    public SingleEmitter<Mqtt5UnsubAck> getFlow() {
        return flow;
    }

    @NotNull
    public MqttUnsubscribeWrapperWithFlow wrap(final int packetIdentifier) {
        return new MqttUnsubscribeWrapperWithFlow(unsubscribe.wrap(packetIdentifier), flow);
    }


    public static class MqttUnsubscribeWrapperWithFlow {

        private final MqttUnsubscribeWrapper unsubscribe;
        private final SingleEmitter<Mqtt5UnsubAck> flow;

        private MqttUnsubscribeWrapperWithFlow(
                @NotNull final MqttUnsubscribeWrapper unsubscribe, @NotNull final SingleEmitter<Mqtt5UnsubAck> flow) {

            this.unsubscribe = unsubscribe;
            this.flow = flow;
        }

        @NotNull
        public MqttUnsubscribeWrapper getUnsubscribe() {
            return unsubscribe;
        }

        @NotNull
        public SingleEmitter<Mqtt5UnsubAck> getFlow() {
            return flow;
        }

    }

}
