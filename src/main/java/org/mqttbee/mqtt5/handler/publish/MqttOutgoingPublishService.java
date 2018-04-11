package org.mqttbee.mqtt5.handler.publish;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Scheduler;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.reactivestreams.Subscription;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttOutgoingPublishService implements FlowableSubscriber<MqttPublishWithFlow> {

    private final Scheduler.Worker rxEventLoop;

    private Subscription subscription;

    private final ConcurrentHashMap<Integer, MqttPublishWithFlow> map;
    private final int receiveMaximum;


    @Inject
    MqttOutgoingPublishService(
            final MqttPublishFlowables publishFlowables, @Named("outgoingPublish") final Scheduler.Worker rxEventLoop,
            final MqttClientData clientData) {

        final MqttServerConnectionData serverConnectionData = clientData.getRawServerConnectionData();
        assert serverConnectionData != null;

        this.rxEventLoop = rxEventLoop;

        receiveMaximum = serverConnectionData.getReceiveMaximum();
        map = new ConcurrentHashMap<>();

        Flowable.mergeDelayError(publishFlowables).subscribe(this);
    }

    @Override
    public void onSubscribe(final Subscription s) {
        subscription = s;
        s.request(receiveMaximum);
    }

    @Override
    public void onNext(final MqttPublishWithFlow publishWithFlow) {
//        map.put(packetIdentifier, publishWithFlow); // TODO
    }

    @Override
    public void onError(final Throwable t) {
        // TODO must not happen if operator onErrorComplete is added
    }

    @Override
    public void onComplete() {
        // TODO does not happen as the flowable is global and never completed
    }

    void onPubAck(@NotNull final MqttPubAck pubAck) {
    }

    void onPubComp(@NotNull final MqttPubComp pubComp) {
    }


    public void request(final long amount) {
        subscription.request(amount);
    }

    @NotNull
    public Scheduler.Worker getRxEventLoop() {
        return rxEventLoop;
    }

}
