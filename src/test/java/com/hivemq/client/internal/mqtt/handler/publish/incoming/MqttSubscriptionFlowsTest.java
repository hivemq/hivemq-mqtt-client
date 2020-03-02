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

import com.google.common.collect.ImmutableSet;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.util.collections.HandleList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Silvio Giebl
 */
abstract class MqttSubscriptionFlowsTest {

    public static class CsvToArray extends SimpleArgumentConverter {

        @Override
        protected @NotNull Object convert(final @NotNull Object source, final @NotNull Class<?> targetType)
                throws ArgumentConversionException {
            final String s = (String) source;
            return s.split("\\s*;\\s*");
        }
    }

    private final @NotNull Supplier<MqttSubscriptionFlows> flowsSupplier;
    @SuppressWarnings("NullabilityAnnotations")
    MqttSubscriptionFlows flows;

    MqttSubscriptionFlowsTest(final @NotNull Supplier<MqttSubscriptionFlows> flowsSupplier) {
        this.flowsSupplier = flowsSupplier;
    }

    @BeforeEach
    void setUp() {
        flows = flowsSupplier.get();
    }

    @ParameterizedTest
    @CsvSource({
            "a,    a; +; a/#; +/#; #                                     ",
            "a/b,  a/b; a/+; +/b; +/+; a/b/#; a/+/#; +/b/#; +/+/#; a/#; #",
            "/,    /; +/+; +/; /+; +/#; /#; #                            "
    })
    void subscribe_matchingTopicFilters_doMatch(
            final @NotNull String topic, @ConvertWith(CsvToArray.class) final @NotNull String[] matchingTopicFilters) {

        final MqttSubscribedPublishFlow[] matchingFlows = new MqttSubscribedPublishFlow[matchingTopicFilters.length];
        for (int i = 0; i < matchingTopicFilters.length; i++) {
            final MqttSubscribedPublishFlow flow = mockSubscriptionFlow(matchingTopicFilters[i]);
            final MqttTopicFilterImpl topicFilter = MqttTopicFilterImpl.of(matchingTopicFilters[i]);
            flows.subscribe(topicFilter, flow);
            assertEquals(ImmutableSet.of(topicFilter), toSet(flow.getTopicFilters()));
            matchingFlows[i] = flow;
        }

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertTrue(matching.subscriptionFound);
        assertFalse(matching.isEmpty());
        assertEquals(ImmutableSet.copyOf(matchingFlows), toSet(matching));
    }

