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
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.util.collections.HandleList;
import com.hivemq.client.internal.util.collections.HandleList.Handle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public class MqttSubscriptionFlowList implements MqttSubscriptionFlows {

    private final @NotNull HandleList<MqttSubscribedPublishFlow> flows;
    private final @NotNull HashMap<MqttTopicFilterImpl, Integer> subscribedTopicFilters;

    @Inject
    MqttSubscriptionFlowList() {
        flows = new HandleList<>();
        subscribedTopicFilters = new HashMap<>();
    }

    @Override
    public void subscribe(
            final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscribedPublishFlow flow) {

        if (flow != null) {
            final HandleList<MqttTopicFilterImpl> topicFilters = flow.getTopicFilters();
            if (topicFilters.isEmpty()) {
                flows.add(flow);
            }
            topicFilters.add(topicFilter);
        }
        final Integer count = subscribedTopicFilters.put(topicFilter, 1);
        if (count != null) {
            subscribedTopicFilters.put(topicFilter, count + 1);
        }
    }

    @Override
    public void remove(final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscribedPublishFlow flow) {
        if (flow != null) {
            final HandleList<MqttTopicFilterImpl> topicFilters = flow.getTopicFilters();
            for (Handle<MqttTopicFilterImpl> h = topicFilters.getFirst(); h != null; h = h.getNext()) {
                if (topicFilter.equals(h.getElement())) {
                    topicFilters.remove(h);
                    break;
                }
            }
            if (topicFilters.isEmpty()) {
                cancel(flow);
            }
        }
        final Integer count = subscribedTopicFilters.remove(topicFilter);
        if ((count != null) && (count > 1)) {
            subscribedTopicFilters.put(topicFilter, count - 1);
        }
    }

    @Override
    public void unsubscribe(
            final @NotNull MqttTopicFilterImpl topicFilter,
            final @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback) {

        for (Handle<MqttSubscribedPublishFlow> h = flows.getFirst(); h != null; h = h.getNext()) {
            final MqttSubscribedPublishFlow flow = h.getElement();
            final HandleList<MqttTopicFilterImpl> topicFilters = flow.getTopicFilters();
            for (Handle<MqttTopicFilterImpl> h2 = topicFilters.getFirst(); h2 != null; h2 = h2.getNext()) {
                if (topicFilter.equals(h2.getElement())) {
                    topicFilters.remove(h2);
                }
            }
            if (topicFilters.isEmpty()) {
                flows.remove(h);
                flow.onComplete();
                if (unsubscribedCallback != null) {
                    unsubscribedCallback.accept(flow);
                }
            }
        }
        subscribedTopicFilters.remove(topicFilter);
    }

    @Override
    public void cancel(final @NotNull MqttSubscribedPublishFlow flow) {
        for (Handle<MqttSubscribedPublishFlow> h = flows.getFirst(); h != null; h = h.getNext()) {
            if (h.getElement() == flow) {
                flows.remove(h);
                break;
            }
        }
    }

    @Override
    public void findMatching(
            final @NotNull MqttTopicImpl topic, final @NotNull MqttMatchingPublishFlows matchingFlows) {

        for (Handle<MqttSubscribedPublishFlow> h = flows.getFirst(); h != null; h = h.getNext()) {
            final MqttSubscribedPublishFlow flow = h.getElement();
            for (Handle<MqttTopicFilterImpl> h2 = flow.getTopicFilters().getFirst(); h2 != null; h2 = h2.getNext()) {
                if (h2.getElement().matches(topic)) {
                    matchingFlows.add(flow);
                    break;
                }
            }
        }
        if (!matchingFlows.isEmpty()) {
            matchingFlows.subscriptionFound = true;
            return;
        }
        for (final MqttTopicFilterImpl subscribedTopicFilter : subscribedTopicFilters.keySet()) {
            if (subscribedTopicFilter.matches(topic)) {
                matchingFlows.subscriptionFound = true;
                return;
            }
        }
    }

    @Override
    public void clear(final @NotNull Throwable cause) {
        for (Handle<MqttSubscribedPublishFlow> h = flows.getFirst(); h != null; h = h.getNext()) {
            h.getElement().onError(cause);
        }
        flows.clear();
        subscribedTopicFilters.clear();
    }
}
