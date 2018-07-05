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

package org.mqttbee.rx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** @author Silvio Giebl */
class FlowableWithSingleSplitTest {

    @Test
    void split() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, StringBuilder, String>
                flowableWithSingleSplit =
                        new FlowableWithSingleSplit<>(flowable, StringBuilder.class, String.class);

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit
                .doOnSingle(
                        (stringBuilder, subscription) ->
                                assertEquals("single", stringBuilder.toString()))
                .doOnNext(string -> assertEquals("next" + count.getAndIncrement(), string))
                .blockingSubscribe();
    }

    @Test
    void observeOnBoth() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, StringBuilder, String>
                flowableWithSingleSplit =
                        new FlowableWithSingleSplit<>(flowable, StringBuilder.class, String.class);

        final ExecutorService executorService =
                Executors.newFixedThreadPool(
                        1, new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit
                .observeOnBoth(Schedulers.from(executorService))
                .doOnSingle(
                        (stringBuilder, subscription) -> {
                            assertEquals("single", stringBuilder.toString());
                            assertEquals("test_thread", Thread.currentThread().getName());
                        })
                .doOnNext(
                        string -> {
                            assertEquals("next" + count.getAndIncrement(), string);
                            assertEquals("test_thread", Thread.currentThread().getName());
                        })
                .blockingSubscribe();
    }

    @Test
    void observeOnFlowable() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, StringBuilder, String>
                flowableWithSingleSplit =
                        new FlowableWithSingleSplit<>(flowable, StringBuilder.class, String.class);

        final ExecutorService executorService =
                Executors.newFixedThreadPool(
                        1, new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit
                .doOnSingle(
                        (stringBuilder, subscription) -> {
                            assertEquals("single", stringBuilder.toString());
                            assertNotEquals("test_thread", Thread.currentThread().getName());
                        })
                .observeOn(Schedulers.from(executorService))
                .doOnNext(
                        string -> {
                            assertEquals("next" + count.getAndIncrement(), string);
                            assertEquals("test_thread", Thread.currentThread().getName());
                        })
                .blockingSubscribe();
    }

    @Test
    void observeOnBoth_delayError() throws InterruptedException {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromIterable(
                                () ->
                                        new Iterator<CharSequence>() {
                                            int count = -1;

                                            @Override
                                            public boolean hasNext() {
                                                return true;
                                            }

                                            @Override
                                            public CharSequence next() {
                                                count++;
                                                if (count == 4) {
                                                    throw new IllegalArgumentException("test");
                                                }
                                                return (count == 0)
                                                        ? new StringBuilder("single")
                                                        : "next" + (count - 1);
                                            }
                                        })
                        .hide();
        final FlowableWithSingleSplit<? extends CharSequence, StringBuilder, String>
                flowableWithSingleSplit =
                        new FlowableWithSingleSplit<>(flowable, StringBuilder.class, String.class);

        final ExecutorService executorService =
                Executors.newFixedThreadPool(
                        1, new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit
                .observeOnBoth(Schedulers.from(executorService), true)
                .doOnSingle(
                        (stringBuilder, subscription) -> {
                            assertEquals("single", stringBuilder.toString());
                            assertEquals("test_thread", Thread.currentThread().getName());
                        })
                .doOnNext(
                        string -> {
                            assertEquals("next" + count.getAndIncrement(), string);
                            assertEquals("test_thread", Thread.currentThread().getName());
                        })
                .test(1)
                .awaitCount(1)
                .assertValuesOnly("next0")
                .requestMore(1)
                .awaitCount(2)
                .assertValuesOnly("next0", "next1")
                .requestMore(1)
                .await()
                .assertError(IllegalArgumentException.class)
                .assertErrorMessage("test");
    }

    @Test
    void observeOnBoth_delayError_bufferSize() throws InterruptedException {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromIterable(
                                () ->
                                        new Iterator<CharSequence>() {
                                            int count = -1;

                                            @Override
                                            public boolean hasNext() {
                                                return true;
                                            }

                                            @Override
                                            public CharSequence next() {
                                                count++;
                                                if (count == 1024) {
                                                    throw new IllegalArgumentException("test");
                                                }
                                                return (count == 0)
                                                        ? new StringBuilder("single")
                                                        : "next" + (count - 1);
                                            }
                                        })
                        .hide();
        final FlowableWithSingleSplit<? extends CharSequence, StringBuilder, String>
                flowableWithSingleSplit =
                        new FlowableWithSingleSplit<>(flowable, StringBuilder.class, String.class);

        final ExecutorService executorService =
                Executors.newFixedThreadPool(
                        1, new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit
                .observeOnBoth(Schedulers.from(executorService), true, 1024)
                .doOnSingle(
                        (stringBuilder, subscription) -> {
                            assertEquals("single", stringBuilder.toString());
                            assertEquals("test_thread", Thread.currentThread().getName());
                        })
                .doOnNext(
                        string -> {
                            assertEquals("next" + count.getAndIncrement(), string);
                            assertEquals("test_thread", Thread.currentThread().getName());
                        })
                .test(1)
                .awaitCount(1)
                .assertValuesOnly("next0")
                .requestMore(1022)
                .awaitCount(1023)
                .requestMore(1)
                .await()
                .assertError(IllegalArgumentException.class)
                .assertErrorMessage("test");
    }

    @Test
    void mapBoth() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, StringBuilder, String>
                flowableWithSingleSplit =
                        new FlowableWithSingleSplit<>(flowable, StringBuilder.class, String.class);

        flowableWithSingleSplit
                .mapBoth(
                        stringBuilder -> (double) stringBuilder.toString().length(), String::length)
                .doOnSingle((aDouble, subscription) -> assertEquals((Double) 6d, aDouble))
                .doOnNext(integer -> assertEquals((Integer) 5, integer))
                .blockingSubscribe();
    }

    @Test
    void mapError() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.error(new IllegalArgumentException("test"));
        final FlowableWithSingleSplit<? extends CharSequence, StringBuilder, String>
                flowableWithSingleSplit =
                        new FlowableWithSingleSplit<>(flowable, StringBuilder.class, String.class);

        final IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                flowableWithSingleSplit
                                        .mapError(
                                                throwable ->
                                                        new IllegalStateException(
                                                                throwable.getMessage()))
                                        .blockingSubscribe());
        assertEquals("test", exception.getMessage());
    }

    @Test
    void conditional_fusion() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, StringBuilder, String>
                flowableWithSingleSplit =
                        new FlowableWithSingleSplit<>(flowable, StringBuilder.class, String.class);

        final AtomicInteger count = new AtomicInteger(1);
        flowableWithSingleSplit
                .doOnSingle(
                        (stringBuilder, subscription) ->
                                assertEquals("single", stringBuilder.toString()))
                .filter(string -> !string.equals("next0"))
                .doOnNext(string -> assertEquals("next" + count.getAndIncrement(), string))
                .blockingSubscribe();
    }

    @Test
    void no_conditional_fusion_source() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2").hide();
        final FlowableWithSingleSplit<? extends CharSequence, StringBuilder, String>
                flowableWithSingleSplit =
                        new FlowableWithSingleSplit<>(flowable, StringBuilder.class, String.class);

        final AtomicInteger count = new AtomicInteger(0);
        flowableWithSingleSplit
                .doOnSingle(
                        (stringBuilder, subscription) ->
                                assertEquals("single", stringBuilder.toString()))
                .doOnNext(string -> assertEquals("next" + count.getAndIncrement(), string))
                .blockingSubscribe();
    }
}
