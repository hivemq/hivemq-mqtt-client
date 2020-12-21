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

import com.google.common.collect.ImmutableSet;
import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPublishBuilder;
import com.hivemq.client2.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscription;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscriptionBuilder;
import com.hivemq.client2.internal.util.collections.HandleList;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Silvio Giebl
 */
abstract class MqttSubscribedPublishFlowsTest {

    public static class CsvToArray extends SimpleArgumentConverter {

        @Override
        protected @NotNull Object convert(final @NotNull Object source, final @NotNull Class<?> targetType)
                throws ArgumentConversionException {
            final String s = (String) source;
            return s.split("\\s*;\\s*");
        }
    }

    private final @NotNull Supplier<MqttSubscribedPublishFlows> flowsSupplier;
    @NotNull MqttSubscribedPublishFlows flows;

    MqttSubscribedPublishFlowsTest(final @NotNull Supplier<MqttSubscribedPublishFlows> flowsSupplier) {
        this.flowsSupplier = flowsSupplier;
    }

    @BeforeEach
    void setUp() {
        flows = flowsSupplier.get();
    }

    @ParameterizedTest
    @CsvSource({
            "a,    a; +; a/#; +/#; #,                                      true",
            "a,    a; +; a/#; +/#; #,                                      false",
            "a/b,  a/b; a/+; +/b; +/+; a/b/#; a/+/#; +/b/#; +/+/#; a/#; #, true",
            "a/b,  a/b; a/+; +/b; +/+; a/b/#; a/+/#; +/b/#; +/+/#; a/#; #, false",
            "/,    /; +/+; +/; /+; +/#; /#; #,                             true",
            "/,    /; +/+; +/; /+; +/#; /#; #,                             false"
    })
    void subscribe_matchingTopicFilters_doMatch(
            final @NotNull String topic,
            @ConvertWith(CsvToArray.class) final String @NotNull [] matchingTopicFilters,
            final boolean acknowledge) {

        final MqttSubscribedPublishFlow[] matchingFlows = new MqttSubscribedPublishFlow[matchingTopicFilters.length];
        for (int i = 0; i < matchingTopicFilters.length; i++) {
            final MqttSubscription subscription =
                    new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilters[i]).build();
            final MqttSubscribedPublishFlow flow = mockSubscriptionFlow(matchingTopicFilters[i]);
            flows.subscribe(subscription, i, flow);
            assertEquals(ImmutableSet.of(subscription.getTopicFilter()), toSet(flow.getTopicFilters()));
            matchingFlows[i] = flow;

            if (acknowledge) {
                flows.suback(subscription.getTopicFilter(), i, false);
            }
        }

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertTrue(publishWithFlows.subscriptionFound);
        assertFalse(publishWithFlows.isEmpty());
        assertEquals(ImmutableSet.copyOf(matchingFlows), toSet(publishWithFlows));
    }

    @ParameterizedTest
    @CsvSource({
            "a,    a; +; a/#; +/#; #,                                      true",
            "a,    a; +; a/#; +/#; #,                                      false",
            "a/b,  a/b; a/+; +/b; +/+; a/b/#; a/+/#; +/b/#; +/+/#; a/#; #, true",
            "a/b,  a/b; a/+; +/b; +/+; a/b/#; a/+/#; +/b/#; +/+/#; a/#; #, false",
            "/,    /; +/+; +/; /+; +/#; /#; #,                             true",
            "/,    /; +/+; +/; /+; +/#; /#; #,                             false"
    })
    void subscribe_matchingTopicFilters_doMatch_noFlow(
            final @NotNull String topic,
            @ConvertWith(CsvToArray.class) final String @NotNull [] matchingTopicFilters,
            final boolean acknowledge) {

        for (int i = 0; i < matchingTopicFilters.length; i++) {
            final MqttSubscription subscription =
                    new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilters[i]).build();
            flows.subscribe(subscription, i, null);

            if (acknowledge) {
                flows.suback(subscription.getTopicFilter(), i, false);
            }
        }

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertTrue(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "a,    /a; b; a/b; a/+; +/a; +/+; a/b/#; /#; /               ",
            "a/b,  /a/b; a/c; c/b; a/b/c; +/a/b; a/+/b; a/b/+; a/b/c/#; +",
            "/,    //; a/b; a/; /a; +                                    "
    })
    void subscribe_nonMatchingTopicFilters_doNotMatch(
            final @NotNull String topic,
            @ConvertWith(CsvToArray.class) final String @NotNull [] notMatchingTopicFilters) {

        for (int i = 0; i < notMatchingTopicFilters.length; i++) {
            final MqttSubscription subscription =
                    new MqttSubscriptionBuilder.Default().topicFilter(notMatchingTopicFilters[i]).build();
            final MqttSubscribedPublishFlow flow = mockSubscriptionFlow(notMatchingTopicFilters[i]);
            flows.subscribe(subscription, i, flow);
            assertEquals(ImmutableSet.of(subscription.getTopicFilter()), toSet(flow.getTopicFilters()));
        }

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "a,    /a; b; a/b; a/+; +/a; +/+; a/b/#; /#; /               ",
            "a/b,  /a/b; a/c; c/b; a/b/c; +/a/b; a/+/b; a/b/+; a/b/c/#; +",
            "/,    //; a/b; a/; /a; +                                    "
    })
    void subscribe_nonMatchingTopicFilters_doNotMatch_noFlow(
            final @NotNull String topic,
            @ConvertWith(CsvToArray.class) final String @NotNull [] notMatchingTopicFilters) {

        for (int i = 0; i < notMatchingTopicFilters.length; i++) {
            final MqttSubscription subscription =
                    new MqttSubscriptionBuilder.Default().topicFilter(notMatchingTopicFilters[i]).build();
            flows.subscribe(subscription, i, null);
        }

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void unsubscribe_matchingTopicFilters_doNoLongerMatch(
            final @NotNull String topic, final @NotNull String matchingTopicFilter) {

        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        flows.subscribe(subscription1, 1, flow1);
        flows.subscribe(subscription2, 2, flow2);
        flows.suback(subscription1.getTopicFilter(), 1, false);
        flows.suback(subscription2.getTopicFilter(), 2, false);

        flows.unsubscribe(MqttTopicFilterImpl.of(matchingTopicFilter));
        assertTrue(flow1.getTopicFilters().isEmpty());
        assertTrue(flow2.getTopicFilters().isEmpty());

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void unsubscribe_matchingTopicFilters_doNoLongerMatch_noFlow(
            final @NotNull String topic, final @NotNull String matchingTopicFilter) {

        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        flows.subscribe(subscription1, 1, null);
        flows.subscribe(subscription2, 2, null);
        flows.suback(subscription1.getTopicFilter(), 1, false);
        flows.suback(subscription2.getTopicFilter(), 2, false);

        flows.unsubscribe(MqttTopicFilterImpl.of(matchingTopicFilter));

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void unsubscribe_matchingTopicFilters_notAcknowledged_doStillMatch(
            final @NotNull String topic, final @NotNull String matchingTopicFilter) {

        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        flows.subscribe(subscription1, 1, flow1);
        flows.subscribe(subscription2, 2, flow2);

        flows.unsubscribe(MqttTopicFilterImpl.of(matchingTopicFilter));
        assertEquals(ImmutableSet.of(subscription1.getTopicFilter()), toSet(flow1.getTopicFilters()));
        assertEquals(ImmutableSet.of(subscription2.getTopicFilter()), toSet(flow2.getTopicFilters()));

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertTrue(publishWithFlows.subscriptionFound);
        assertEquals(ImmutableSet.of(flow1, flow2), toSet(publishWithFlows));
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void unsubscribe_matchingTopicFilters_notAcknowledged_doStillMatch_noFlow(
            final @NotNull String topic, final @NotNull String matchingTopicFilter) {

        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        flows.subscribe(subscription1, 1, null);
        flows.subscribe(subscription2, 2, null);

        flows.unsubscribe(MqttTopicFilterImpl.of(matchingTopicFilter));

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertTrue(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a, b", "a, a, a/b", "a/b, a/b, a/c"})
    void unsubscribe_nonMatchingTopicFilters_othersStillMatch(
            final @NotNull String topic,
            final @NotNull String matchingTopicFilter,
            final @NotNull String notMatchingTopicFilter) {

        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(notMatchingTopicFilter);
        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(notMatchingTopicFilter).build();
        flows.subscribe(subscription1, 1, flow1);
        flows.subscribe(subscription2, 2, flow2);
        flows.suback(subscription1.getTopicFilter(), 1, false);
        flows.suback(subscription2.getTopicFilter(), 2, false);

        flows.unsubscribe(MqttTopicFilterImpl.of(notMatchingTopicFilter));
        assertFalse(flow1.getTopicFilters().isEmpty());
        assertTrue(flow2.getTopicFilters().isEmpty());

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertTrue(publishWithFlows.subscriptionFound);
        assertFalse(publishWithFlows.isEmpty());
        assertEquals(ImmutableSet.of(flow1), toSet(publishWithFlows));
    }

    @ParameterizedTest
    @CsvSource({"a, a, b", "a, a, a/b", "a/b, a/b, a/c"})
    void unsubscribe_nonMatchingTopicFilters_othersStillMatch_noFlow(
            final @NotNull String topic,
            final @NotNull String matchingTopicFilter,
            final @NotNull String notMatchingTopicFilter) {

        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(notMatchingTopicFilter).build();
        flows.subscribe(subscription1, 1, null);
        flows.subscribe(subscription2, 2, null);
        flows.suback(subscription1.getTopicFilter(), 1, false);
        flows.suback(subscription2.getTopicFilter(), 2, false);

        flows.unsubscribe(MqttTopicFilterImpl.of(notMatchingTopicFilter));

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertTrue(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void cancel_doNoLongerMatch(final @NotNull String topic, final @NotNull String matchingTopicFilter) {
        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        flows.subscribe(subscription1, 1, flow1);
        flows.subscribe(subscription2, 2, flow2);

        flows.cancel(flow1);
        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertTrue(publishWithFlows.subscriptionFound);
        assertFalse(publishWithFlows.isEmpty());
        assertEquals(ImmutableSet.of(flow2), toSet(publishWithFlows));

        flows.cancel(flow2);
        final MqttStatefulPublishWithFlows publishWithFlows2 = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows2);
        assertTrue(publishWithFlows2.subscriptionFound);
        assertTrue(publishWithFlows2.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void cancel_notPresentFlows_areIgnored(final @NotNull String topic, final @NotNull String matchingTopicFilter) {
        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscription subscription =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        flows.subscribe(subscription, 1, flow1);

        flows.cancel(flow2);
        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertTrue(publishWithFlows.subscriptionFound);
        assertFalse(publishWithFlows.isEmpty());
        assertEquals(ImmutableSet.of(flow1), toSet(publishWithFlows));
    }

    @Test
    void cancel_partiallyUnsubscribedFlow() {
        final MqttSubscribedPublishFlow flow = mockSubscriptionFlow("test/topic(2)");
        final MqttSubscription subscription1 = new MqttSubscriptionBuilder.Default().topicFilter("test/topic").build();
        final MqttSubscription subscription2 = new MqttSubscriptionBuilder.Default().topicFilter("test/topic2").build();
        flows.subscribe(subscription1, 1, flow);
        flows.subscribe(subscription2, 1, flow);
        flows.suback(subscription1.getTopicFilter(), 1, false);
        flows.suback(subscription2.getTopicFilter(), 1, false);

        flows.unsubscribe(MqttTopicFilterImpl.of("test/topic"));
        flows.cancel(flow);

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows("test/topic");
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());
        final MqttStatefulPublishWithFlows publishWithFlows2 = newPublishWithFlows("test/topic2");
        flows.findMatching(publishWithFlows2);
        assertTrue(publishWithFlows2.subscriptionFound);
        assertTrue(publishWithFlows2.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "1/a, 1/a, 2/a, 2/a", "1/a, 1/+, 2/a, 2/+", "1/a, 1/#, 2/a, 2/#", "1/a/b, 1/a/b, 2/a/b, 2/a/b",
            "1/a/b, 1/a/+, 2/a/b, 2/a/+", "1/a/b, 1/+/b, 2/a/b, 2/+/b", "1/a/b, 1/+/+, 2/a/b, 2/+/+",
            "1/a/b, 1/+/#, 2/a/b, 2/+/#", "1/a/b, 1/#, 2/a/b, 2/#"
    })
    void suback_error(
            final @NotNull String topic,
            final @NotNull String matchingTopicFilter,
            final @NotNull String topic2,
            final @NotNull String matchingTopicFilter2) {

        final MqttSubscribedPublishFlow flow = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter2).build();
        flows.subscribe(subscription1, 1, flow);
        flows.subscribe(subscription2, 2, flow);
        assertEquals(ImmutableSet.of(subscription1.getTopicFilter(), subscription2.getTopicFilter()),
                toSet(flow.getTopicFilters()));

        flows.suback(subscription1.getTopicFilter(), 1, true);
        assertEquals(ImmutableSet.of(subscription2.getTopicFilter()), toSet(flow.getTopicFilters()));

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());

        final MqttStatefulPublishWithFlows publishWithFlows2 = newPublishWithFlows(topic2);
        flows.findMatching(publishWithFlows2);
        assertTrue(publishWithFlows2.subscriptionFound);
        assertFalse(publishWithFlows2.isEmpty());
        assertEquals(ImmutableSet.of(flow), toSet(publishWithFlows2));
    }

    @ParameterizedTest
    @CsvSource({
            "1/a, 1/a, 2/a, 2/a", "1/a, 1/+, 2/a, 2/+", "1/a, 1/#, 2/a, 2/#", "1/a/b, 1/a/b, 2/a/b, 2/a/b",
            "1/a/b, 1/a/+, 2/a/b, 2/a/+", "1/a/b, 1/+/b, 2/a/b, 2/+/b", "1/a/b, 1/+/+, 2/a/b, 2/+/+",
            "1/a/b, 1/+/#, 2/a/b, 2/+/#", "1/a/b, 1/#, 2/a/b, 2/#"
    })
    void suback_error_noFlow(
            final @NotNull String topic,
            final @NotNull String matchingTopicFilter,
            final @NotNull String topic2,
            final @NotNull String matchingTopicFilter2) {

        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter2).build();
        flows.subscribe(subscription1, 1, null);
        flows.subscribe(subscription2, 2, null);

        flows.suback(subscription1.getTopicFilter(), 1, true);

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());

        final MqttStatefulPublishWithFlows publishWithFlows2 = newPublishWithFlows(topic2);
        flows.findMatching(publishWithFlows2);
        assertTrue(publishWithFlows2.subscriptionFound);
        assertTrue(publishWithFlows2.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void suback_error_doesNotUnsubscribe(final @NotNull String topic, final @NotNull String matchingTopicFilter) {
        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscription subscription =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        flows.subscribe(subscription, 1, flow1);
        flows.subscribe(subscription, 2, flow2);
        assertEquals(ImmutableSet.of(subscription.getTopicFilter()), toSet(flow1.getTopicFilters()));
        assertEquals(ImmutableSet.of(subscription.getTopicFilter()), toSet(flow2.getTopicFilters()));

        flows.suback(subscription.getTopicFilter(), 1, true);
        assertTrue(flow1.getTopicFilters().isEmpty());
        assertEquals(ImmutableSet.of(subscription.getTopicFilter()), toSet(flow2.getTopicFilters()));

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertTrue(publishWithFlows.subscriptionFound);
        assertFalse(publishWithFlows.isEmpty());
        assertEquals(ImmutableSet.of(flow2), toSet(publishWithFlows));
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void suback_error_doesNotUnsubscribe_noFlow(
            final @NotNull String topic, final @NotNull String matchingTopicFilter) {

        final MqttSubscription subscription =
                new MqttSubscriptionBuilder.Default().topicFilter(matchingTopicFilter).build();
        flows.subscribe(subscription, 1, null);
        flows.subscribe(subscription, 2, null);

        flows.suback(subscription.getTopicFilter(), 1, true);

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertTrue(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a/b, a/b/c, +/b/c, +/+/+", "a/b/c/d, a/b/c/d/e, +/b//d/ec, +/+/+/+/+"})
    void findMatching_matchingMultipleButNotAllLevels(
            final @NotNull String topic,
            final @NotNull String filter1,
            final @NotNull String filter2,
            final @NotNull String filter3) {

        final MqttSubscription subscription1 = new MqttSubscriptionBuilder.Default().topicFilter(filter1).build();
        final MqttSubscription subscription2 = new MqttSubscriptionBuilder.Default().topicFilter(filter2).build();
        final MqttSubscription subscription3 = new MqttSubscriptionBuilder.Default().topicFilter(filter3).build();
        flows.subscribe(subscription1, 1, null);
        flows.subscribe(subscription2, 2, null);
        flows.subscribe(subscription3, 3, null);

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows(topic);
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        assertTrue(publishWithFlows.isEmpty());
    }

    @Test
    void clear() {
        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow("test/topic/filter");
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow("test2/topic/filter");
        final MqttSubscribedPublishFlow flow3 = mockSubscriptionFlow("test/topic2/filter");
        final MqttSubscribedPublishFlow flow4 = mockSubscriptionFlow("test/topic/filter2");
        final MqttSubscribedPublishFlow flow5 = mockSubscriptionFlow("+/topic");
        final MqttSubscribedPublishFlow flow6 = mockSubscriptionFlow("topic/#");
        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow1.toString()).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow2.toString()).build();
        final MqttSubscription subscription3 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow3.toString()).build();
        final MqttSubscription subscription4 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow4.toString()).build();
        final MqttSubscription subscription5 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow5.toString()).build();
        final MqttSubscription subscription6 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow6.toString()).build();
        flows.subscribe(subscription1, 1, flow1);
        flows.subscribe(subscription2, 2, flow2);
        flows.subscribe(subscription3, 3, flow3);
        flows.subscribe(subscription4, 4, flow4);
        flows.subscribe(subscription5, 5, flow5);
        flows.subscribe(subscription6, 6, flow6);
        flows.suback(subscription1.getTopicFilter(), 1, false);
        flows.suback(subscription2.getTopicFilter(), 2, false);
        flows.suback(subscription3.getTopicFilter(), 3, false);
        flows.suback(subscription4.getTopicFilter(), 4, false);
        flows.suback(subscription5.getTopicFilter(), 5, false);
        flows.suback(subscription6.getTopicFilter(), 6, false);

        final Exception cause = new Exception("test");
        flows.clear(cause);
        verify(flow1).onError(cause);
        verify(flow2).onError(cause);
        verify(flow3).onError(cause);
        verify(flow4).onError(cause);
        verify(flow5).onError(cause);
        verify(flow6).onError(cause);

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows("test/topic");
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows2 = newPublishWithFlows("test/topic/filter");
        flows.findMatching(publishWithFlows2);
        assertFalse(publishWithFlows2.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows3 = newPublishWithFlows("test2/topic/filter");
        flows.findMatching(publishWithFlows3);
        assertFalse(publishWithFlows3.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows4 = newPublishWithFlows("test/topic2/filter");
        flows.findMatching(publishWithFlows4);
        assertFalse(publishWithFlows4.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows5 = newPublishWithFlows("test/topic/filter2");
        flows.findMatching(publishWithFlows5);
        assertFalse(publishWithFlows5.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows6 = newPublishWithFlows("abc/topic");
        flows.findMatching(publishWithFlows6);
        assertFalse(publishWithFlows6.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows7 = newPublishWithFlows("topic/abc");
        flows.findMatching(publishWithFlows7);
        assertFalse(publishWithFlows7.subscriptionFound);
    }

    @Test
    void clear_noFlow() {
        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter("test/topic/filter").build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter("test2/topic/filter").build();
        final MqttSubscription subscription3 =
                new MqttSubscriptionBuilder.Default().topicFilter("test/topic2/filter").build();
        final MqttSubscription subscription4 =
                new MqttSubscriptionBuilder.Default().topicFilter("test/topic/filter2").build();
        final MqttSubscription subscription5 = new MqttSubscriptionBuilder.Default().topicFilter("+/topic").build();
        final MqttSubscription subscription6 = new MqttSubscriptionBuilder.Default().topicFilter("topic/#").build();
        flows.subscribe(subscription1, 1, null);
        flows.subscribe(subscription2, 2, null);
        flows.subscribe(subscription3, 3, null);
        flows.subscribe(subscription4, 4, null);
        flows.subscribe(subscription5, 5, null);
        flows.subscribe(subscription6, 6, null);
        flows.suback(subscription1.getTopicFilter(), 1, false);
        flows.suback(subscription2.getTopicFilter(), 2, false);
        flows.suback(subscription3.getTopicFilter(), 3, false);
        flows.suback(subscription4.getTopicFilter(), 4, false);
        flows.suback(subscription5.getTopicFilter(), 5, false);
        flows.suback(subscription6.getTopicFilter(), 6, false);

        final Exception cause = new Exception("test");
        flows.clear(cause);

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows("test/topic");
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows2 = newPublishWithFlows("test/topic/filter");
        flows.findMatching(publishWithFlows2);
        assertFalse(publishWithFlows2.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows3 = newPublishWithFlows("test2/topic/filter");
        flows.findMatching(publishWithFlows3);
        assertFalse(publishWithFlows3.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows4 = newPublishWithFlows("test/topic2/filter");
        flows.findMatching(publishWithFlows4);
        assertFalse(publishWithFlows4.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows5 = newPublishWithFlows("test/topic/filter2");
        flows.findMatching(publishWithFlows5);
        assertFalse(publishWithFlows5.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows6 = newPublishWithFlows("abc/topic");
        flows.findMatching(publishWithFlows6);
        assertFalse(publishWithFlows6.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows7 = newPublishWithFlows("topic/abc");
        flows.findMatching(publishWithFlows7);
        assertFalse(publishWithFlows7.subscriptionFound);
    }

    @Test
    void clear_notAcknowledged_doesNotErrorFlows() {
        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow("test/topic/filter");
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow("test2/topic/filter");
        final MqttSubscribedPublishFlow flow3 = mockSubscriptionFlow("test/topic2/filter");
        final MqttSubscribedPublishFlow flow4 = mockSubscriptionFlow("test/topic/filter2");
        final MqttSubscribedPublishFlow flow5 = mockSubscriptionFlow("+/topic");
        final MqttSubscribedPublishFlow flow6 = mockSubscriptionFlow("topic/#");
        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow1.toString()).build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow2.toString()).build();
        final MqttSubscription subscription3 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow3.toString()).build();
        final MqttSubscription subscription4 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow4.toString()).build();
        final MqttSubscription subscription5 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow5.toString()).build();
        final MqttSubscription subscription6 =
                new MqttSubscriptionBuilder.Default().topicFilter(flow6.toString()).build();
        flows.subscribe(subscription1, 1, flow1);
        flows.subscribe(subscription2, 2, flow2);
        flows.subscribe(subscription3, 3, flow3);
        flows.subscribe(subscription4, 4, flow4);
        flows.subscribe(subscription5, 5, flow5);
        flows.subscribe(subscription6, 6, flow6);

        final Exception cause = new Exception("test");
        flows.clear(cause);
        verify(flow1, never()).onError(cause);
        verify(flow2, never()).onError(cause);
        verify(flow3, never()).onError(cause);
        verify(flow4, never()).onError(cause);
        verify(flow5, never()).onError(cause);
        verify(flow6, never()).onError(cause);

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows("test/topic");
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows2 = newPublishWithFlows("test/topic/filter");
        flows.findMatching(publishWithFlows2);
        assertFalse(publishWithFlows2.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows3 = newPublishWithFlows("test2/topic/filter");
        flows.findMatching(publishWithFlows3);
        assertFalse(publishWithFlows3.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows4 = newPublishWithFlows("test/topic2/filter");
        flows.findMatching(publishWithFlows4);
        assertFalse(publishWithFlows4.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows5 = newPublishWithFlows("test/topic/filter2");
        flows.findMatching(publishWithFlows5);
        assertFalse(publishWithFlows5.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows6 = newPublishWithFlows("abc/topic");
        flows.findMatching(publishWithFlows6);
        assertFalse(publishWithFlows6.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows7 = newPublishWithFlows("topic/abc");
        flows.findMatching(publishWithFlows7);
        assertFalse(publishWithFlows7.subscriptionFound);
    }

    @Test
    void clear_notAcknowledged_noFlow() {
        final MqttSubscription subscription1 =
                new MqttSubscriptionBuilder.Default().topicFilter("test/topic/filter").build();
        final MqttSubscription subscription2 =
                new MqttSubscriptionBuilder.Default().topicFilter("test2/topic/filter").build();
        final MqttSubscription subscription3 =
                new MqttSubscriptionBuilder.Default().topicFilter("test/topic2/filter").build();
        final MqttSubscription subscription4 =
                new MqttSubscriptionBuilder.Default().topicFilter("test/topic/filter2").build();
        final MqttSubscription subscription5 = new MqttSubscriptionBuilder.Default().topicFilter("+/topic").build();
        final MqttSubscription subscription6 = new MqttSubscriptionBuilder.Default().topicFilter("topic/#").build();
        flows.subscribe(subscription1, 1, null);
        flows.subscribe(subscription2, 2, null);
        flows.subscribe(subscription3, 3, null);
        flows.subscribe(subscription4, 4, null);
        flows.subscribe(subscription5, 5, null);
        flows.subscribe(subscription6, 6, null);

        final Exception cause = new Exception("test");
        flows.clear(cause);

        final MqttStatefulPublishWithFlows publishWithFlows = newPublishWithFlows("test/topic");
        flows.findMatching(publishWithFlows);
        assertFalse(publishWithFlows.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows2 = newPublishWithFlows("test/topic/filter");
        flows.findMatching(publishWithFlows2);
        assertFalse(publishWithFlows2.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows3 = newPublishWithFlows("test2/topic/filter");
        flows.findMatching(publishWithFlows3);
        assertFalse(publishWithFlows3.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows4 = newPublishWithFlows("test/topic2/filter");
        flows.findMatching(publishWithFlows4);
        assertFalse(publishWithFlows4.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows5 = newPublishWithFlows("test/topic/filter2");
        flows.findMatching(publishWithFlows5);
        assertFalse(publishWithFlows5.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows6 = newPublishWithFlows("abc/topic");
        flows.findMatching(publishWithFlows6);
        assertFalse(publishWithFlows6.subscriptionFound);
        final MqttStatefulPublishWithFlows publishWithFlows7 = newPublishWithFlows("topic/abc");
        flows.findMatching(publishWithFlows7);
        assertFalse(publishWithFlows7.subscriptionFound);
    }

    static @NotNull MqttStatefulPublishWithFlows newPublishWithFlows(final @NotNull String topic) {
        return new MqttStatefulPublishWithFlows(new MqttPublishBuilder.Default().topic(topic)
                .build()
                .createStateful(1, false, MqttStatefulPublish.DEFAULT_NO_TOPIC_ALIAS,
                        MqttStatefulPublish.DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS));
    }

    @Test
    void getSubscriptions() {
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscriptionBuilder.Default().topicFilter("abc").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("$share/group/abc").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("$share/group/#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("test/#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("$share/group/test/#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("+/#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("$share/group/+/#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("+/abc").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("$share/group/+/abc").build());
        for (int i = 0; i < subscriptions.size(); i++) {
            flows.subscribe(subscriptions.get(i), i, null);
            flows.suback(subscriptions.get(i).getTopicFilter(), i, false);
        }
        final Map<Integer, List<MqttSubscription>> allSubscriptions = flows.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i++) {
            assertEquals(ImmutableList.of(subscriptions.get(i)), allSubscriptions.get(i));
        }
        // check if sorted in reverse order
        final AtomicInteger atomicInteger = new AtomicInteger(subscriptions.size());
        allSubscriptions.forEach(
                (subscriptionId, subscriptionsForId) -> assertEquals(atomicInteger.decrementAndGet(), subscriptionId));
    }

    @Test
    void getSubscriptions_sameSubscriptionIdentifiers() {
        final ImmutableList<MqttSubscription> subscriptions = ImmutableList.of(
                new MqttSubscriptionBuilder.Default().topicFilter("abc").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("$share/group/abc").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("$share/group/#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("test/#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("$share/group/test/#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("+/#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("$share/group/+/#").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("+/abc").build(),
                new MqttSubscriptionBuilder.Default().topicFilter("$share/group/+/abc").build());
        for (int i = 0; i < subscriptions.size(); i += 2) {
            for (int j = 0; j < 2; j++) {
                flows.subscribe(subscriptions.get(i + j), i, null);
                flows.suback(subscriptions.get(i + j).getTopicFilter(), i, false);
            }
        }
        final Map<Integer, List<MqttSubscription>> allSubscriptions = flows.getSubscriptions();
        for (int i = 0; i < subscriptions.size(); i += 2) {
            assertEquals(
                    ImmutableSet.of(subscriptions.get(i), subscriptions.get(i + 1)),
                    ImmutableSet.copyOf(allSubscriptions.get(i)));
        }
        // check if sorted in reverse order
        final AtomicInteger atomicInteger = new AtomicInteger(subscriptions.size());
        allSubscriptions.forEach(
                (subscriptionId, subscriptionsForId) -> assertEquals(atomicInteger.addAndGet(-2), subscriptionId));
    }

    private static @NotNull MqttSubscribedPublishFlow mockSubscriptionFlow(final @NotNull String name) {
        final MqttSubscribedPublishFlow flow = mock(MqttSubscribedPublishFlow.class);
        final HandleList<MqttTopicFilterImpl> topicFilters = new HandleList<>();
        when(flow.getTopicFilters()).thenReturn(topicFilters);
        when(flow.toString()).thenReturn(name);
        return flow;
    }

    private <E> @NotNull ImmutableSet<E> toSet(final @NotNull HandleList<E> list) {
        final ImmutableSet.Builder<E> builder = ImmutableSet.builder();
        for (HandleList.Handle<E> h = list.getFirst(); h != null; h = h.getNext()) {
            builder.add(h.getElement());
        }
        return builder.build();
    }
}