    @ParameterizedTest
    @CsvSource({
            "a,    a; +; a/#; +/#; #                                     ",
            "a/b,  a/b; a/+; +/b; +/+; a/b/#; a/+/#; +/b/#; +/+/#; a/#; #",
            "/,    /; +/+; +/; /+; +/#; /#; #                            "
    })
    void subscribe_matchingTopicFilters_doMatch_noFlow(
            final @NotNull String topic, @ConvertWith(CsvToArray.class) final @NotNull String[] matchingTopicFilters) {

        for (final String matchingTopicFilter : matchingTopicFilters) {
            flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), null);
        }

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertTrue(matching.subscriptionFound);
        assertTrue(matching.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "a,    /a; b; a/b; a/+; +/a; +/+; a/b/#; /#; /               ",
            "a/b,  /a/b; a/c; c/b; a/b/c; +/a/b; a/+/b; a/b/+; a/b/c/#; +",
            "/,    //; a/b; a/; /a; +                                    "
    })
    void subscribe_nonMatchingTopicFilters_doNotMatch(
            final @NotNull String topic,
            @ConvertWith(CsvToArray.class) final @NotNull String[] notMatchingTopicFilters) {

        for (final String notMatchingTopicFilter : notMatchingTopicFilters) {
            final MqttSubscribedPublishFlow flow = mockSubscriptionFlow(notMatchingTopicFilter);
            final MqttTopicFilterImpl topicFilter = MqttTopicFilterImpl.of(notMatchingTopicFilter);
            flows.subscribe(topicFilter, flow);
            assertEquals(ImmutableSet.of(topicFilter), toSet(flow.getTopicFilters()));
        }

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertFalse(matching.subscriptionFound);
        assertTrue(matching.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "a,    /a; b; a/b; a/+; +/a; +/+; a/b/#; /#; /               ",
            "a/b,  /a/b; a/c; c/b; a/b/c; +/a/b; a/+/b; a/b/+; a/b/c/#; +",
            "/,    //; a/b; a/; /a; +                                    "
    })
    void subscribe_nonMatchingTopicFilters_doNotMatch_noFlow(
            final @NotNull String topic,
            @ConvertWith(CsvToArray.class) final @NotNull String[] notMatchingTopicFilters) {

        for (final String notMatchingTopicFilter : notMatchingTopicFilters) {
            flows.subscribe(MqttTopicFilterImpl.of(notMatchingTopicFilter), null);
        }

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertFalse(matching.subscriptionFound);
        assertTrue(matching.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void unsubscribe_matchingTopicFilters_doNoLongerMatch(
            final @NotNull String topic, final @NotNull String matchingTopicFilter) {

        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), flow1);
        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), flow2);

        final HandleList<MqttSubscribedPublishFlow> unsubscribed = new HandleList<>();
        flows.unsubscribe(MqttTopicFilterImpl.of(matchingTopicFilter), unsubscribed::add);
        assertTrue(flow1.getTopicFilters().isEmpty());
        assertTrue(flow2.getTopicFilters().isEmpty());
        assertEquals(ImmutableSet.of(flow1, flow2), toSet(unsubscribed));

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertFalse(matching.subscriptionFound);
        assertTrue(matching.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void unsubscribe_matchingTopicFilters_doNoLongerMatch_noFlow(
            final @NotNull String topic, final @NotNull String matchingTopicFilter) {

        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), null);
        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), null);

        final HandleList<MqttSubscribedPublishFlow> unsubscribed = new HandleList<>();
        flows.unsubscribe(MqttTopicFilterImpl.of(matchingTopicFilter), unsubscribed::add);
        assertTrue(unsubscribed.isEmpty());

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertFalse(matching.subscriptionFound);
        assertTrue(matching.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a, b", "a, a, a/b", "a/b, a/b, a/c"})
    void unsubscribe_nonMatchingTopicFilters_othersStillMatch(
            final @NotNull String topic, final @NotNull String matchingTopicFilter,
            final @NotNull String notMatchingTopicFilter) {

        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(notMatchingTopicFilter);
        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), flow1);
        flows.subscribe(MqttTopicFilterImpl.of(notMatchingTopicFilter), flow2);

        final HandleList<MqttSubscribedPublishFlow> unsubscribed = new HandleList<>();
        flows.unsubscribe(MqttTopicFilterImpl.of(notMatchingTopicFilter), unsubscribed::add);
        assertFalse(flow1.getTopicFilters().isEmpty());
        assertTrue(flow2.getTopicFilters().isEmpty());
        assertEquals(ImmutableSet.of(flow2), toSet(unsubscribed));

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertTrue(matching.subscriptionFound);
        assertFalse(matching.isEmpty());
        assertEquals(ImmutableSet.of(flow1), toSet(matching));
    }

    @ParameterizedTest
    @CsvSource({"a, a, b", "a, a, a/b", "a/b, a/b, a/c"})
    void unsubscribe_nonMatchingTopicFilters_othersStillMatch_noFlow(
            final @NotNull String topic, final @NotNull String matchingTopicFilter,
            final @NotNull String notMatchingTopicFilter) {

        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), null);
        flows.subscribe(MqttTopicFilterImpl.of(notMatchingTopicFilter), null);

        final HandleList<MqttSubscribedPublishFlow> unsubscribed = new HandleList<>();
        flows.unsubscribe(MqttTopicFilterImpl.of(notMatchingTopicFilter), unsubscribed::add);
        assertTrue(unsubscribed.isEmpty());

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertTrue(matching.subscriptionFound);
        assertTrue(matching.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void cancel_doNoLongerMatch(final @NotNull String topic, final @NotNull String matchingTopicFilter) {
        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), flow1);
        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), flow2);

        flows.cancel(flow1);
        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertTrue(matching.subscriptionFound);
        assertFalse(matching.isEmpty());
        assertEquals(ImmutableSet.of(flow2), toSet(matching));

        flows.cancel(flow2);
        final MqttMatchingPublishFlows matching2 = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching2);
        assertTrue(matching2.subscriptionFound);
        assertTrue(matching2.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void cancel_notPresentFlows_areIgnored(final @NotNull String topic, final @NotNull String matchingTopicFilter) {
        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), flow1);

        flows.cancel(flow2);
        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertTrue(matching.subscriptionFound);
        assertFalse(matching.isEmpty());
        assertEquals(ImmutableSet.of(flow1), toSet(matching));
    }

    @Test
    void cancel_partiallyUnsubscribedFlow() {
        final MqttSubscribedPublishFlow flow = mockSubscriptionFlow("test/topic(2)");
        flows.subscribe(MqttTopicFilterImpl.of("test/topic"), flow);
        flows.subscribe(MqttTopicFilterImpl.of("test/topic2"), flow);

        flows.unsubscribe(MqttTopicFilterImpl.of("test/topic"), null);
        flows.cancel(flow);

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of("test/topic"), matching);
        assertFalse(matching.subscriptionFound);
        flows.findMatching(MqttTopicImpl.of("test/topic2"), matching);
        assertTrue(matching.subscriptionFound);
        assertTrue(matching.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "1/a, 1/a, 2/a, 2/a", "1/a, 1/+, 2/a, 2/+", "1/a, 1/#, 2/a, 2/#", "1/a/b, 1/a/b, 2/a/b, 2/a/b",
            "1/a/b, 1/a/+, 2/a/b, 2/a/+", "1/a/b, 1/+/b, 2/a/b, 2/+/b", "1/a/b, 1/+/+, 2/a/b, 2/+/+",
            "1/a/b, 1/+/#, 2/a/b, 2/+/#", "1/a/b, 1/#, 2/a/b, 2/#"
    })
    void remove(
            final @NotNull String topic, final @NotNull String matchingTopicFilter, final @NotNull String topic2,
            final @NotNull String matchingTopicFilter2) {

        final MqttSubscribedPublishFlow flow = mockSubscriptionFlow(matchingTopicFilter);
        final MqttTopicFilterImpl topicFilter = MqttTopicFilterImpl.of(matchingTopicFilter);
        final MqttTopicFilterImpl topicFilter2 = MqttTopicFilterImpl.of(matchingTopicFilter2);
        flows.subscribe(topicFilter, flow);
        flows.subscribe(topicFilter2, flow);
        assertEquals(ImmutableSet.of(topicFilter, topicFilter2), toSet(flow.getTopicFilters()));

        flows.remove(topicFilter, flow);
        assertEquals(ImmutableSet.of(topicFilter2), toSet(flow.getTopicFilters()));

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertFalse(matching.subscriptionFound);
        assertTrue(matching.isEmpty());

        final MqttMatchingPublishFlows matching2 = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic2), matching2);
        assertTrue(matching2.subscriptionFound);
        assertFalse(matching2.isEmpty());
        assertEquals(ImmutableSet.of(flow), toSet(matching2));
    }

    @ParameterizedTest
    @CsvSource({
            "1/a, 1/a, 2/a, 2/a", "1/a, 1/+, 2/a, 2/+", "1/a, 1/#, 2/a, 2/#", "1/a/b, 1/a/b, 2/a/b, 2/a/b",
            "1/a/b, 1/a/+, 2/a/b, 2/a/+", "1/a/b, 1/+/b, 2/a/b, 2/+/b", "1/a/b, 1/+/+, 2/a/b, 2/+/+",
            "1/a/b, 1/+/#, 2/a/b, 2/+/#", "1/a/b, 1/#, 2/a/b, 2/#"
    })
    void remove_noFlow(
            final @NotNull String topic, final @NotNull String matchingTopicFilter, final @NotNull String topic2,
            final @NotNull String matchingTopicFilter2) {

        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), null);
        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter2), null);

        flows.remove(MqttTopicFilterImpl.of(matchingTopicFilter), null);

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertFalse(matching.subscriptionFound);
        assertTrue(matching.isEmpty());

        final MqttMatchingPublishFlows matching2 = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic2), matching2);
        assertTrue(matching2.subscriptionFound);
        assertTrue(matching2.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void remove_doesNotUnsubscribe(final @NotNull String topic, final @NotNull String matchingTopicFilter) {
        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttTopicFilterImpl topicFilter = MqttTopicFilterImpl.of(matchingTopicFilter);
        flows.subscribe(topicFilter, flow1);
        flows.subscribe(topicFilter, flow2);
        assertEquals(ImmutableSet.of(topicFilter), toSet(flow1.getTopicFilters()));
        assertEquals(ImmutableSet.of(topicFilter), toSet(flow2.getTopicFilters()));

        flows.remove(topicFilter, flow1);
        assertTrue(flow1.getTopicFilters().isEmpty());
        assertEquals(ImmutableSet.of(topicFilter), toSet(flow2.getTopicFilters()));

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertTrue(matching.subscriptionFound);
        assertFalse(matching.isEmpty());
        assertEquals(ImmutableSet.of(flow2), toSet(matching));
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void remove_doesNotUnsubscribe_noFlow(final @NotNull String topic, final @NotNull String matchingTopicFilter) {
        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), null);
        flows.subscribe(MqttTopicFilterImpl.of(matchingTopicFilter), null);

        flows.remove(MqttTopicFilterImpl.of(matchingTopicFilter), null);

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertTrue(matching.subscriptionFound);
        assertTrue(matching.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a/b, a/b/c, +/b/c, +/+/+", "a/b/c/d, a/b/c/d/e, +/b//d/ec, +/+/+/+/+"})
    void findMatching_matchingMultipleButNotAllLevels(
            final @NotNull String topic, final @NotNull String filter1, final @NotNull String filter2,
            final @NotNull String filter3) {

        flows.subscribe(MqttTopicFilterImpl.of(filter1), null);
        flows.subscribe(MqttTopicFilterImpl.of(filter2), null);
        flows.subscribe(MqttTopicFilterImpl.of(filter3), null);

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of(topic), matching);
        assertFalse(matching.subscriptionFound);
        assertTrue(matching.isEmpty());
    }

    @Test
    void clear() {
        final MqttSubscribedPublishFlow flow1 = mockSubscriptionFlow("test/topic/filter");
        final MqttSubscribedPublishFlow flow2 = mockSubscriptionFlow("test2/topic/filter");
        final MqttSubscribedPublishFlow flow3 = mockSubscriptionFlow("test/topic2/filter");
        final MqttSubscribedPublishFlow flow4 = mockSubscriptionFlow("test/topic/filter2");
        final MqttSubscribedPublishFlow flow5 = mockSubscriptionFlow("+/topic");
        final MqttSubscribedPublishFlow flow6 = mockSubscriptionFlow("topic/#");
        flows.subscribe(MqttTopicFilterImpl.of(flow1.toString()), flow1);
        flows.subscribe(MqttTopicFilterImpl.of(flow2.toString()), flow2);
        flows.subscribe(MqttTopicFilterImpl.of(flow3.toString()), flow3);
        flows.subscribe(MqttTopicFilterImpl.of(flow4.toString()), flow4);
        flows.subscribe(MqttTopicFilterImpl.of(flow5.toString()), flow5);
        flows.subscribe(MqttTopicFilterImpl.of(flow6.toString()), flow6);

        final Exception cause = new Exception("test");
        flows.clear(cause);
        verify(flow1).onError(cause);
        verify(flow2).onError(cause);
        verify(flow3).onError(cause);
        verify(flow4).onError(cause);
        verify(flow5).onError(cause);
        verify(flow6).onError(cause);

        final MqttMatchingPublishFlows matching = new MqttMatchingPublishFlows();
        flows.findMatching(MqttTopicImpl.of("test/topic"), matching);
        flows.findMatching(MqttTopicImpl.of("test/topic/filter"), matching);
        flows.findMatching(MqttTopicImpl.of("test2/topic/filter"), matching);
        flows.findMatching(MqttTopicImpl.of("test/topic2/filter"), matching);
        flows.findMatching(MqttTopicImpl.of("test/topic/filter2"), matching);
        flows.findMatching(MqttTopicImpl.of("abc/topic"), matching);
        flows.findMatching(MqttTopicImpl.of("topic/abc"), matching);
        assertFalse(matching.subscriptionFound);
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
