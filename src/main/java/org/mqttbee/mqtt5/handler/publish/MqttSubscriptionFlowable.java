package org.mqttbee.mqtt5.handler.publish;

import io.reactivex.Flowable;
import io.reactivex.internal.subscriptions.EmptySubscription;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt5.handler.subscribe.MqttSubscribeWithFlow;
import org.mqttbee.mqtt5.handler.subscribe.MqttSubscriptionHandler;
import org.mqttbee.mqtt5.ioc.ChannelComponent;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttSubscriptionFlowable extends Flowable<Mqtt5SubscribeResult> {

    private final MqttSubscribe subscribe;
    private final MqttClientData clientData;

    public MqttSubscriptionFlowable(@NotNull final MqttSubscribe subscribe, @NotNull final MqttClientData clientData) {
        this.subscribe = subscribe;
        this.clientData = clientData;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super Mqtt5SubscribeResult> s) {
        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData(); // TODO temp
        if (clientConnectionData == null) {
            EmptySubscription.error(new NotConnectedException(), s);
        } else {
            final ChannelComponent channelComponent = ChannelComponent.get(clientConnectionData.getChannel());
            final MqttIncomingPublishService incomingPublishService = channelComponent.incomingPublishService();
            final MqttSubscriptionHandler subscriptionHandler = channelComponent.subscriptionHandler();

            final MqttSubscriptionFlow flow = new MqttSubscriptionFlow(s, incomingPublishService);
            s.onSubscribe(flow);
            subscriptionHandler.subscribe(new MqttSubscribeWithFlow(subscribe, flow)); // TODO temp
        }
    }

}
