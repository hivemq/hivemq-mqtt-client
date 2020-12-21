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

package com.hivemq.client2.internal.mqtt.handler.publish.incoming;

import com.hivemq.client2.internal.annotations.NotThreadSafe;
import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client2.internal.mqtt.ioc.ClientScope;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client2.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client2.internal.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubAckView;
import com.hivemq.client2.internal.util.collections.HandleList;
import com.hivemq.client2.internal.util.collections.HandleList.Handle;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client2.mqtt.mqtt5.message.subscribe.Mqtt5SubAckReasonCode;
import com.hivemq.client2.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAckReasonCode;
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

    void findMatching(final @NotNull MqttStatefulPublishWithFlows publishWithFlows) {
        subscribedFlows.findMatching(publishWithFlows);
        if (publishWithFlows.subscriptionFound) {
            add(publishWithFlows, globalFlows[MqttGlobalPublishFilter.SUBSCRIBED.ordinal()]);
        } else {
            add(publishWithFlows, globalFlows[MqttGlobalPublishFilter.UNSOLICITED.ordinal()]);
        }
        add(publishWithFlows, globalFlows[MqttGlobalPublishFilter.ALL.ordinal()]);
        if (publishWithFlows.isEmpty()) {
            add(publishWithFlows, globalFlows[MqttGlobalPublishFilter.REMAINING.ordinal()]);
        }
    }

    private static void add(
            final @NotNull MqttStatefulPublishWithFlows publishWithFlows,
            final @Nullable HandleList<MqttGlobalIncomingPublishFlow> globalFlows) {

        if (globalFlows != null) {
            for (Handle<MqttGlobalIncomingPublishFlow> h = globalFlows.getFirst(); h != null; h = h.getNext()) {
                publishWithFlows.add(h.getElement());
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
