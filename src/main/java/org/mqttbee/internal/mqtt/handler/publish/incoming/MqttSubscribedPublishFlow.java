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

package org.mqttbee.internal.mqtt.handler.publish.incoming;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.internal.mqtt.handler.subscribe.MqttSubscriptionFlow;
import org.mqttbee.internal.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.internal.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.internal.util.collections.HandleList;
import org.mqttbee.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.rx.FlowableWithSingleSubscriber;
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
