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

package org.mqttbee.mqtt.handler.publish.incoming;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.ioc.ClientScope;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.util.collections.HandleList;
import org.mqttbee.util.collections.ImmutableIntList;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.function.Consumer;

import static org.mqttbee.mqtt.message.subscribe.MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

/**
 * single threaded, in channel eventloop
 *
 * @author Silvio Giebl
 */
@ClientScope
@NotThreadSafe
public class MqttIncomingPublishFlowsWithId extends MqttIncomingPublishFlows {

    private final @NotNull HashMap<Integer, MqttSubscriptionFlow> flowsWithIdsMap = new HashMap<>();
    private final @NotNull MqttSubscriptionFlows flowsWithIds;
    private final @NotNull Consumer<MqttSubscriptionFlow> flowWithIdUnsubscribedCallback = this::unsubscribed;

    @Inject
    MqttIncomingPublishFlowsWithId(
            final @NotNull MqttSubscriptionFlows flowsWithoutIds, final @NotNull MqttSubscriptionFlows flowsWithIds) {

        super(flowsWithoutIds);
        this.flowsWithIds = flowsWithIds;
    }

    @Override
    public void subscribe(
            final @NotNull MqttStatefulSubscribe subscribe, final @NotNull MqttSubAck subAck,
            final @Nullable MqttSubscriptionFlow flow) {

        if (flow != null) {
            final int subscriptionIdentifier = subscribe.getSubscriptionIdentifier();
            if (subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
                flowsWithIdsMap.put(subscriptionIdentifier, flow);
                flow.setSubscriptionIdentifier(subscriptionIdentifier);
            }
        }
        super.subscribe(subscribe, subAck, flow);
    }

    @Override
    void subscribe(final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscriptionFlow flow) {
        if ((flow != null) && (flow.getSubscriptionIdentifier() != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER)) {
            flowsWithIds.subscribe(topicFilter, flow);
        } else {
            super.subscribe(topicFilter, flow);
        }
    }

    @Override
    void unsubscribe(final @NotNull MqttTopicFilterImpl topicFilter) {
        flowsWithIds.unsubscribe(topicFilter, flowWithIdUnsubscribedCallback);
        super.unsubscribe(topicFilter);
    }

    private void unsubscribed(final @NotNull MqttSubscriptionFlow flow) {
        final int subscriptionIdentifier = flow.getSubscriptionIdentifier();
        if (subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            flowsWithIdsMap.remove(subscriptionIdentifier);
        }
    }

    @Override
    void cancel(final @NotNull MqttSubscriptionFlow flow) {
        final int subscriptionIdentifier = flow.getSubscriptionIdentifier();
        if (subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            flowsWithIdsMap.remove(subscriptionIdentifier);
            flowsWithIds.cancel(flow);
        } else {
            super.cancel(flow);
        }
    }

    @Override
    void findMatching(
            final @NotNull MqttStatefulPublish publish,
            final @NotNull HandleList<MqttIncomingPublishFlow> matchingFlows) {

        final ImmutableIntList subscriptionIdentifiers = publish.getSubscriptionIdentifiers();
        if (!subscriptionIdentifiers.isEmpty()) {
            for (int i = 0; i < subscriptionIdentifiers.size(); i++) {
                final int subscriptionIdentifier = subscriptionIdentifiers.get(i);
                final MqttSubscriptionFlow flow = flowsWithIdsMap.get(subscriptionIdentifier);
                if (flow != null) {
                    matchingFlows.add(flow);
                }
            }
        }
        super.findMatching(publish, matchingFlows);
    }

    @Override
    public void clear(final @NotNull Throwable cause) {
        flowsWithIdsMap.clear();
        flowsWithIds.clear(cause);
        super.clear(cause);
    }
}
