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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.ioc.ClientScope;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;
import org.mqttbee.util.collections.HandleList;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

/**
 * @author Silvio Giebl
 */
@ClientScope
@NotThreadSafe
public class MqttIncomingPublishFlows {

    private final @NotNull MqttSubscriptionFlows subscriptionFlows;
    private final @Nullable HandleList<MqttGlobalIncomingPublishFlow> @NotNull [] globalFlows;

    @Inject
    @SuppressWarnings("unchecked")
    MqttIncomingPublishFlows(final @NotNull MqttSubscriptionFlows subscriptionFlows) {
        this.subscriptionFlows = subscriptionFlows;
        globalFlows = new HandleList[MqttGlobalPublishFilter.values().length];
    }

    public void subscribe(
            final @NotNull MqttStatefulSubscribe subscribe, final @NotNull MqttSubAck subAck,
            final @Nullable MqttSubscriptionFlow flow) {

        final ImmutableList<MqttSubscription> subscriptions = subscribe.getStatelessMessage().getSubscriptions();
        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
        for (int i = 0; i < subscriptions.size(); i++) {
            if (!reasonCodes.get(i).isError()) {
                subscribe(subscriptions.get(i).getTopicFilter(), flow);
            }
        }
    }

    void subscribe(final @NotNull MqttTopicFilterImpl topicFilter, final @Nullable MqttSubscriptionFlow flow) {
        subscriptionFlows.subscribe(topicFilter, flow);
    }

    public void unsubscribe(final @NotNull MqttStatefulUnsubscribe unsubscribe, final @NotNull MqttUnsubAck unsubAck) {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = unsubscribe.getStatelessMessage().getTopicFilters();
        final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes = unsubAck.getReasonCodes();
        final boolean areAllSuccess = reasonCodes == Mqtt3UnsubAckView.REASON_CODES_ALL_SUCCESS;
        for (int i = 0; i < topicFilters.size(); i++) {
            if (areAllSuccess || !reasonCodes.get(i).isError()) {
                unsubscribe(topicFilters.get(i));
            }
        }
    }

    void unsubscribe(final @NotNull MqttTopicFilterImpl topicFilter) {
        subscriptionFlows.unsubscribe(topicFilter, null);
    }

    void cancel(final @NotNull MqttSubscriptionFlow flow) {
        subscriptionFlows.cancel(flow);
    }

    @NotNull HandleList<MqttIncomingPublishFlow> findMatching(final @NotNull MqttStatefulPublish publish) {
        final HandleList<MqttIncomingPublishFlow> matchingFlows = new HandleList<>();
        findMatching(publish, matchingFlows);
        return matchingFlows;
    }

    void findMatching(
            final @NotNull MqttStatefulPublish publish,
            final @NotNull HandleList<MqttIncomingPublishFlow> matchingFlows) {

        final MqttTopicImpl topic = publish.getStatelessMessage().getTopic();
        if (subscriptionFlows.findMatching(topic, matchingFlows) || !matchingFlows.isEmpty()) {
            add(matchingFlows, globalFlows[MqttGlobalPublishFilter.ALL_SUBSCRIPTIONS.ordinal()]);
        }
        add(matchingFlows, globalFlows[MqttGlobalPublishFilter.ALL_PUBLISHES.ordinal()]);
        if (matchingFlows.isEmpty()) {
            add(matchingFlows, globalFlows[MqttGlobalPublishFilter.REMAINING_PUBLISHES.ordinal()]);
        }
    }

    void subscribeGlobal(final @NotNull MqttGlobalIncomingPublishFlow flow) {
        final int filter = flow.getFilter().ordinal();
        HandleList<MqttGlobalIncomingPublishFlow> globalFlow = globalFlows[filter];
        if (globalFlow == null) {
            globalFlow = new HandleList<>();
            globalFlows[filter] = globalFlow;
        }
        flow.setHandle(globalFlow.add(flow));
    }

    void cancelGlobal(final @NotNull MqttGlobalIncomingPublishFlow flow) {
        final HandleList.Handle<MqttGlobalIncomingPublishFlow> handle = flow.getHandle();
        assert handle != null;
        handle.remove();
        final int filter = flow.getFilter().ordinal();
        final HandleList<MqttGlobalIncomingPublishFlow> globalFlow = globalFlows[filter];
        assert globalFlow != null;
        if (globalFlow.isEmpty()) {
            globalFlows[filter] = null;
        }
    }

    private static void add(
            final @NotNull HandleList<MqttIncomingPublishFlow> target,
            final @Nullable HandleList<? extends MqttIncomingPublishFlow> source) {

        if (source != null) {
            for (final MqttIncomingPublishFlow flow : source) {
                target.add(flow);
            }
        }
    }

}
