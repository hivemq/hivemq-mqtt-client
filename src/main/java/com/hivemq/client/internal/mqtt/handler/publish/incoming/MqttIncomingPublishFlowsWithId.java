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

import com.hivemq.client.internal.annotations.NotThreadSafe;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.ioc.ClientScope;
import com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttStatefulSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.suback.MqttSubAck;
import com.hivemq.client.internal.util.collections.ImmutableIntList;
import com.hivemq.client.internal.util.collections.IntIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import static com.hivemq.client.internal.mqtt.message.subscribe.MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

/**
 * @author Silvio Giebl
 */
@ClientScope
@NotThreadSafe
public class MqttIncomingPublishFlowsWithId extends MqttIncomingPublishFlows {

    private static final @NotNull IntIndex.Spec<MqttSubscribedPublishFlow> INDEX_SPEC =
            new IntIndex.Spec<>(MqttSubscribedPublishFlow::getSubscriptionIdentifier);

    private final @NotNull IntIndex<MqttSubscribedPublishFlow> flowsWithIdsIndex = new IntIndex<>(INDEX_SPEC);
    private final @NotNull MqttSubscriptionFlows flowsWithIds;

    @Inject
    MqttIncomingPublishFlowsWithId(
            final @NotNull MqttSubscriptionFlows flowsWithoutIds, final @NotNull MqttSubscriptionFlows flowsWithIds) {

        super(flowsWithoutIds);
        this.flowsWithIds = flowsWithIds;
    }

    @Override
    public void subscribe(
            final @NotNull MqttStatefulSubscribe subscribe, final @Nullable MqttSubscribedPublishFlow flow) {

        if (flow != null) {
            final int subscriptionIdentifier = subscribe.getSubscriptionIdentifier();
            if (subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
                flow.setSubscriptionIdentifier(subscriptionIdentifier);
                flowsWithIdsIndex.put(flow);
            }
        }
        super.subscribe(subscribe, flow);
    }

    @Override
    void subscribe(final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscribedPublishFlow flow) {
        if ((flow != null) && (flow.getSubscriptionIdentifier() != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER)) {
            flowsWithIds.subscribe(topicFilter, flow);
        } else {
            super.subscribe(topicFilter, flow);
        }
    }

    @Override
    public void subAck(
            final @NotNull MqttStatefulSubscribe subscribe, final @NotNull MqttSubAck subAck,
            final @Nullable MqttSubscribedPublishFlow flow) {

        super.subAck(subscribe, subAck, flow);
        if (flow != null) {
            final int subscriptionIdentifier = subscribe.getSubscriptionIdentifier();
            if ((subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) && flow.getTopicFilters().isEmpty()) {
                flowsWithIdsIndex.remove(subscriptionIdentifier);
            }
        }
    }

    @Override
    void remove(final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscribedPublishFlow flow) {
        if ((flow != null) && (flow.getSubscriptionIdentifier() != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER)) {
            flowsWithIds.remove(topicFilter, flow);
        } else {
            super.remove(topicFilter, flow);
        }
    }

    @Override
    void unsubscribe(final @NotNull MqttTopicFilterImpl topicFilter) {
        flowsWithIds.unsubscribe(topicFilter, this::unsubscribed);
        super.unsubscribe(topicFilter);
    }

    private void unsubscribed(final @NotNull MqttSubscribedPublishFlow flow) {
        flowsWithIdsIndex.remove(flow.getSubscriptionIdentifier());
    }

    @Override
    void cancel(final @NotNull MqttSubscribedPublishFlow flow) {
        final int subscriptionIdentifier = flow.getSubscriptionIdentifier();
        if (subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            flowsWithIdsIndex.remove(subscriptionIdentifier);
            flowsWithIds.cancel(flow);
        } else {
            super.cancel(flow);
        }
    }

    @Override
    void findMatching(
            final @NotNull MqttStatefulPublish publish, final @NotNull MqttMatchingPublishFlows matchingFlows) {

        final ImmutableIntList subscriptionIdentifiers = publish.getSubscriptionIdentifiers();
        if (!subscriptionIdentifiers.isEmpty()) {
            for (int i = 0; i < subscriptionIdentifiers.size(); i++) {
                final MqttSubscribedPublishFlow flow = flowsWithIdsIndex.get(subscriptionIdentifiers.get(i));
                if (flow != null) {
                    matchingFlows.add(flow);
                }
            }
            if (matchingFlows.isEmpty()) {
                flowsWithIds.findMatching(publish.stateless().getTopic(), matchingFlows);
            } else {
                matchingFlows.subscriptionFound = true;
            }
        }
        super.findMatching(publish, matchingFlows);
    }

    @Override
    public void clear(final @NotNull Throwable cause) {
        flowsWithIdsIndex.clear();
        flowsWithIds.clear(cause);
        super.clear(cause);
    }
}
