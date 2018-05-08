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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribeWrapper;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;
import org.mqttbee.util.collections.ScNodeList;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;

/**
 * @author Silvio Giebl
 */
@ChannelScope
@NotThreadSafe
public class MqttIncomingPublishFlows {

    @NotNull
    private final MqttSubscriptionFlows subscriptionFlows;
    @NotNull
    private final ScNodeList<MqttGlobalIncomingPublishFlow>[] globalFlows;

    @Inject
    @SuppressWarnings("unchecked")
    MqttIncomingPublishFlows(@NotNull final MqttSubscriptionFlows subscriptionFlows) {
        this.subscriptionFlows = subscriptionFlows;
        globalFlows = new ScNodeList[MqttGlobalIncomingPublishFlow.TYPE_COUNT];
    }

    public void subscribe(
            @NotNull final MqttSubscribeWrapper subscribe, @NotNull final MqttSubAck subAck,
            @NotNull final MqttSubscriptionFlow flow) {

        final ImmutableList<MqttSubscription> subscriptions = subscribe.getWrapped().getSubscriptions();
        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
        for (int i = 0; i < subscriptions.size(); i++) {
            if (!reasonCodes.get(i).isError()) {
                subscribe(subscriptions.get(i).getTopicFilter(), flow);
            }
        }
    }

    void subscribe(@NotNull final MqttTopicFilterImpl topicFilter, @NotNull final MqttSubscriptionFlow flow) {
        subscriptionFlows.subscribe(topicFilter, flow);
    }

    public void unsubscribe(@NotNull final MqttUnsubscribeWrapper unsubscribe, @NotNull final MqttUnsubAck unsubAck) {
        final ImmutableList<MqttTopicFilterImpl> topicFilters = unsubscribe.getWrapped().getTopicFilters();
        final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes = unsubAck.getReasonCodes();
        final boolean areAllSuccess = reasonCodes == Mqtt3UnsubAckView.REASON_CODES_ALL_SUCCESS;
        for (int i = 0; i < topicFilters.size(); i++) {
            if (areAllSuccess || !reasonCodes.get(i).isError()) {
                unsubscribe(topicFilters.get(i));
            }
        }
    }

    void unsubscribe(@NotNull final MqttTopicFilterImpl topicFilter) {
        subscriptionFlows.unsubscribe(topicFilter, null);
    }

    public void cancel(@NotNull final MqttSubscriptionFlow flow) {
        subscriptionFlows.cancel(flow);
    }

    @NotNull
    public ScNodeList<MqttIncomingPublishFlow> findMatching(@NotNull final MqttPublishWrapper publish) {
        final ScNodeList<MqttIncomingPublishFlow> matchingFlows = new ScNodeList<>();
        findMatching(publish, matchingFlows);
        return matchingFlows;
    }

    void findMatching(
            @NotNull final MqttPublishWrapper publish,
            @NotNull final ScNodeList<MqttIncomingPublishFlow> matchingFlows) {

        final MqttTopicImpl topic = publish.getWrapped().getTopic();
        if (subscriptionFlows.findMatching(topic, matchingFlows)) {
            addAndReference(matchingFlows, globalFlows[MqttGlobalIncomingPublishFlow.TYPE_ALL_SUBSCRIPTIONS]);
        }
        addAndReference(matchingFlows, globalFlows[MqttGlobalIncomingPublishFlow.TYPE_ALL_PUBLISHES]);
        if (matchingFlows.isEmpty()) {
            addAndReference(matchingFlows, globalFlows[MqttGlobalIncomingPublishFlow.TYPE_REMAINING_PUBLISHES]);
        }
    }

    public void subscribeGlobal(@NotNull final MqttGlobalIncomingPublishFlow flow) {
        final int type = flow.getType();
        ScNodeList<MqttGlobalIncomingPublishFlow> globalFlow = globalFlows[type];
        if (globalFlow == null) {
            globalFlow = new ScNodeList<>();
            globalFlows[type] = globalFlow;
        }
        flow.setHandle(globalFlow.add(flow));
    }

    public void cancelGlobal(@NotNull final MqttGlobalIncomingPublishFlow flow) {
        flow.getHandle().remove();
        final int type = flow.getType();
        final ScNodeList<MqttGlobalIncomingPublishFlow> globalFlow = globalFlows[type];
        if (globalFlow.isEmpty()) {
            globalFlows[type] = null;
        }
    }


    static void addAndReference(
            @NotNull final ScNodeList<MqttIncomingPublishFlow> target, @NotNull final MqttIncomingPublishFlow flow) {

        flow.reference();
        target.add(flow);
    }

    private static void addAndReference(
            @NotNull final ScNodeList<MqttIncomingPublishFlow> target,
            @Nullable final ScNodeList<? extends MqttIncomingPublishFlow> source) {

        if (source != null) {
            for (final MqttIncomingPublishFlow flow : source) {
                addAndReference(target, flow);
            }
        }
    }

}
