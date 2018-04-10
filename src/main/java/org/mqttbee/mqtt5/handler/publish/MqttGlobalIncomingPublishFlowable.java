package org.mqttbee.mqtt5.handler.publish;

import io.reactivex.Flowable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeResult;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttGlobalIncomingPublishFlowable extends Flowable<MqttSubscribeResult> {

    private final MqttIncomingPublishService incomingPublishService;
    private final int type;

    public MqttGlobalIncomingPublishFlowable(
            @NotNull final MqttIncomingPublishService incomingPublishService, final int type) {

        this.incomingPublishService = incomingPublishService;
        this.type = type;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super MqttSubscribeResult> s) {
        final MqttGlobalIncomingPublishFlow flow = new MqttGlobalIncomingPublishFlow(s, incomingPublishService, type);
        incomingPublishService.getNettyEventLoop()
                .execute(() -> incomingPublishService.getIncomingPublishFlows().subscribeGlobal(flow));
        s.onSubscribe(flow);
    }

}
