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

package com.hivemq.client.rx.reactor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hivemq.client.rx.reactivestreams.PublisherWithSingle;
import com.hivemq.client.rx.reactivestreams.WithSingleSubscriber;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Silvio Giebl
 */
class FluxWithSingleTest {

    private static @NotNull Stream<FluxWithSingle<String, StringBuilder>> singleNext3() {
        return Stream.of(new FluxWithSingleSplit<>(
                        Flux.fromIterable(Arrays.asList(new StringBuilder("single"), "next0", "next1", "next2")), String.class,
                        StringBuilder.class),
                new FluxWithSingleItem<>(Flux.fromIterable(Arrays.asList("next0", "next1", "next2")),
                        new StringBuilder("single"), 0));
    }

    @Test
    void from() {
        final PublisherWithSingle<?, ?> publisherWithSingle = mock(PublisherWithSingle.class);
        final FluxWithSingle<?, ?> fluxWithSingle = FluxWithSingle.from(publisherWithSingle);
        assertNotSame(publisherWithSingle, fluxWithSingle);

        final WithSingleSubscriber<Object, Object> subscriber = new WithSingleSubscriber<Object, Object>() {
            @Override
            public void onSingle(final @NotNull Object o) {}

            @Override
            public void onSubscribe(final @NotNull Subscription s) {}

            @Override
            public void onNext(final @NotNull Object o) {}

            @Override
            public void onError(final @NotNull Throwable t) {}

            @Override
            public void onComplete() {}
        };
        fluxWithSingle.subscribe(subscriber);
        verify(publisherWithSingle).subscribe(any());
        fluxWithSingle.subscribeBoth(subscriber);
        verify(publisherWithSingle).subscribeBoth(any());
    }

    @Test
    void from_fluxWithSingle_returnsSame() {
        final FluxWithSingle<?, ?> publisherWithSingle = mock(FluxWithSingle.class);
        final FluxWithSingle<?, ?> fluxWithSingle = FluxWithSingle.from(publisherWithSingle);
        assertSame(publisherWithSingle, fluxWithSingle);
    }

    @MethodSource("singleNext3")
    @ParameterizedTest
    void doOnSingle(final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle) {
        final AtomicInteger count = new AtomicInteger();
        fluxWithSingle.doOnSingle(stringBuilder -> assertEquals("single", stringBuilder.toString()))
                .doOnNext(string -> assertEquals("next" + count.getAndIncrement(), string))
                .blockLast();
        assertEquals(3, count.get());
    }

    @MethodSource("singleNext3")
    @ParameterizedTest
    void publishBothOn(final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle) {
        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        fluxWithSingle.publishBothOn(Schedulers.fromExecutor(executorService)).doOnSingle(stringBuilder -> {
            assertEquals("single", stringBuilder.toString());
            assertEquals("test_thread", Thread.currentThread().getName());
        }).doOnNext(string -> {
            assertEquals("next" + count.getAndIncrement(), string);
            assertEquals("test_thread", Thread.currentThread().getName());
        }).blockLast();
        assertEquals(3, count.get());

        executorService.shutdown();
    }

    @Test
    void publishBothOn_request() {
        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleItem<>(Flux.range(0, 10).map(i -> "next" + i), new StringBuilder("single"), 0);

        final AtomicInteger count = new AtomicInteger();
        // bufferSize 4 -> requests 3 -> checks if request for single item leads to request 2 upstream
        fluxWithSingle.publishBothOn(Schedulers.fromExecutor(executorService), false, 4).doOnSingle(stringBuilder -> {
            assertEquals("single", stringBuilder.toString());
            assertEquals("test_thread", Thread.currentThread().getName());
        }).doOnNext(string -> {
            assertEquals("next" + count.getAndIncrement(), string);
            assertEquals("test_thread", Thread.currentThread().getName());
        }).blockLast();
        assertEquals(10, count.get());

        executorService.shutdown();
    }

    @MethodSource("singleNext3")
    @ParameterizedTest
    void publishOn(final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle) {
        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        fluxWithSingle.doOnSingle(stringBuilder -> {
            assertEquals("single", stringBuilder.toString());
            assertNotEquals("test_thread", Thread.currentThread().getName());
        }).publishOn(Schedulers.fromExecutor(executorService)).doOnNext(string -> {
            assertEquals("next" + count.getAndIncrement(), string);
            assertEquals("test_thread", Thread.currentThread().getName());
        }).blockLast();
        assertEquals(3, count.get());

        executorService.shutdown();
    }

