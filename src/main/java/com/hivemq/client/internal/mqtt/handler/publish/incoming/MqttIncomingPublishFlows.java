/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt.handler.publish.incoming;

import com.hivemq.client.internal.annotations.NotThreadSafe;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.ioc.ClientScope;
import com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client.internal.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;
import com.hivemq.client.internal.util.collections.HandleList;
import com.hivemq.client.internal.util.collections.HandleList.Handle;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author Silvio Giebl
 */
@ClientScope
@NotThreadSafe
public class MqttIncomingPublishFlows {

    private final @NotNull MqttSubscribedPublishFlows subscribedFlows;
    private final @Nullable HandleList<MqttGlobalIncomingPublishFlow> @NotNull [] globalFlows;

    @Inject
    MqttIncomingPublishFlows() {
        subscribedFlows = new MqttSubscribedPublishFlowTree();
        //noinspection unchecked
        globalFlows = new HandleList[MqttGlobalPublishFilter.values().length];
    }

    public void subscribe(
            final @NotNull MqttSubscribe subscribe,
            final int subscriptionIdentifier,
            final @Nullable MqttSubscribedPublishFlow flow) {

        final ImmutableList<MqttSubscription> subscriptions = subscribe.getSubscriptions();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < subscriptions.size(); i++) {
            subscribedFlows.subscribe(subscriptions.get(i), subscriptionIdentifier, flow);
        }
    }

    public void subAck(
            final @NotNull MqttSubscribe subscribe,
            final int subscriptionIdentifier,
            final @NotNull ImmutableList<Mqtt5SubAckReasonCode> reasonCodes) {

        final ImmutableList<MqttSubscription> subscriptions = subscribe.getSubscriptions();
        final boolean countNotMatching = subscriptions.size() > reasonCodes.size();
        for (int i = 0; i < subscriptions.size(); i++) {
            subscribedFlows.suback(subscriptions.get(i).getTopicFilter(), subscriptionIdentifier,
                    countNotMatching || reasonCodes.get(i).isError());
        }
    }

    public void unsubscribe(
            final @NotNull MqttUnsubscribe unsubscribe,
            final @NotNull ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes) {

        final ImmutableList<MqttTopicFilterImpl> topicFilters = unsubscribe.getTopicFilters();
        final boolean allSuccess = reasonCodes == Mqtt3UnsubAckView.REASON_CODES_ALL_SUCCESS;
        for (int i = 0; i < topicFilters.size(); i++) {
            if (allSuccess || !reasonCodes.get(i).isError()) {
                subscribedFlows.unsubscribe(topicFilters.get(i));
            }
        }
    }

    void cancel(final @NotNull MqttSubscribedPublishFlow flow) {
        subscribedFlows.cancel(flow);
    }

    public void subscribeGlobal(final @NotNull MqttGlobalIncomingPublishFlow flow) {
        final int filter = flow.getFilter().ordinal();
        HandleList<MqttGlobalIncomingPublishFlow> globalFlowsForFilter = globalFlows[filter];
        if (globalFlowsForFilter == null) {
            globalFlowsForFilter = new HandleList<>();
            globalFlows[filter] = globalFlowsForFilter;
        }
        flow.setHandle(globalFlowsForFilter.add(flow));
    }

    void cancelGlobal(final @NotNull MqttGlobalIncomingPublishFlow flow) {
        final int filter = flow.getFilter().ordinal();
        final HandleList<MqttGlobalIncomingPublishFlow> globalFlowsForFilter = globalFlows[filter];
        final Handle<MqttGlobalIncomingPublishFlow> handle = flow.getHandle();
        if ((globalFlowsForFilter != null) && (handle != null)) {
            globalFlowsForFilter.remove(handle);
            if (globalFlowsForFilter.isEmpty()) {
                globalFlows[filter] = null;
            }
        }
    }

    @NotNull MqttMatchingPublishFlows findMatching(final @NotNull MqttStatefulPublish publish) {
        final MqttMatchingPublishFlows matchingFlows = new MqttMatchingPublishFlows();
        findMatching(publish, matchingFlows);
        return matchingFlows;
    }

    void findMatching(
            final @NotNull MqttStatefulPublish publish, final @NotNull MqttMatchingPublishFlows matchingFlows) {

        subscribedFlows.findMatching(publish.stateless().getTopic(), matchingFlows);
        if (matchingFlows.subscriptionFound) {
            add(matchingFlows, globalFlows[MqttGlobalPublishFilter.SUBSCRIBED.ordinal()]);
        } else {
            add(matchingFlows, globalFlows[MqttGlobalPublishFilter.UNSOLICITED.ordinal()]);
        }
        add(matchingFlows, globalFlows[MqttGlobalPublishFilter.ALL.ordinal()]);
        if (matchingFlows.isEmpty()) {
            add(matchingFlows, globalFlows[MqttGlobalPublishFilter.REMAINING.ordinal()]);
        }
    }

    private static void add(
            final @NotNull MqttMatchingPublishFlows matchingPublishFlows,
            final @Nullable HandleList<MqttGlobalIncomingPublishFlow> globalFlows) {

        if (globalFlows != null) {
            for (Handle<MqttGlobalIncomingPublishFlow> h = globalFlows.getFirst(); h != null; h = h.getNext()) {
                matchingPublishFlows.add(h.getElement());
            }
        }
    }

    public void clear(final @NotNull Throwable cause) {
        subscribedFlows.clear(cause);
        for (int i = 0; i < globalFlows.length; i++) {
            final HandleList<MqttGlobalIncomingPublishFlow> globalFlow = globalFlows[i];
            if (globalFlow != null) {
                for (Handle<MqttGlobalIncomingPublishFlow> h = globalFlow.getFirst(); h != null; h = h.getNext()) {
                    h.getElement().onError(cause);
                }
            }
            globalFlows[i] = null;
        }
    }

    public @NotNull Map<@NotNull Integer, @NotNull List<@NotNull MqttSubscription>> getSubscriptions() {
        return subscribedFlows.getSubscriptions();
    }
}
