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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.provider.CsvSource;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.datatypes.MqttTopicFilterImpl;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.util.collections.ScNodeList;

import java.util.Objects;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Silvio Giebl
 */
abstract class MqttSubscriptionFlowsTest {

    public static class CsvToArray extends SimpleArgumentConverter {

        @Override
        protected Object convert(final Object source, final Class<?> targetType) throws ArgumentConversionException {
            final String s = (String) source;
            return s.split("\\s*;\\s*");
        }
    }

    private final Supplier<MqttSubscriptionFlows> flowsSupplier;
    private MqttSubscriptionFlows flows;

    MqttSubscriptionFlowsTest(@NotNull final Supplier<MqttSubscriptionFlows> flowsSupplier) {
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
    void matching(final String topic, @ConvertWith(CsvToArray.class) final String[] matchingTopicFilters) {
        final MqttSubscriptionFlow[] matchingFlows = new MqttSubscriptionFlow[matchingTopicFilters.length];
        for (int i = 0; i < matchingTopicFilters.length; i++) {
            matchingFlows[i] = mockSubscriptionFlow(matchingTopicFilters[i]);
            flows.subscribe(
                    Objects.requireNonNull(MqttTopicFilterImpl.from(matchingTopicFilters[i])), matchingFlows[i]);
        }

        final ScNodeList<MqttIncomingPublishFlow> matching = new ScNodeList<>();
        flows.findMatching(Objects.requireNonNull(MqttTopicImpl.from(topic)), matching);

        assertFalse(matching.isEmpty());
        assertEquals(ImmutableSet.copyOf(matchingFlows), ImmutableSet.copyOf(matching));
    }

    @ParameterizedTest
    @CsvSource({
            "a,    /a; b; a/b; a/+; +/a; +/+; a/b/#; /#; /               ",
            "a/b,  /a/b; a/c; c/b; a/b/c; +/a/b; a/+/b; a/b/+; a/b/c/#; +",
            "/,    //; a/b; a/; /a; +                                    "
    })
    void non_matching(final String topic, @ConvertWith(CsvToArray.class) final String[] notMatchingTopicFilters) {
        final MqttSubscriptionFlow[] notMatchingFlows = new MqttSubscriptionFlow[notMatchingTopicFilters.length];
        for (int i = 0; i < notMatchingTopicFilters.length; i++) {
            notMatchingFlows[i] = mockSubscriptionFlow(notMatchingTopicFilters[i]);
            flows.subscribe(
                    Objects.requireNonNull(MqttTopicFilterImpl.from(notMatchingTopicFilters[i])), notMatchingFlows[i]);
        }

        final ScNodeList<MqttIncomingPublishFlow> matching = new ScNodeList<>();
        flows.findMatching(Objects.requireNonNull(MqttTopicImpl.from(topic)), matching);

        assertTrue(matching.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void matching_unsubscribe(final String topic, final String matchingTopicFilter) {
        final MqttSubscriptionFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscriptionFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        flows.subscribe(Objects.requireNonNull(MqttTopicFilterImpl.from(matchingTopicFilter)), flow1);
        flows.subscribe(Objects.requireNonNull(MqttTopicFilterImpl.from(matchingTopicFilter)), flow2);

        final ScNodeList<MqttSubscriptionFlow> unsubscribed = new ScNodeList<>();
        flows.unsubscribe(Objects.requireNonNull(MqttTopicFilterImpl.from(matchingTopicFilter)), unsubscribed::add);
        final ScNodeList<MqttIncomingPublishFlow> matching = new ScNodeList<>();
        flows.findMatching(Objects.requireNonNull(MqttTopicImpl.from(topic)), matching);

        assertTrue(matching.isEmpty());
        assertEquals(ImmutableSet.of(flow1, flow2), ImmutableSet.copyOf(unsubscribed));
    }

    @ParameterizedTest
    @CsvSource({"a, a, b", "a, a, a/b", "a/b, a/b, a/c"})
    void non_matching_unsubscribe(
            final String topic, final String matchingTopicFilter, final String notMatchingTopicFilter) {

        final MqttSubscriptionFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscriptionFlow flow2 = mockSubscriptionFlow(notMatchingTopicFilter);
        flows.subscribe(Objects.requireNonNull(MqttTopicFilterImpl.from(matchingTopicFilter)), flow1);
        flows.subscribe(Objects.requireNonNull(MqttTopicFilterImpl.from(notMatchingTopicFilter)), flow2);

        final ScNodeList<MqttSubscriptionFlow> unsubscribed = new ScNodeList<>();
        flows.unsubscribe(Objects.requireNonNull(MqttTopicFilterImpl.from(notMatchingTopicFilter)), unsubscribed::add);
        final ScNodeList<MqttIncomingPublishFlow> matching = new ScNodeList<>();
        flows.findMatching(Objects.requireNonNull(MqttTopicImpl.from(topic)), matching);

        assertFalse(matching.isEmpty());
        assertEquals(ImmutableSet.of(flow1), ImmutableSet.copyOf(matching));
        assertEquals(ImmutableSet.of(flow2), ImmutableSet.copyOf(unsubscribed));
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void cancel(final String topic, final String matchingTopicFilter) {
        final MqttSubscriptionFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscriptionFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        flows.subscribe(Objects.requireNonNull(MqttTopicFilterImpl.from(matchingTopicFilter)), flow1);
        flows.subscribe(Objects.requireNonNull(MqttTopicFilterImpl.from(matchingTopicFilter)), flow2);

        flows.cancel(flow1);
        ScNodeList<MqttIncomingPublishFlow> matching = new ScNodeList<>();
        flows.findMatching(Objects.requireNonNull(MqttTopicImpl.from(topic)), matching);

        assertFalse(matching.isEmpty());
        assertEquals(ImmutableSet.of(flow2), ImmutableSet.copyOf(matching));

        flows.cancel(flow2);
        matching = new ScNodeList<>();
        flows.findMatching(Objects.requireNonNull(MqttTopicImpl.from(topic)), matching);

        assertTrue(matching.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"a, a", "a, +", "a, #", "a/b, a/b", "a/b, a/+", "a/b, +/b", "a/b, +/+", "a/b, +/#", "a/b, #"})
    void cancel_not_present(final String topic, final String matchingTopicFilter) {
        final MqttSubscriptionFlow flow1 = mockSubscriptionFlow(matchingTopicFilter);
        final MqttSubscriptionFlow flow2 = mockSubscriptionFlow(matchingTopicFilter);
        flows.subscribe(Objects.requireNonNull(MqttTopicFilterImpl.from(matchingTopicFilter)), flow1);

        flows.cancel(flow2);
        final ScNodeList<MqttIncomingPublishFlow> matching = new ScNodeList<>();
        flows.findMatching(Objects.requireNonNull(MqttTopicImpl.from(topic)), matching);

        assertFalse(matching.isEmpty());
        assertEquals(ImmutableSet.of(flow1), ImmutableSet.copyOf(matching));
    }

    @NotNull
    private MqttSubscriptionFlow mockSubscriptionFlow(final String name) {
        final MqttSubscriptionFlow flow = mock(MqttSubscriptionFlow.class);
        when(flow.getTopicFilters()).thenReturn(new ScNodeList<>());
        when(flow.toString()).thenReturn(name);
        return flow;
    }

}
