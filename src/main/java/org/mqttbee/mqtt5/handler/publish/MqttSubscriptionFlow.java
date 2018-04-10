package org.mqttbee.mqtt5.handler.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeResult;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.util.collections.ScNodeList;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttSubscriptionFlow extends MqttIncomingPublishFlow {

    private final ScNodeList<MqttTopicFilterImpl> topicFilters;
    private int subscriptionIdentifier = MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

    public MqttSubscriptionFlow(
            @NotNull final Subscriber<? super MqttSubscribeResult> actual,
            @NotNull final MqttIncomingPublishService incomingPublishService) {

        super(actual, incomingPublishService);
        this.topicFilters = new ScNodeList<>();
    }

    @Override
    void runRemoveOnCancel() {
        incomingPublishService.getIncomingPublishFlows().cancel(this);
    }

    @NotNull
    public ScNodeList<MqttTopicFilterImpl> getTopicFilters() {
        return topicFilters;
    }

    public int getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    public void setSubscriptionIdentifier(final int subscriptionIdentifier) {
        this.subscriptionIdentifier = subscriptionIdentifier;
    }

}
