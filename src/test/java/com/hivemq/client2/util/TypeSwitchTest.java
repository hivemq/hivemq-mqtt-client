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

package com.hivemq.client2.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class TypeSwitchTest {

    @Test
    void never_alwaysSame() {
        final TypeSwitch<Interface> never1 = TypeSwitch.never();
        final TypeSwitch<String> never2 = TypeSwitch.never();
        assertSame(never1, never2);
    }

    @Test
    void when_true() {
        final Interface i = new Impl1();
        final AtomicInteger counter = new AtomicInteger();
        TypeSwitch.when(i).is(Impl1.class, impl1 -> counter.incrementAndGet());
        assertEquals(1, counter.get());
    }

    @Test
    void when_false() {
        final Interface i = new Impl2();
        final AtomicInteger counter = new AtomicInteger();
        TypeSwitch.when(i).is(Impl1.class, impl1 -> counter.incrementAndGet());
        assertEquals(0, counter.get());
    }

    @Test
    void when_true_false() {
        final Interface i = new Impl1();
        final AtomicInteger counter = new AtomicInteger();
        TypeSwitch.when(i).is(Impl1.class, impl1 -> counter.incrementAndGet()).is(Impl2.class, impl2 -> fail());
        assertEquals(1, counter.get());
    }

    @Test
    void when_false_true() {
        final Interface i = new Impl1();
        final AtomicInteger counter = new AtomicInteger();
        TypeSwitch.when(i).is(Impl2.class, impl2 -> fail()).is(Impl1.class, impl1 -> counter.incrementAndGet());
        assertEquals(1, counter.get());
    }

    private interface Interface {}

    private static class Impl1 implements Interface {}

    private static class Impl2 implements Interface {}
}