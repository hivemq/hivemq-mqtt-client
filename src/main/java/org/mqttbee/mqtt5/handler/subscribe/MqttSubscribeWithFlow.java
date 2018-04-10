package org.mqttbee.mqtt5.handler.subscribe;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.mqtt5.handler.publish.MqttSubscriptionFlow;

/**
 * @author Silvio Giebl
 */
public class MqttSubscribeWithFlow {

    private final MqttSubscribe subscribe;
    private final MqttSubscriptionFlow flow;

    public MqttSubscribeWithFlow(@NotNull final MqttSubscribe subscribe, @NotNull final MqttSubscriptionFlow flow) {
        this.subscribe = subscribe;
        this.flow = flow;
    }

    @NotNull
    public MqttSubscribe getSubscribe() {
        return subscribe;
    }

    @NotNull
    public MqttSubscriptionFlow getFlow() {
        return flow;
    }

    @NotNull
    public MqttSubscribeWrapperWithFlow wrap(final int packetIdentifier, final int subscriptionIdentifier) {
        return new MqttSubscribeWrapperWithFlow(subscribe.wrap(packetIdentifier, subscriptionIdentifier), flow);
    }


    public static class MqttSubscribeWrapperWithFlow {

        private final MqttSubscribeWrapper subscribe;
        private final MqttSubscriptionFlow flow;

        private MqttSubscribeWrapperWithFlow(
                @NotNull final MqttSubscribeWrapper subscribe, @NotNull final MqttSubscriptionFlow flow) {

            this.subscribe = subscribe;
            this.flow = flow;
        }

        @NotNull
        public MqttSubscribeWrapper getSubscribe() {
            return subscribe;
        }

        @NotNull
        public MqttSubscriptionFlow getFlow() {
            return flow;
        }

    }

}
