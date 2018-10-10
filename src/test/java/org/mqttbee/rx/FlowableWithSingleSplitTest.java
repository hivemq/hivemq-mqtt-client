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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mqttbee.rx.FlowableWithSingle.SingleAndDisposable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class FlowableWithSingleSplitTest {

    @Test
    void doOnSingle() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit.doOnSingle(stringBuilder -> assertEquals("single", stringBuilder.toString()))
                .doOnNext(string -> assertEquals("next" + count.getAndIncrement(), string))
                .blockingSubscribe();
    }

    @Test
    void observeOnBoth() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit.observeOnBoth(Schedulers.from(executorService)).doOnSingle(stringBuilder -> {
            assertEquals("single", stringBuilder.toString());
            assertEquals("test_thread", Thread.currentThread().getName());
        }).doOnNext(string -> {
            assertEquals("next" + count.getAndIncrement(), string);
            assertEquals("test_thread", Thread.currentThread().getName());
        }).blockingSubscribe();

        executorService.shutdown();
    }

    @Test
    void observeOnFlowable() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit.doOnSingle(stringBuilder -> {
            assertEquals("single", stringBuilder.toString());
            assertNotEquals("test_thread", Thread.currentThread().getName());
        }).observeOn(Schedulers.from(executorService)).doOnNext(string -> {
            assertEquals("next" + count.getAndIncrement(), string);
            assertEquals("test_thread", Thread.currentThread().getName());
        }).blockingSubscribe();

        executorService.shutdown();
    }

    @Test
    void observeOnBoth_delayError() throws InterruptedException {
        final Flowable<? extends CharSequence> flowable = Flowable.fromIterable(() -> new Iterator<CharSequence>() {
            int count = -1;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public @NotNull CharSequence next() {
                count++;
                if (count == 4) {
                    throw new IllegalArgumentException("test");
                }
                return (count == 0) ? new StringBuilder("single") : "next" + (count - 1);
            }
        }).hide();
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit.observeOnBoth(Schedulers.from(executorService), true)
                .doOnSingle(stringBuilder -> {
                    assertEquals("single", stringBuilder.toString());
                    assertEquals("test_thread", Thread.currentThread().getName());
                })
                .doOnNext(string -> {
                    assertEquals("next" + count.getAndIncrement(), string);
                    assertEquals("test_thread", Thread.currentThread().getName());
                })
                .test(1)
                .awaitCount(1)
                .assertValue("next0")
                .requestMore(1)
                .awaitCount(2)
                .assertValues("next0", "next1")
                .requestMore(1)
                .await()
                .assertError(IllegalArgumentException.class)
                .assertErrorMessage("test");

        executorService.shutdown();
    }

    @Test
    void observeOnBoth_delayError_bufferSize() throws InterruptedException {
        final Flowable<? extends CharSequence> flowable = Flowable.fromIterable(() -> new Iterator<CharSequence>() {
            int count = -1;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public @NotNull CharSequence next() {
                count++;
                if (count == 1024) {
                    throw new IllegalArgumentException("test");
                }
                return (count == 0) ? new StringBuilder("single") : "next" + (count - 1);
            }
        }).hide();
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit.observeOnBoth(Schedulers.from(executorService), true, 1024)
                .doOnSingle(stringBuilder -> {
                    assertEquals("single", stringBuilder.toString());
                    assertEquals("test_thread", Thread.currentThread().getName());
                })
                .doOnNext(string -> {
                    assertEquals("next" + count.getAndIncrement(), string);
                    assertEquals("test_thread", Thread.currentThread().getName());
                })
                .test(1)
                .awaitCount(1)
                .assertValue("next0")
                .requestMore(1022)
                .awaitCount(1023)
                .requestMore(1)
                .await()
                .assertError(IllegalArgumentException.class)
                .assertErrorMessage("test");

        executorService.shutdown();
    }

    @Test
    void mapSingle() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final AtomicInteger count = new AtomicInteger();
        flowableWithSingleSplit.mapSingle(stringBuilder -> (double) stringBuilder.toString().length())
                .doOnSingle(aDouble -> assertEquals((Double) 6d, aDouble))
                .doOnNext(string -> assertEquals("next" + count.getAndIncrement(), string))
                .blockingSubscribe();
    }

    @Test
    void mapBoth() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        flowableWithSingleSplit.mapBoth(String::length, stringBuilder -> (double) stringBuilder.toString().length())
                .doOnSingle(aDouble -> assertEquals((Double) 6d, aDouble))
                .doOnNext(integer -> assertEquals((Integer) 5, integer))
                .blockingSubscribe();
    }

    @Test
    void mapError() {
        final Flowable<? extends CharSequence> flowable = Flowable.error(new IllegalArgumentException("test"));
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> flowableWithSingleSplit.mapError(throwable -> new IllegalStateException(throwable.getMessage()))
                        .blockingSubscribe());
        assertEquals("test", exception.getMessage());
    }

    @Test
    void doOnSingle_multiple() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger counter = new AtomicInteger();
        flowableWithSingleSplit //
                .doOnSingle(stringBuilder -> {
                    assertEquals(1, counter.incrementAndGet());
                    assertNotEquals("test_thread", Thread.currentThread().getName());
                })
                .doOnSingle(stringBuilder -> {
                    assertEquals(2, counter.incrementAndGet());
                    assertNotEquals("test_thread", Thread.currentThread().getName());
                })
                .observeOnBoth(Schedulers.from(executorService))
                .doOnSingle(stringBuilder -> {
                    assertEquals(3, counter.incrementAndGet());
                    assertEquals("test_thread", Thread.currentThread().getName());
                })
                .mapBoth(String::length, stringBuilder -> (double) stringBuilder.toString().length())
                .doOnSingle(stringBuilder -> {
                    assertEquals(4, counter.incrementAndGet());
                    assertEquals("test_thread", Thread.currentThread().getName());
                })
                .blockingSubscribe();

        assertEquals(4, counter.get());
    }

    @Test
    void subscribeBoth() throws InterruptedException {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final CountDownLatch latch = new CountDownLatch(6);
        flowableWithSingleSplit.subscribeBoth(new FlowableWithSingleSubscriber<String, StringBuilder>() {
            @Override
            public void onSubscribe(final @NotNull Subscription s) {
                assertEquals(6, latch.getCount());
                latch.countDown();
                s.request(10);
            }

            @Override
            public void onSingle(final @NotNull StringBuilder stringBuilder) {
                assertEquals("single", stringBuilder.toString());
                assertEquals(5, latch.getCount());
                latch.countDown();
            }

            @Override
            public void onNext(final @NotNull String s) {
                latch.countDown();
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }

            @Override
            public void onError(final @NotNull Throwable t) {
            }
        });
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void subscribeBlockUntilSingle() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2")
                        .zipWith(Flowable.interval(10, 10, TimeUnit.MILLISECONDS), (o, aLong) -> o);
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        assertTimeout(Duration.ofMillis(100), () -> {
            final SingleAndDisposable<StringBuilder> singleAndDisposable =
                    flowableWithSingleSplit.subscribeBlockUntilSingle();
            assertEquals("single", singleAndDisposable.getSingle().toString());
            singleAndDisposable.getDisposable().dispose();
        });
    }

    @Test
    void subscribeBlockUntilSingle_onNext() throws InterruptedException {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2")
                        .zipWith(Flowable.interval(10, 10, TimeUnit.MILLISECONDS), (o, aLong) -> o);
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final CountDownLatch latch = new CountDownLatch(3);
        assertTimeout(Duration.ofMillis(100), () -> {
            final SingleAndDisposable<StringBuilder> singleAndDisposable =
                    flowableWithSingleSplit.subscribeBlockUntilSingle(s -> latch.countDown());
            assertEquals("single", singleAndDisposable.getSingle().toString());
        });
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void subscribeBlockUntilSingle_onNext_onError() throws InterruptedException {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2")
                        .zipWith(Flowable.interval(10, 10, TimeUnit.MILLISECONDS), (o, aLong) -> o);
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final CountDownLatch latch = new CountDownLatch(3);
        assertTimeout(Duration.ofMillis(100), () -> {
            final SingleAndDisposable<StringBuilder> singleAndDisposable =
                    flowableWithSingleSplit.subscribeBlockUntilSingle(s -> latch.countDown(), throwable -> {
                    });
            assertEquals("single", singleAndDisposable.getSingle().toString());
        });
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void subscribeBlockUntilSingle_onNext_onError_onComplete() throws InterruptedException {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2")
                        .zipWith(Flowable.interval(10, 10, TimeUnit.MILLISECONDS), (o, aLong) -> o);
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final CountDownLatch latch = new CountDownLatch(4);
        assertTimeout(Duration.ofMillis(100), () -> {
            final SingleAndDisposable<StringBuilder> singleAndDisposable =
                    flowableWithSingleSplit.subscribeBlockUntilSingle(s -> latch.countDown(), throwable -> {
                    }, latch::countDown);
            assertEquals("single", singleAndDisposable.getSingle().toString());
        });
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void subscribeBlockUntilSingle_subscriber() throws InterruptedException {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2")
                        .zipWith(Flowable.interval(10, 10, TimeUnit.MILLISECONDS), (o, aLong) -> o);
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final CountDownLatch latch = new CountDownLatch(5);
        assertTimeout(Duration.ofMillis(100), () -> {
            final StringBuilder single = flowableWithSingleSplit.subscribeBlockUntilSingle(new Subscriber<String>() {
                @Override
                public void onSubscribe(final @NotNull Subscription s) {
                    latch.countDown();
                    s.request(10);
                }

                @Override
                public void onNext(final @NotNull String s) {
                    latch.countDown();
                }

                @Override
                public void onComplete() {
                    latch.countDown();
                }

                @Override
                public void onError(final @NotNull Throwable t) {
                }
            });
            assertEquals("single", single.toString());
        });
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @Test
    void conditional_fusion() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2");
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final AtomicInteger count = new AtomicInteger(1);
        flowableWithSingleSplit.doOnSingle(stringBuilder -> assertEquals("single", stringBuilder.toString()))
                .filter(string -> !string.equals("next0"))
                .doOnNext(string -> assertEquals("next" + count.getAndIncrement(), string))
                .blockingSubscribe();
    }

    @Test
    void no_conditional_fusion_source() {
        final Flowable<? extends CharSequence> flowable =
                Flowable.fromArray(new StringBuilder("single"), "next0", "next1", "next2").hide();
        final FlowableWithSingleSplit<? extends CharSequence, String, StringBuilder> flowableWithSingleSplit =
                new FlowableWithSingleSplit<>(flowable, String.class, StringBuilder.class);

        final AtomicInteger count = new AtomicInteger(0);
        flowableWithSingleSplit.doOnSingle(stringBuilder -> assertEquals("single", stringBuilder.toString()))
                .doOnNext(string -> assertEquals("next" + count.getAndIncrement(), string))
                .blockingSubscribe();
    }

}