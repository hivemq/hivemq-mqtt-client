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

import com.google.common.primitives.ImmutableIntArray;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.message.subscribe.MqttStatefulSubscribe;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.util.collections.IntMap;
import org.mqttbee.util.collections.ScNodeList;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.util.function.Consumer;

import static org.mqttbee.mqtt.message.subscribe.MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;

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
    private final Consumer<MqttSubscriptionFlow> flowWithIdUnsubscribedCallback = this::unsubscribed;

    @Inject
    MqttIncomingPublishFlowsWithId(
            @NotNull final MqttClientData clientData, @NotNull final MqttSubscriptionFlows flowsWithoutIds,
            @NotNull final MqttSubscriptionFlows flowsWithIds) {

        super(flowsWithoutIds);

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        flowsWithIdsMap = new IntMap<>(1, clientConnectionData.getSubscriptionIdentifierMaximum());
        this.flowsWithIds = flowsWithIds;
    }

    @Override
    public void subscribe(
            @NotNull final MqttStatefulSubscribe subscribe, @NotNull final MqttSubAck subAck,
            @Nullable final MqttSubscriptionFlow flow) {

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
    void subscribe(@NotNull final MqttTopicFilterImpl topicFilter, @Nullable final MqttSubscriptionFlow flow) {
        if ((flow != null) && (flow.getSubscriptionIdentifier() != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER)) {
            flowsWithIds.subscribe(topicFilter, flow);
        } else {
            super.subscribe(topicFilter, flow);
        }
    }

    @Override
    void unsubscribe(@NotNull final MqttTopicFilterImpl topicFilter) {
        flowsWithIds.unsubscribe(topicFilter, flowWithIdUnsubscribedCallback);
        super.unsubscribe(topicFilter);
    }

    private void unsubscribed(@NotNull final MqttSubscriptionFlow flow) {
        final int subscriptionIdentifier = flow.getSubscriptionIdentifier();
        if (subscriptionIdentifier != DEFAULT_NO_SUBSCRIPTION_IDENTIFIER) {
            flowsWithIdsMap.remove(subscriptionIdentifier);
        }
    }

    @Override
    public void cancel(@NotNull final MqttSubscriptionFlow flow) {
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
            @NotNull final MqttStatefulPublish publish,
            @NotNull final ScNodeList<MqttIncomingPublishFlow> matchingFlows) {

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
    }

}
