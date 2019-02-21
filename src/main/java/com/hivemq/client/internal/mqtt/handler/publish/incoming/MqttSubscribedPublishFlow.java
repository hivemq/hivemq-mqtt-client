/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.handler.publish.incoming;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.handler.subscribe.MqttSubscriptionFlow;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttStatefulSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.suback.MqttSubAck;
import com.hivemq.client.internal.util.collections.HandleList;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.rx.FlowableWithSingleSubscriber;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Subscriber;

/**
 * @author Silvio Giebl
 */
public class MqttSubscribedPublishFlow extends MqttIncomingPublishFlow implements MqttSubscriptionFlow<MqttSubAck> {

    private final @NotNull HandleList<MqttTopicFilterImpl> topicFilters;
    private int subscriptionIdentifier = MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

    MqttSubscribedPublishFlow(
            final @NotNull Subscriber<? super Mqtt5Publish> subscriber, final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttIncomingQosHandler incomingQosHandler) {

        super(subscriber, clientConfig, incomingQosHandler);
        topicFilters = new HandleList<>();
    }

    @Override
    public void onSuccess(final @NotNull MqttSubAck subAck) {
        if (subscriber instanceof FlowableWithSingleSubscriber) {
            //noinspection unchecked
            ((FlowableWithSingleSubscriber<? super Mqtt5Publish, ? super Mqtt5SubAck>) subscriber).onSingle(subAck);
        }
    }

    @Override
    void runCancel() {
        incomingQosHandler.getIncomingPublishFlows().cancel(this);
        super.runCancel();
    }

    @NotNull HandleList<MqttTopicFilterImpl> getTopicFilters() {
        return topicFilters;
    }

    int getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }

    void setSubscriptionIdentifier(final int subscriptionIdentifier) {
        this.subscriptionIdentifier = subscriptionIdentifier;
    }
}
