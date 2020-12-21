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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscriptionBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttSubscribedPublishFlowTreeTest extends MqttSubscribedPublishFlowsTest {

    MqttSubscribedPublishFlowTreeTest() {
        super(MqttSubscribedPublishFlowTree::new);
    }

    @ParameterizedTest
    @CsvSource({
            // split single level before and after
            "unsubscribe, test/topic1, test/topic2, test/topic3, test/topic1, test/topic2, test/topic3",
            // split single level before, single level wildcard after
            "unsubscribe, test/+, test/topic2, test/topic3, test/topic1, test/topic2, test/topic3",
            // split single level before, single level wildcard after, fuse different order
            "unsubscribe, test/topic1, test/topic2, test/+, test/topic1, test/topic2, test/topic3",
            // split multiple levels before, single level after
            "unsubscribe, test/topic/filter1, test/topic/filter2, test/topic/filter3, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            // split multiple levels before, single level wildcard after
            "unsubscribe, test/topic/+, test/topic/filter2, test/topic/filter3, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            // split multiple levels before, single level wildcard after, fuse different order
            "unsubscribe, test/topic/filter1, test/topic/filter2, test/topic/+, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            // split single level before, multiple levels after
            "unsubscribe, test/topic1/filter, test/topic2/filter, test/topic3/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            // split single level before, single level wildcard with multiple levels after
            "unsubscribe, test/+/filter, test/topic2/filter, test/topic3/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            // split single level before, single level wildcard with multiple levels after, fuse different order
            "unsubscribe, test/topic1/filter, test/topic2/filter, test/+/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            // split multiple levels before, multiple levels after
            "unsubscribe, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/filter3/abc, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/filter3/abc",
            // split multi levels before, single level wildcard with multiple levels after
            "unsubscribe, test/topic/+/abc, test/topic/filter2/abc, test/topic/filter3/abc, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/filter3/abc",
            // split multi levels before, single level wildcard with multiple levels after, fuse different order
            "unsubscribe, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/+/abc, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/filter3/abc",
            // split single level wildcard before, single level after
            "unsubscribe, +/topic1, +/topic2, +/topic3, test/topic1, test/topic2, test/topic3",
            // split single level wildcard before, multiple levels after
            "unsubscribe, +/topic1/filter, +/topic2/filter, +/topic3/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            // split single level wildcard with multiple levels before, single level after
            "unsubscribe, +/topic/filter1, +/topic/filter2, +/topic/filter3, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            // split single level wildcard with multiple levels before, multiple levels after
            "unsubscribe, +/topic/filter1/abc, +/topic/filter2/abc, +/topic/filter3/abc, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/filter3/abc",
            // split linear chain
            "unsubscribe, test/topic/filter, test/topic//filter, test/topic///filter, test/topic/filter, test/topic//filter, test/topic///filter",
            // split linear chain, do not fuse
            "unsubscribe, test/topic/filter, test/topic//filter, test/topic///filter, test/topic///filter, test/topic//filter, test/topic/filter",
            "remove, test/topic1, test/topic2, test/topic3, test/topic1, test/topic2, test/topic3",
            "remove, test/+, test/topic2, test/topic3, test/topic1, test/topic2, test/topic3",
            "remove, test/topic1, test/topic2, test/+, test/topic1, test/topic2, test/topic3",
            "remove, test/topic/filter1, test/topic/filter2, test/topic/filter3, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            "remove, test/topic/+, test/topic/filter2, test/topic/filter3, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            "remove, test/topic/filter1, test/topic/filter2, test/topic/+, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            "remove, test/topic1/filter, test/topic2/filter, test/topic3/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            "remove, test/+/filter, test/topic2/filter, test/topic3/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            "remove, test/topic1/filter, test/topic2/filter, test/+/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            "remove, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/filter3/abc, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/filter3/abc",
            "remove, test/topic/+/abc, test/topic/filter2/abc, test/topic/filter3/abc, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/filter3/abc",
            "remove, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/+/abc, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/filter3/abc",
            "remove, +/filter1, +/filter2, +/filter3, topic/filter1, topic/filter2, topic/filter3",
            "remove, +/topic1/filter, +/topic2/filter, +/topic3/filter, test/topic1/filter, test/topic2/filter, test/topic3/filter",
            "remove, +/topic/filter1, +/topic/filter2, +/topic/filter3, test/topic/filter1, test/topic/filter2, test/topic/filter3",
            "remove, +/topic/filter1/abc, +/topic/filter2/abc, +/topic/filter3/abc, test/topic/filter1/abc, test/topic/filter2/abc, test/topic/filter3/abc",
            "remove, test/topic/filter, test/topic//filter, test/topic///filter, test/topic/filter, test/topic//filter, test/topic///filter",
            "remove, test/topic/filter, test/topic//filter, test/topic///filter, test/topic///filter, test/topic//filter, test/topic/filter",
    })
    void branching_compaction(
            final @NotNull String compactOperation,
            final @NotNull String filter1,
            final @NotNull String filter2,
            final @NotNull String filter3,
            final @NotNull String topic1,
            final @NotNull String topic2,
            final @NotNull String topic3) {

        final MqttSubscription subscription1 = new MqttSubscriptionBuilder.Default().topicFilter(filter1).build();
        final MqttSubscription subscription2 = new MqttSubscriptionBuilder.Default().topicFilter(filter2).build();
        final MqttSubscription subscription3 = new MqttSubscriptionBuilder.Default().topicFilter(filter3).build();
        flows.subscribe(subscription1, 1, null);
        flows.subscribe(subscription2, 2, null);
        flows.subscribe(subscription3, 3, null);
        flows.suback(subscription1.getTopicFilter(), 1, false);
        flows.suback(subscription2.getTopicFilter(), 2, false);
        flows.suback(subscription3.getTopicFilter(), 3, false);

        final MqttStatefulPublishWithFlows publishWithFlows1 = newPublishWithFlows(topic1);
        flows.findMatching(publishWithFlows1);
        assertTrue(publishWithFlows1.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows2 = newPublishWithFlows(topic2);
        flows.findMatching(publishWithFlows2);
        assertTrue(publishWithFlows1.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows3 = newPublishWithFlows(topic3);
        flows.findMatching(publishWithFlows3);
        assertTrue(publishWithFlows1.subscriptionFound);

        assertEquals(ImmutableMap.of(1, ImmutableList.of(subscription1), 2, ImmutableList.of(subscription2), 3,
                ImmutableList.of(subscription3)), flows.getSubscriptions());

        switch (compactOperation) {
            case "unsubscribe":
                flows.unsubscribe(MqttTopicFilterImpl.of(filter1));
                flows.unsubscribe(MqttTopicFilterImpl.of(filter2));
                flows.unsubscribe(MqttTopicFilterImpl.of(filter3));
                break;
            case "remove":
                flows.suback(MqttTopicFilterImpl.of(filter1), 1, true);
                flows.suback(MqttTopicFilterImpl.of(filter2), 2, true);
                flows.suback(MqttTopicFilterImpl.of(filter3), 3, true);
                break;
            default:
                fail();
        }

        final MqttStatefulPublishWithFlows publishWithFlows4 = newPublishWithFlows(topic1);
        flows.findMatching(publishWithFlows4);
        assertFalse(publishWithFlows4.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows5 = newPublishWithFlows(topic2);
        flows.findMatching(publishWithFlows5);
        assertFalse(publishWithFlows5.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows6 = newPublishWithFlows(topic3);
        flows.findMatching(publishWithFlows6);
        assertFalse(publishWithFlows6.subscriptionFound);

        assertTrue(flows.getSubscriptions().isEmpty());
    }
}
