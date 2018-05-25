/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.mqtt.handler.publish;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.rx.SingleFlow;
import org.mqttbee.util.collections.ScNodeList;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttSubscriptionFlow extends MqttIncomingPublishFlow implements SingleFlow<Mqtt5SubAck> {

    private final Subscriber<? super Mqtt5SubscribeResult> subscriber;
    private final ScNodeList<MqttTopicFilterImpl> topicFilters;
    private int subscriptionIdentifier = MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

    MqttSubscriptionFlow(
            @NotNull final Subscriber<? super Mqtt5SubscribeResult> subscriber,
            @NotNull final MqttIncomingPublishService incomingPublishService) {

        super(incomingPublishService);
        this.subscriber = subscriber;
        this.topicFilters = new ScNodeList<>();
    }

    @NotNull
    @Override
    Subscriber<? super Mqtt5SubscribeResult> getSubscriber() {
        return subscriber;
    }

    @Override
    public void onSuccess(@NotNull final Mqtt5SubAck subAck) {
        if (done) {
            return;
        }
        subscriber.onNext(subAck);
        if (requested != Long.MAX_VALUE) {
            requested--;
        }
    }

    @Override
    void runRemoveOnCancel() {
        incomingPublishService.getIncomingPublishFlows().cancel(this);
    }

    @NotNull
    ScNodeList<MqttTopicFilterImpl> getTopicFilters() {
        return topicFilters;
    }

    int getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    void setSubscriptionIdentifier(final int subscriptionIdentifier) {
        this.subscriptionIdentifier = subscriptionIdentifier;
    }

}
