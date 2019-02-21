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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Iterator;
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
            for (final Iterator<MqttTopicFilterImpl> iterator = topicFilters.iterator(); iterator.hasNext(); ) {
                if (topicFilter.equals(iterator.next())) {
                    iterator.remove();
                    break;
                }
            }
            if (topicFilters.isEmpty()) {
                for (final Iterator<MqttSubscribedPublishFlow> iterator = flows.iterator(); iterator.hasNext(); ) {
                    if (iterator.next() == flow) {
                        iterator.remove();
                        break;
                    }
                }
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

        for (final Iterator<MqttSubscribedPublishFlow> flowIt = flows.iterator(); flowIt.hasNext(); ) {
            final MqttSubscribedPublishFlow flow = flowIt.next();
            final HandleList<MqttTopicFilterImpl> flowTopicFilters = flow.getTopicFilters();
            for (final Iterator<MqttTopicFilterImpl> iterator = flowTopicFilters.iterator(); iterator.hasNext(); ) {
                if (topicFilter.equals(iterator.next())) {
                    iterator.remove();
                }
            }
            if (flowTopicFilters.isEmpty()) {
                flowIt.remove();
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
        for (final Iterator<MqttSubscribedPublishFlow> iterator = flows.iterator(); iterator.hasNext(); ) {
            if (iterator.next() == flow) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public boolean findMatching(
            final @NotNull MqttTopicImpl topic, final @NotNull HandleList<MqttIncomingPublishFlow> matchingFlows) {

        for (final MqttSubscribedPublishFlow flow : flows) {
            for (final MqttTopicFilterImpl topicFilter : flow.getTopicFilters()) {
                if (topicFilter.matches(topic)) {
                    matchingFlows.add(flow);
                    break;
                }
            }
        }
        if (!matchingFlows.isEmpty()) {
            return true;
        }
        for (final MqttTopicFilterImpl subscribedTopicFilter : subscribedTopicFilters.keySet()) {
            if (subscribedTopicFilter.matches(topic)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear(final @NotNull Throwable cause) {
        for (final Iterator<MqttSubscribedPublishFlow> iterator = flows.iterator(); iterator.hasNext(); ) {
            iterator.next().onError(cause);
            iterator.remove();
        }
        subscribedTopicFilters.clear();
    }
}
