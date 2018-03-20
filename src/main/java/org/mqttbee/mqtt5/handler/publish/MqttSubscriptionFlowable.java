package org.mqttbee.mqtt5.handler.publish;

import com.google.common.collect.ImmutableList;
import io.reactivex.Flowable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeResult;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
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
        final MqttSubscriptionFlow flow = new MqttSubscriptionFlow(s, getTopicFilters(), incomingPublishService);
        incomingPublishService.onSubscribe(flow);
        s.onSubscribe(flow);
        // TODO trigger send(subscribe, flow)
    }

    @NotNull
    private ImmutableList<MqttTopicFilter> getTopicFilters() {
        final ImmutableList<MqttSubscription> subscriptions = subscribe.getSubscriptions();
        final ImmutableList.Builder<MqttTopicFilter> topicFilters =
                ImmutableList.builderWithExpectedSize(subscriptions.size());
        for (final MqttSubscription subscription : subscriptions) {
            topicFilters.add(subscription.getTopicFilter());
        }
        return topicFilters.build();
    }

}
