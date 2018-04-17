/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.mqtt5.handler.publish;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;
import org.mqttbee.mqtt.message.subscribe.MqttSubscription;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.util.collections.IntMap;
import org.mqttbee.util.collections.ScNodeList;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * single threaded, in channel eventloop
 *
 * @author Silvio Giebl
 */
@ChannelScope
@NotThreadSafe
public class MqttIncomingPublishFlowsWithId extends MqttIncomingPublishFlows {

    private final IntMap<MqttSubscriptionFlow> flowsWithIdsMap;
    private final MqttSubscriptionFlows flowsWithIds;
    private final Consumer<MqttSubscriptionFlow> flowWithIdUnsubscribedCallback;

    @Inject
    MqttIncomingPublishFlowsWithId(
            @NotNull final MqttClientData clientData, @NotNull final MqttSubscriptionFlows flowsWithoutIds,
            @NotNull final MqttSubscriptionFlows flowsWithIds) {

        super(flowsWithoutIds);

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        flowsWithIdsMap = new IntMap<>(clientConnectionData.getSubscriptionIdentifierMaximum());
        this.flowsWithIds = flowsWithIds;
        flowWithIdUnsubscribedCallback = this::unsubscribed;
    }

    @Override
    public void subscribe(
            @NotNull final MqttSubscribeWrapper subscribe, @NotNull final MqttSubAck subAck,
            @NotNull final MqttSubscriptionFlow flow) {

        final int subscriptionIdentifier = subscribe.getSubscriptionIdentifier();
        if (subscriptionIdentifier != MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            final ImmutableList<MqttSubscription> subscriptions = subscribe.getWrapped().getSubscriptions();
            final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
            final ScNodeList<MqttTopicFilterImpl> topicFilters = flow.getTopicFilters();
            for (int i = 0; i < subscriptions.size(); i++) {
                final Mqtt5SubAckReasonCode reasonCode = reasonCodes.get(i);
                if (!reasonCode.isError()) {
                    topicFilters.add(subscriptions.get(i).getTopicFilter());
                }
            }
            flowsWithIdsMap.put(subscriptionIdentifier, flow);
            flow.setSubscriptionIdentifier(subscriptionIdentifier);
        } else {
            super.subscribe(subscribe, subAck, flow);
        }
    }

    @Override
    void unsubscribe(@NotNull final MqttTopicFilterImpl topicFilter) {
        flowsWithIds.unsubscribe(topicFilter, flowWithIdUnsubscribedCallback);
        super.unsubscribe(topicFilter);
    }

    private void unsubscribed(@NotNull final MqttSubscriptionFlow flow) {
        final int subscriptionIdentifier = flow.getSubscriptionIdentifier();
        if (subscriptionIdentifier != MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            flowsWithIdsMap.remove(subscriptionIdentifier);
        }
    }

    @Override
    public void cancel(@NotNull final MqttSubscriptionFlow flow) {
        final int subscriptionIdentifier = flow.getSubscriptionIdentifier();
        if (subscriptionIdentifier != MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            flowsWithIdsMap.remove(subscriptionIdentifier);
        } else {
            super.cancel(flow);
        }
    }

    @NotNull
    @Override
    public ScNodeList<MqttIncomingPublishFlow> findMatching(@NotNull final MqttPublishWrapper publish) {
        final ScNodeList<MqttIncomingPublishFlow> matchingFlows = new ScNodeList<>();

        final ImmutableIntArray subscriptionIdentifiers = publish.getSubscriptionIdentifiers();
        if (!subscriptionIdentifiers.isEmpty()) {
            for (int i = 0; i < subscriptionIdentifiers.length(); i++) {
                final int subscriptionIdentifier = subscriptionIdentifiers.get(i);
                final MqttSubscriptionFlow flow = flowsWithIdsMap.get(subscriptionIdentifier);
                if (flow != null) {
                    matchingFlows.add(flow);
                }
            }
        }

        super.findMatching(publish, matchingFlows);

        return matchingFlows;
    }

}
