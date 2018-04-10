package org.mqttbee.mqtt5.handler.publish;

import io.reactivex.Flowable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeResult;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttSubscriptionFlowable extends Flowable<MqttSubscribeResult> {

    private final MqttSubscribe subscribe;
    private final MqttIncomingPublishService incomingPublishService;

    public MqttSubscriptionFlowable(
            @NotNull final MqttSubscribe subscribe, @NotNull final MqttIncomingPublishService incomingPublishService) {

        this.subscribe = subscribe;
        this.incomingPublishService = incomingPublishService;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super MqttSubscribeResult> s) {
        final MqttSubscriptionFlow flow = new MqttSubscriptionFlow(s, incomingPublishService);
        s.onSubscribe(flow);
        // TODO trigger send(subscribe, flow)
    }

}
