package org.mqttbee.mqtt5.handler.publish;

import io.reactivex.Flowable;
import io.reactivex.internal.subscriptions.EmptySubscription;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt5.ioc.ChannelComponent;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttIncomingAckFlowable extends Flowable<Mqtt5PublishResult> {

    private final Flowable<MqttPublish> publishFlowable;
    private final MqttClientData clientData;

    public MqttIncomingAckFlowable(
            @NotNull final Flowable<MqttPublish> publishFlowable, @NotNull final MqttClientData clientData) {

        this.publishFlowable = publishFlowable;
        this.clientData = clientData;
    }

    @Override
    protected void subscribeActual(final Subscriber<? super Mqtt5PublishResult> s) {
        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData(); // TODO temp
        final MqttServerConnectionData serverConnectionData = clientData.getRawServerConnectionData(); // TODO temp
        if ((clientConnectionData == null) || (serverConnectionData == null)) {
            EmptySubscription.error(new NotConnectedException(), s);
        } else {
            final ChannelComponent channelComponent = ChannelComponent.get(clientConnectionData.getChannel());
            final MqttOutgoingPublishService outgoingPublishService = channelComponent.outgoingPublishService();
            final MqttPublishFlowables publishFlowables = channelComponent.publishFlowables();

            final MqttIncomingAckFlow incomingAckFlow = new MqttIncomingAckFlow(s, outgoingPublishService);
            s.onSubscribe(incomingAckFlow);
            publishFlowables.add(publishFlowable.map(publish -> new MqttPublishWithFlow(publish, incomingAckFlow)));
        }
    }

}