    @Test
    void publishBothOn_prefetch() {
        final Flux<? extends CharSequence> flux =
                Flux.fromIterable(Arrays.asList(new StringBuilder("single"), "next0", "next1", "next2"))
                        .concatWith(Flux.error(new IllegalArgumentException("test")))
                        .hide();
        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        StepVerifier.create(
                fluxWithSingle.publishBothOn(Schedulers.fromExecutor(executorService), 16).doOnSingle(stringBuilder -> {
                    assertEquals("single", stringBuilder.toString());
                    assertEquals("test_thread", Thread.currentThread().getName());
                }).doOnNext(string -> {
                    assertEquals("next" + count.getAndIncrement(), string);
                    assertEquals("test_thread", Thread.currentThread().getName());
                }), 1)
                .expectNext("next0")
                .thenRequest(1)
                .expectNext("next1")
                .thenRequest(1)
                .expectNext("next2")
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("test"))
                .verify();

        executorService.shutdown();
    }

    @Test
    void publishBothOn_delayError_prefetch() {
        final Flux<? extends CharSequence> flux = Flux.<CharSequence>just(new StringBuilder("single")).concatWith(
                Flux.range(0, 1024).zipWith(Flux.just("next").repeat(1024), (i, s) -> s + i))
                .concatWith(Flux.error(new IllegalArgumentException("test")))
                .hide();
        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger count = new AtomicInteger();
        StepVerifier.create(fluxWithSingle.publishBothOn(Schedulers.fromExecutor(executorService), true, 1024)
                .doOnSingle(stringBuilder -> {
                    assertEquals("single", stringBuilder.toString());
                    assertEquals("test_thread", Thread.currentThread().getName());
                })
                .doOnNext(string -> {
                    assertEquals("next" + count.getAndIncrement(), string);
                    assertEquals("test_thread", Thread.currentThread().getName());
                }), 1)
                .expectNext("next0")
                .thenRequest(1022)
                .expectNextCount(1022)
                .thenRequest(1)
                .expectNextCount(1)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("test"))
                .verify();

        executorService.shutdown();
    }

    @MethodSource("singleNext3")
    @ParameterizedTest
    void mapSingle(final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle) {
        final AtomicInteger count = new AtomicInteger();
        fluxWithSingle.mapSingle(stringBuilder -> (double) stringBuilder.toString().length())
                .doOnSingle(aDouble -> assertEquals((Double) 6d, aDouble))
                .doOnNext(string -> assertEquals("next" + count.getAndIncrement(), string))
                .blockLast();
        assertEquals(3, count.get());
    }

    @MethodSource("singleNext3")
    @ParameterizedTest
    void mapBoth(final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle) {
        fluxWithSingle.mapBoth(String::length, stringBuilder -> (double) stringBuilder.toString().length())
                .doOnSingle(aDouble -> assertEquals((Double) 6d, aDouble))
                .doOnNext(integer -> assertEquals((Integer) 5, integer))
                .blockLast();
    }

    @MethodSource("singleNext3")
    @ParameterizedTest
    void doOnSingle_multiple(final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle) {
        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger counter = new AtomicInteger();
        fluxWithSingle //
                .doOnSingle(stringBuilder -> {
                    assertEquals(1, counter.incrementAndGet());
                    assertNotEquals("test_thread", Thread.currentThread().getName());
                })
                .doOnSingle(stringBuilder -> {
                    assertEquals(2, counter.incrementAndGet());
                    assertNotEquals("test_thread", Thread.currentThread().getName());
                })
                .publishBothOn(Schedulers.fromExecutor(executorService))
                .doOnSingle(stringBuilder -> {
                    assertEquals(3, counter.incrementAndGet());
                    assertEquals("test_thread", Thread.currentThread().getName());
                })
                .mapBoth(String::length, stringBuilder -> (double) stringBuilder.toString().length())
                .doOnSingle(stringBuilder -> {
                    assertEquals(4, counter.incrementAndGet());
                    assertEquals("test_thread", Thread.currentThread().getName());
                })
                .blockLast();
        assertEquals(4, counter.get());

        executorService.shutdown();
    }

    @MethodSource("singleNext3")
    @ParameterizedTest
    void mapBoth_multiple(final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle) {
        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final AtomicInteger nextCounter = new AtomicInteger();
        final AtomicInteger singleCounter = new AtomicInteger();
        fluxWithSingle.mapBoth(s -> {
            nextCounter.incrementAndGet();
            assertNotEquals("test_thread", Thread.currentThread().getName());
            return s + "-1";
        }, stringBuilder -> {
            assertEquals(1, singleCounter.incrementAndGet());
            assertNotEquals("test_thread", Thread.currentThread().getName());
            return stringBuilder.append("-1");
        }).mapBoth(s -> {
            nextCounter.incrementAndGet();
            assertNotEquals("test_thread", Thread.currentThread().getName());
            return s + "-2";
        }, stringBuilder -> {
            assertEquals(2, singleCounter.incrementAndGet());
            assertNotEquals("test_thread", Thread.currentThread().getName());
            return stringBuilder.append("-2");
        }).publishBothOn(Schedulers.fromExecutor(executorService)).mapBoth(s -> {
            nextCounter.incrementAndGet();
            assertEquals("test_thread", Thread.currentThread().getName());
            return s + "-3";
        }, stringBuilder -> {
            assertEquals(3, singleCounter.incrementAndGet());
            assertEquals("test_thread", Thread.currentThread().getName());
            return stringBuilder.append("-3");
        }).blockLast();

        assertEquals(9, nextCounter.get());
        assertEquals(3, singleCounter.get());

        executorService.shutdown();
    }

    @Test
    void transformFlux_sync_singleAtSamePosition() {
        final FluxWithSingleItem<String, String> fluxWithSingle =
                new FluxWithSingleItem<>(Flux.fromIterable(Arrays.asList("next0", "next1", "next2")), "single", 2);

        final LinkedList<Object> list = new LinkedList<>();
        final String mainThreadName = Thread.currentThread().getName();
        fluxWithSingle.transformFlux(upstream -> upstream.map(String::getBytes).map(String::new)).doOnSingle(s -> {
            assertEquals(mainThreadName, Thread.currentThread().getName());
            list.add(s);
        }).doOnNext(s -> {
            assertEquals(mainThreadName, Thread.currentThread().getName());
            list.add(s);
        }).subscribe();

        assertEquals(Arrays.asList("next0", "next1", "single", "next2"), list);
    }

    @Test
    void transformFlux_async_singleAtDifferentPositionButSerial() {
        final FluxWithSingleItem<String, String> fluxWithSingle =
                new FluxWithSingleItem<>(Flux.fromIterable(Arrays.asList("next0", "next1", "next2")), "single", 2);

        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final CountDownLatch singleLatch = new CountDownLatch(1);
        final CountDownLatch fluxLatch = new CountDownLatch(2);
        final LinkedList<Object> list = new LinkedList<>();
        final String mainThreadName = Thread.currentThread().getName();
        fluxWithSingle //
                .transformFlux(upstream -> upstream.publishOn(Schedulers.fromExecutor(executorService)).doOnNext(s -> {
                    try {
                        singleLatch.await();
                    } catch (final InterruptedException e) {
                        fail(e);
                    }
                    fluxLatch.countDown();
                })) //
                .doOnSingle(s -> {
                    singleLatch.countDown();
                    try {
                        fluxLatch.await();
                    } catch (final InterruptedException e) {
                        fail(e);
                    }
                    assertEquals(mainThreadName, Thread.currentThread().getName());
                    list.add(s);
                }) //
                .doOnNext(s -> {
                    if (s.equals("next0")) {
                        assertEquals(mainThreadName, Thread.currentThread().getName());
                    }
                    list.add(s);
                }) //
                .blockLast();

        assertEquals(Arrays.asList("single", "next0", "next1", "next2"), list);

        executorService.shutdown();
    }

    @Test
    void transformFlux_async_earlierCompleteButSerial() {
        final FluxWithSingleItem<String, String> fluxWithSingle =
                new FluxWithSingleItem<>(Flux.fromIterable(Arrays.asList("next0", "next1", "next2")), "single", 2);

        final ExecutorService executorService =
                Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("test_thread").build());

        final CountDownLatch singleLatch = new CountDownLatch(1);
        final CountDownLatch fluxLatch = new CountDownLatch(2);
        final LinkedList<Object> list = new LinkedList<>();
        final String mainThreadName = Thread.currentThread().getName();
        fluxWithSingle //
                .transformFlux(upstream -> upstream.publishOn(Schedulers.fromExecutor(executorService)).take(1) //
                        .doOnNext(s -> {
                            try {
                                singleLatch.await();
                            } catch (final InterruptedException e) {
                                fail(e);
                            }
                            fluxLatch.countDown();
                        }) //
                        .doAfterTerminate(fluxLatch::countDown)) //
                .doOnSingle(s -> {
                    singleLatch.countDown();
                    try {
                        fluxLatch.await();
                    } catch (final InterruptedException e) {
                        fail(e);
                    }
                    assertEquals(mainThreadName, Thread.currentThread().getName());
                    list.add(s);
                }) //
                .doOnNext(s -> {
                    assertEquals(mainThreadName, Thread.currentThread().getName());
                    list.add(s);
                }) //
                .doOnComplete(() -> assertEquals(mainThreadName, Thread.currentThread().getName())) //
                .blockLast();

        assertEquals(Arrays.asList("single", "next0"), list);

        executorService.shutdown();
    }

    @MethodSource("singleNext3")
    @ParameterizedTest
    void subscribeBoth(final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle)
            throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(6);
        final WithSingleSubscriber<String, StringBuilder> subscriber =
                new CoreWithSingleSubscriber<String, StringBuilder>() {
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
                };
        fluxWithSingle.subscribeBoth(subscriber);
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @MethodSource("singleNext3")
    @ParameterizedTest
    void subscribeBoth_strict(final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle)
            throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(6);
        final WithSingleSubscriber<String, StringBuilder> subscriber =
                new WithSingleSubscriber<String, StringBuilder>() {
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
                };
        fluxWithSingle.subscribeBoth(subscriber);
        assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }

    @ValueSource(ints = {0, 1, 2, 3, 4})
    @ParameterizedTest
    void subscribeSingleFuture_immediate(final int args) throws ExecutionException, InterruptedException {
        final Flux<? extends CharSequence> flux =
                Flux.fromIterable(Arrays.asList(new StringBuilder("single"), "next0", "next1", "next2"));
        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final AtomicInteger onNextCounter = new AtomicInteger();
        final AtomicInteger onErrorCounter = new AtomicInteger();
        final AtomicInteger onCompleteCounter = new AtomicInteger();
        final CompletableFuture<StringBuilder> future =
                subscribeSingleFuture(args, fluxWithSingle, onNextCounter, onErrorCounter, onCompleteCounter);
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("single", future.get().toString());
        switch (args) {
            case 4:
                // fallthrough
            case 3:
                assertEquals(1, onCompleteCounter.get());
                // fallthrough
            case 2:
                assertEquals(0, onErrorCounter.get());
                // fallthrough
            case 1:
                assertEquals(3, onNextCounter.get());
        }
    }

    @ValueSource(ints = {0, 1, 2, 3, 4})
    @ParameterizedTest
    void subscribeSingleFuture_immediate_single_in_between(final int args)
            throws ExecutionException, InterruptedException {

        final Flux<? extends CharSequence> flux =
                Flux.fromIterable(Arrays.asList("next0", "next1", new StringBuilder("single"), "next2"));
        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final AtomicInteger onNextCounter = new AtomicInteger();
        final AtomicInteger onErrorCounter = new AtomicInteger();
        final AtomicInteger onCompleteCounter = new AtomicInteger();
        final CompletableFuture<StringBuilder> future =
                subscribeSingleFuture(args, fluxWithSingle, onNextCounter, onErrorCounter, onCompleteCounter);
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertFalse(future.isCompletedExceptionally());
        assertEquals("single", future.get().toString());
        switch (args) {
            case 4:
                // fallthrough
            case 3:
                assertEquals(1, onCompleteCounter.get());
                // fallthrough
            case 2:
                assertEquals(0, onErrorCounter.get());
                // fallthrough
            case 1:
                assertEquals(3, onNextCounter.get());
        }
    }

    @ValueSource(ints = {0, 1, 2, 3, 4})
    @ParameterizedTest
    void subscribeSingleFuture_immediate_no_single(final int args) {
        final Flux<? extends CharSequence> flux = Flux.fromIterable(Arrays.asList("next0", "next1", "next2"));
        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final AtomicInteger onNextCounter = new AtomicInteger();
        final AtomicInteger onErrorCounter = new AtomicInteger();
        final AtomicInteger onCompleteCounter = new AtomicInteger();
        final CompletableFuture<StringBuilder> future =
                subscribeSingleFuture(args, fluxWithSingle, onNextCounter, onErrorCounter, onCompleteCounter);
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertTrue(future.isCompletedExceptionally());
        final ExecutionException executionException = assertThrows(ExecutionException.class, future::get);
        assertTrue(executionException.getCause() instanceof NoSuchElementException);
        switch (args) {
            case 4:
                // fallthrough
            case 3:
                assertEquals(1, onCompleteCounter.get());
                // fallthrough
            case 2:
                assertEquals(0, onErrorCounter.get());
                // fallthrough
            case 1:
                assertEquals(3, onNextCounter.get());
        }
    }

    @ValueSource(ints = {2, 3, 4})
    @ParameterizedTest
    void subscribeSingleFuture_immediate_error(final int args) {
        final Flux<? extends CharSequence> flux = Flux.error(new Exception("test"));
        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final AtomicInteger onNextCounter = new AtomicInteger();
        final AtomicInteger onErrorCounter = new AtomicInteger();
        final AtomicInteger onCompleteCounter = new AtomicInteger();
        final CompletableFuture<StringBuilder> future =
                subscribeSingleFuture(args, fluxWithSingle, onNextCounter, onErrorCounter, onCompleteCounter);
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertTrue(future.isCompletedExceptionally());
        final ExecutionException executionException = assertThrows(ExecutionException.class, future::get);
        assertEquals("test", executionException.getCause().getMessage());
        switch (args) {
            case 4:
                // fallthrough
            case 3:
                assertEquals(0, onCompleteCounter.get());
                // fallthrough
            case 2:
                assertEquals(1, onErrorCounter.get());
                // fallthrough
            case 1:
                assertEquals(0, onNextCounter.get());
        }
    }

    @ValueSource(ints = {0, 1, 2, 3, 4})
    @ParameterizedTest
    void subscribeSingleFuture(final int args) throws InterruptedException {
        final Flux<? extends CharSequence> flux =
                Flux.fromIterable(Arrays.asList(new StringBuilder("single"), "next0", "next1", "next2"))
                        .zipWith(Flux.interval(Duration.ofMillis(10), Duration.ofMillis(10)), (o, aLong) -> o);
        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final AtomicInteger onNextCounter = new AtomicInteger();
        final AtomicInteger onErrorCounter = new AtomicInteger();
        final AtomicInteger onCompleteCounter = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch(3 + ((args >= 3) ? 1 : 0));
        final CompletableFuture<StringBuilder> future =
                subscribeSingleFuture(args, fluxWithSingle, onNextCounter, onErrorCounter, onCompleteCounter, latch);
        assertTimeout(Duration.ofMillis(100), () -> assertEquals("single", future.get().toString()));
        if (args == 0) {
            future.cancel(false);
        } else {
            assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
            switch (args) {
                case 4:
                    // fallthrough
                case 3:
                    assertEquals(1, onCompleteCounter.get());
                    // fallthrough
                case 2:
                    assertEquals(0, onErrorCounter.get());
                    // fallthrough
                case 1:
                    assertEquals(3, onNextCounter.get());
            }
        }
    }

    @ValueSource(ints = {0, 1, 2, 3, 4})
    @ParameterizedTest
    void subscribeSingleFuture_single_in_between(final int args) throws InterruptedException {
        final Flux<? extends CharSequence> flux =
                Flux.fromIterable(Arrays.asList("next0", "next1", new StringBuilder("single"), "next2"))
                        .zipWith(Flux.interval(Duration.ofMillis(10), Duration.ofMillis(10)), (o, aLong) -> o);
        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final AtomicInteger onNextCounter = new AtomicInteger();
        final AtomicInteger onErrorCounter = new AtomicInteger();
        final AtomicInteger onCompleteCounter = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch(3 + ((args >= 3) ? 1 : 0));
        final CompletableFuture<StringBuilder> future =
                subscribeSingleFuture(args, fluxWithSingle, onNextCounter, onErrorCounter, onCompleteCounter, latch);
        assertTimeout(Duration.ofMillis(100), () -> assertEquals("single", future.get().toString()));
        if (args == 0) {
            future.cancel(false);
        } else {
            assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
            switch (args) {
                case 4:
                    // fallthrough
                case 3:
                    assertEquals(1, onCompleteCounter.get());
                    // fallthrough
                case 2:
                    assertEquals(0, onErrorCounter.get());
                    // fallthrough
                case 1:
                    assertEquals(3, onNextCounter.get());
            }
        }
    }

    @ValueSource(ints = {0, 1, 2, 3, 4})
    @ParameterizedTest
    void subscribeSingleFuture_no_single(final int args) throws InterruptedException {
        final Flux<? extends CharSequence> flux = Flux.fromIterable(Arrays.asList("next0", "next1", "next2"))
                .zipWith(Flux.interval(Duration.ofMillis(10), Duration.ofMillis(10)), (o, aLong) -> o);
        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final AtomicInteger onNextCounter = new AtomicInteger();
        final AtomicInteger onErrorCounter = new AtomicInteger();
        final AtomicInteger onCompleteCounter = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch(3 + ((args >= 3) ? 1 : 0));
        final CompletableFuture<StringBuilder> future =
                subscribeSingleFuture(args, fluxWithSingle, onNextCounter, onErrorCounter, onCompleteCounter, latch);
        final ExecutionException executionException = assertThrows(ExecutionException.class,
                () -> assertTimeout(Duration.ofMillis(100), (ThrowingSupplier<StringBuilder>) future::get));
        assertTrue(executionException.getCause() instanceof NoSuchElementException);
        if (args == 0) {
            future.cancel(false);
        } else {
            assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
            switch (args) {
                case 4:
                    // fallthrough
                case 3:
                    assertEquals(1, onCompleteCounter.get());
                    // fallthrough
                case 2:
                    assertEquals(0, onErrorCounter.get());
                    // fallthrough
                case 1:
                    assertEquals(3, onNextCounter.get());
            }
        }
    }

    @ValueSource(ints = {2, 3, 4})
    @ParameterizedTest
    void subscribeSingleFuture_error(final int args) throws InterruptedException {
        final Flux<? extends CharSequence> flux =
                Flux.<CharSequence>error(new Exception("test")).delaySequence(Duration.ofMillis(10));
        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final AtomicInteger onNextCounter = new AtomicInteger();
        final AtomicInteger onErrorCounter = new AtomicInteger();
        final AtomicInteger onCompleteCounter = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch((args >= 2) ? 1 : 0);
        final CompletableFuture<StringBuilder> future =
                subscribeSingleFuture(args, fluxWithSingle, onNextCounter, onErrorCounter, onCompleteCounter, latch);
        final ExecutionException executionException = assertThrows(ExecutionException.class,
                () -> assertTimeout(Duration.ofMillis(100), (ThrowingSupplier<StringBuilder>) future::get));
        assertEquals("test", executionException.getCause().getMessage());
        if (args == 0) {
            future.cancel(false);
        } else {
            assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
            switch (args) {
                case 4:
                    // fallthrough
                case 3:
                    assertEquals(0, onCompleteCounter.get());
                    // fallthrough
                case 2:
                    assertEquals(1, onErrorCounter.get());
                    // fallthrough
                case 1:
                    assertEquals(0, onNextCounter.get());
            }
        }
    }

    @Test
    void subscribeSingleFuture_cancel() throws InterruptedException {
        final CountDownLatch subscribeLatch = new CountDownLatch(1);
        final CountDownLatch cancelLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(1);
        final Flux<? extends CharSequence> flux = Flux.<CharSequence>create(emitter -> {
            subscribeLatch.countDown();
            try {
                assertTrue(cancelLatch.await(100, TimeUnit.MILLISECONDS));
            } catch (final InterruptedException e) {
                // ignore
            }
            assertTrue(emitter.isCancelled());
            completeLatch.countDown();
        }, FluxSink.OverflowStrategy.IGNORE).subscribeOn(Schedulers.single());

        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final CompletableFuture<StringBuilder> future = fluxWithSingle.subscribeSingleFuture();
        assertTrue(subscribeLatch.await(100, TimeUnit.MILLISECONDS));
        future.cancel(false);
        cancelLatch.countDown();
        assertTrue(completeLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void subscribeSingleFuture_cancel_after_single() throws InterruptedException {
        final CountDownLatch cancelLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(1);
        final Flux<? extends CharSequence> flux = Flux.<CharSequence>create(emitter -> {
            emitter.next(new StringBuilder("single"));
            try {
                assertTrue(cancelLatch.await(100, TimeUnit.MILLISECONDS));
            } catch (final InterruptedException e) {
                // ignore
            }
            assertTrue(emitter.isCancelled());
            completeLatch.countDown();
        }, FluxSink.OverflowStrategy.IGNORE).subscribeOn(Schedulers.single());

        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final CompletableFuture<StringBuilder> future = fluxWithSingle.subscribeSingleFuture();
        assertTimeout(Duration.ofMillis(100), () -> assertEquals("single", future.get().toString()));
        future.cancel(false);
        cancelLatch.countDown();
        assertTrue(completeLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void subscribeSingleFuture_cancel_after_next() throws InterruptedException {
        final CountDownLatch cancelLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(1);
        final Flux<? extends CharSequence> flux = Flux.<CharSequence>create(emitter -> {
            emitter.next(new StringBuilder("single"));
            emitter.next("next0");
            try {
                assertTrue(cancelLatch.await(100, TimeUnit.MILLISECONDS));
            } catch (final InterruptedException e) {
                // ignore
            }
            assertTrue(emitter.isCancelled());
            completeLatch.countDown();
        }, FluxSink.OverflowStrategy.IGNORE).subscribeOn(Schedulers.single());

        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final CountDownLatch onNextLatch = new CountDownLatch(1);
        final CompletableFuture<StringBuilder> future =
                fluxWithSingle.subscribeSingleFuture(s -> onNextLatch.countDown());
        assertTimeout(Duration.ofMillis(100), () -> assertEquals("single", future.get().toString()));
        assertTrue(onNextLatch.await(100, TimeUnit.MILLISECONDS));
        future.cancel(false);
        cancelLatch.countDown();
        assertTrue(completeLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void subscribeSingleFuture_cancel_before_onSubscribe() throws InterruptedException {
        final CountDownLatch cancelLatch = new CountDownLatch(1);
        final CountDownLatch completeLatch = new CountDownLatch(1);
        final Flux<? extends CharSequence> flux = new Flux<CharSequence>() {
            @Override
            public void subscribe(final @NotNull CoreSubscriber<? super CharSequence> s) {
                final Thread thread = new Thread(() -> {
                    try {
                        assertTrue(cancelLatch.await(100, TimeUnit.MILLISECONDS));
                    } catch (final InterruptedException e) {
                        fail(e);
                    }
                    final TestSubscription subscription = new TestSubscription();
                    s.onSubscribe(subscription);
                    assertTrue(subscription.cancelled);
                    completeLatch.countDown();
                });
                thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
                thread.start();
            }
        };

        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final CompletableFuture<StringBuilder> future = fluxWithSingle.subscribeSingleFuture();
        future.cancel(false);
        cancelLatch.countDown();
        assertTrue(completeLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void subscribeSingleFuture_cancel_subscription() throws InterruptedException {
        final CountDownLatch cancelLatch = new CountDownLatch(1);
        final Flux<? extends CharSequence> flux = new Flux<CharSequence>() {
            @Override
            public void subscribe(final @NotNull CoreSubscriber<? super CharSequence> s) {
                final Thread thread = new Thread(() -> {
                    final TestSubscription subscription = new TestSubscription();
                    s.onSubscribe(subscription);
                    assertTrue(subscription.cancelled);
                    cancelLatch.countDown();
                });
                thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
                thread.start();
            }
        };

        final FluxWithSingle<String, StringBuilder> fluxWithSingle =
                new FluxWithSingleSplit<>(flux, String.class, StringBuilder.class);

        final CompletableFuture<StringBuilder> future = fluxWithSingle.subscribeSingleFuture(new Subscriber<String>() {
            @Override
            public void onSubscribe(final @NotNull Subscription s) {
                s.cancel();
            }

            @Override
            public void onNext(final @NotNull String s) {}

            @Override
            public void onComplete() {}

            @Override
            public void onError(final @NotNull Throwable t) {}
        });
        assertTrue(cancelLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
        assertTrue(future.isCancelled());
        assertTrue(future.isCompletedExceptionally());
        assertThrows(CancellationException.class, future::get);
    }

    private @NotNull CompletableFuture<StringBuilder> subscribeSingleFuture(
            final int args,
            final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle,
            final @NotNull AtomicInteger onNextCounter,
            final @NotNull AtomicInteger onErrorCounter,
            final @NotNull AtomicInteger onCompleteCounter,
            final @NotNull CountDownLatch latch) {

        switch (args) {
            case 0:
                return fluxWithSingle.subscribeSingleFuture();
            case 1:
                return fluxWithSingle.subscribeSingleFuture(s -> {
                    assertEquals("next" + onNextCounter.get(), s);
                    onNextCounter.incrementAndGet();
                    latch.countDown();
                });
            case 2:
                return fluxWithSingle.subscribeSingleFuture(s -> {
                    assertEquals("next" + onNextCounter.get(), s);
                    onNextCounter.incrementAndGet();
                    latch.countDown();
                }, throwable -> {
                    onErrorCounter.incrementAndGet();
                    latch.countDown();
                });
            case 3:
                return fluxWithSingle.subscribeSingleFuture(s -> {
                    assertEquals("next" + onNextCounter.get(), s);
                    onNextCounter.incrementAndGet();
                    latch.countDown();
                }, throwable -> {
                    onErrorCounter.incrementAndGet();
                    latch.countDown();
                }, () -> {
                    onCompleteCounter.incrementAndGet();
                    latch.countDown();
                });
            default:
                return fluxWithSingle.subscribeSingleFuture(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(final @NotNull Subscription s) {
                        s.request(10);
                    }

                    @Override
                    public void onNext(final @NotNull String s) {
                        assertEquals("next" + onNextCounter.get(), s);
                        onNextCounter.incrementAndGet();
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        onCompleteCounter.incrementAndGet();
                        latch.countDown();
                    }

                    @Override
                    public void onError(final @NotNull Throwable t) {
                        onErrorCounter.incrementAndGet();
                        latch.countDown();
                    }
                });
        }
    }

    private @NotNull CompletableFuture<StringBuilder> subscribeSingleFuture(
            final int args,
            final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle,
            final @NotNull AtomicInteger onNextCounter,
            final @NotNull AtomicInteger onErrorCounter,
            final @NotNull AtomicInteger onCompleteCounter) {

        return subscribeSingleFuture(
                args, fluxWithSingle, onNextCounter, onErrorCounter, onCompleteCounter, new CountDownLatch(0));
    }

    @MethodSource("singleNext3")
    @ParameterizedTest
    void conditional_fusion(final @NotNull FluxWithSingle<String, StringBuilder> fluxWithSingle) {
        final AtomicInteger count = new AtomicInteger(1);
        fluxWithSingle.doOnSingle(stringBuilder -> assertEquals("single", stringBuilder.toString()))
                .mapBoth(s -> s, stringBuilder -> stringBuilder)
                .doOnSingle(stringBuilder -> {})
                .mapBoth(s -> s, stringBuilder -> stringBuilder)
                .filter(string -> !string.equals("next0"))
                .doOnNext(string -> assertEquals("next" + count.getAndIncrement(), string))
                .blockLast();
        assertEquals(3, count.get());
    }

    private static class TestSubscription implements Subscription {

        volatile boolean cancelled;
        final @NotNull AtomicLong requested = new AtomicLong();

        @Override
        public void request(final long n) {
            requested.addAndGet(n);
        }

        @Override
        public void cancel() {
            cancelled = true;
        }
    }
}