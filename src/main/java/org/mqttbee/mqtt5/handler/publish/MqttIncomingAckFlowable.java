package org.mqttbee.mqtt5.handler.publish;

import io.reactivex.Flowable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishResult;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttIncomingAckFlowable extends Flowable<MqttPublishResult> {

    @NotNull
    private final MqttOutgoingPublishService outgoingPublishService;
    @NotNull
    private final MqttPublishFlowables publishFlowables;
    @NotNull
    private final Flowable<MqttPublish> publishFlowable;

    public MqttIncomingAckFlowable(
            @NotNull final MqttOutgoingPublishService outgoingPublishService,
            @NotNull final MqttPublishFlowables publishFlowables,
            @NotNull final Flowable<MqttPublish> publishFlowable) {

        this.outgoingPublishService = outgoingPublishService;
        this.publishFlowables = publishFlowables;
        this.publishFlowable = publishFlowable;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super MqttPublishResult> s) {
        final MqttIncomingAckFlow incomingAckFlow = new MqttIncomingAckFlow(s, outgoingPublishService);
        s.onSubscribe(incomingAckFlow);
        publishFlowables.add(publishFlowable.map(publish -> new MqttPublishWithFlow(publish, incomingAckFlow)));
    }

}
