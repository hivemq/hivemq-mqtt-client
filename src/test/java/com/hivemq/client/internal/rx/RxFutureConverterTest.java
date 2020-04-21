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

package com.hivemq.client.internal.rx;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class RxFutureConverterTest {

    @Test
    void toFuture_completable_immediate() {
        final CompletableFuture<Void> future = RxFutureConverter.toFuture(Completable.complete());
        assertTrue(future.isDone());
    }

    @Test
    void toFuture_completable_immediate_error() {
        final CompletableFuture<Void> future = RxFutureConverter.toFuture(Completable.error(new Exception("test")));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        final ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals("test", exception.getCause().getMessage());
    }

    @Test
    void toFuture_completable() throws InterruptedException {
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Completable completable = Completable.create(emitter -> {
            emitLatch.await(1, TimeUnit.SECONDS);
            emitter.onComplete();
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Void> future = RxFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
    }

    @Test
    void toFuture_completable_error() throws InterruptedException {
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Completable completable = Completable.create(emitter -> {
            emitLatch.await(1, TimeUnit.SECONDS);
            emitter.onError(new Exception("test"));
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Void> future = RxFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        final ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals("test", exception.getCause().getMessage());
    }

    @Test
    void toFuture_completable_cancel() throws InterruptedException {
        final CountDownLatch subscribeLatch = new CountDownLatch(1);
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Completable completable = Completable.create(emitter -> {
            subscribeLatch.countDown();
            try {
                emitLatch.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
            if (emitter.isDisposed()) {
                completedLatch.countDown();
            }
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Void> future = RxFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        assertTrue(subscribeLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.cancel(false));

        emitLatch.countDown();
        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertThrows(CancellationException.class, future::get);
    }

    @Test
    void toFuture_completable_cancel_before_onSubscribe() throws InterruptedException {
        final CountDownLatch subscribeLatch = new CountDownLatch(1);
        final CountDownLatch cancelLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Completable completable = new Completable() {
            @Override
            protected void subscribeActual(final @NotNull CompletableObserver observer) {
                subscribeLatch.countDown();
                final Thread thread = new Thread(() -> {
                    try {
                        assertTrue(cancelLatch.await(1, TimeUnit.SECONDS));
                    } catch (final InterruptedException e) {
                        fail(e);
                    }
                    final Disposable disposable = new TestDisposable();
                    observer.onSubscribe(disposable);
                    assertTrue(disposable.isDisposed());
                    completedLatch.countDown();
                });
                thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
                thread.start();
            }
        };

        final CompletableFuture<Void> future = RxFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        assertTrue(subscribeLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.cancel(false));
        cancelLatch.countDown();

        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertThrows(CancellationException.class, future::get);
    }

    @Test
    void toFuture_maybe_immediate() throws ExecutionException, InterruptedException {
        final CompletableFuture<Optional<String>> future = RxFutureConverter.toFuture(Maybe.just("maybe"));
        assertTrue(future.isDone());
        final Optional<String> optional = future.get();
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertEquals("maybe", optional.get());
    }

    @Test
    void toFuture_maybe_immediate_empty() throws ExecutionException, InterruptedException {
        final CompletableFuture<Optional<String>> future = RxFutureConverter.toFuture(Maybe.empty());
        assertTrue(future.isDone());
        final Optional<String> optional = future.get();
        assertNotNull(optional);
        assertFalse(optional.isPresent());
    }

    @Test
    void toFuture_maybe_immediate_error() {
        final CompletableFuture<Optional<String>> future =
                RxFutureConverter.toFuture(Maybe.error(new Exception("test")));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        final ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals("test", exception.getCause().getMessage());
    }

    @Test
    void toFuture_maybe() throws InterruptedException, ExecutionException {
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Maybe<String> maybe = Maybe.<String>create(emitter -> {
            emitLatch.await(1, TimeUnit.SECONDS);
            emitter.onSuccess("maybe");
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Optional<String>> future = RxFutureConverter.toFuture(maybe);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        final Optional<String> optional = future.get();
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertEquals("maybe", optional.get());
    }

    @Test
    void toFuture_maybe_empty() throws InterruptedException, ExecutionException {
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Maybe<String> maybe = Maybe.<String>create(emitter -> {
            emitLatch.await(1, TimeUnit.SECONDS);
            emitter.onComplete();
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Optional<String>> future = RxFutureConverter.toFuture(maybe);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        final Optional<String> optional = future.get();
        assertNotNull(optional);
        assertFalse(optional.isPresent());
    }

    @Test
    void toFuture_maybe_cancel() throws InterruptedException {
        final CountDownLatch subscribeLatch = new CountDownLatch(1);
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Maybe<String> completable = Maybe.<String>create(emitter -> {
            subscribeLatch.countDown();
            try {
                emitLatch.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
            if (emitter.isDisposed()) {
                completedLatch.countDown();
            }
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Optional<String>> future = RxFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        assertTrue(subscribeLatch.await(1, TimeUnit.SECONDS));
        future.cancel(false);

        emitLatch.countDown();
        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertThrows(CancellationException.class, future::get);
    }

    @Test
    void toFuture_maybe_cancel_before_onSubscribe() throws InterruptedException {
        final CountDownLatch subscribeLatch = new CountDownLatch(1);
        final CountDownLatch cancelLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Maybe<String> maybe = new Maybe<String>() {
            @Override
            protected void subscribeActual(final @NotNull MaybeObserver<? super String> observer) {
                subscribeLatch.countDown();
                final Thread thread = new Thread(() -> {
                    try {
                        assertTrue(cancelLatch.await(1, TimeUnit.SECONDS));
                    } catch (final InterruptedException e) {
                        fail(e);
                    }
                    final Disposable disposable = new TestDisposable();
                    observer.onSubscribe(disposable);
                    assertTrue(disposable.isDisposed());
                    completedLatch.countDown();
                });
                thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
                thread.start();
            }
        };

        final CompletableFuture<Optional<String>> future = RxFutureConverter.toFuture(maybe);
        assertFalse(future.isDone());

        assertTrue(subscribeLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.cancel(false));
        cancelLatch.countDown();

        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertThrows(CancellationException.class, future::get);
    }

    @Test
    void toFuture_maybe_error() throws InterruptedException {
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Maybe<String> maybe = Maybe.<String>create(emitter -> {
            emitLatch.await(1, TimeUnit.SECONDS);
            emitter.onError(new Exception("test"));
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Optional<String>> future = RxFutureConverter.toFuture(maybe);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        final ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals("test", exception.getCause().getMessage());
    }

    @Test
    void toFuture_single_immediate() throws ExecutionException, InterruptedException {
        final CompletableFuture<String> future = RxFutureConverter.toFuture(Single.just("single"));
        assertTrue(future.isDone());
        assertEquals("single", future.get());
    }

    @Test
    void toFuture_single_immediate_error() {
        final CompletableFuture<String> future = RxFutureConverter.toFuture(Single.error(new Exception("test")));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        final ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals("test", exception.getCause().getMessage());
    }

    @Test
    void toFuture_single() throws InterruptedException, ExecutionException {
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Single<String> single = Single.<String>create(emitter -> {
            emitLatch.await(1, TimeUnit.SECONDS);
            emitter.onSuccess("single");
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<String> future = RxFutureConverter.toFuture(single);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertEquals("single", future.get());
    }

    @Test
    void toFuture_single_error() throws InterruptedException {
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Single<String> single = Single.<String>create(emitter -> {
            emitLatch.await(1, TimeUnit.SECONDS);
            emitter.onError(new Exception("test"));
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<String> future = RxFutureConverter.toFuture(single);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        final ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals("test", exception.getCause().getMessage());
    }

    @Test
    void toFuture_single_cancel() throws InterruptedException {
        final CountDownLatch subscribeLatch = new CountDownLatch(1);
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Single<String> completable = Single.<String>create(emitter -> {
            subscribeLatch.countDown();
            try {
                emitLatch.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
            if (emitter.isDisposed()) {
                completedLatch.countDown();
            }
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<String> future = RxFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        assertTrue(subscribeLatch.await(1, TimeUnit.SECONDS));
        future.cancel(false);

        emitLatch.countDown();
        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertThrows(CancellationException.class, future::get);
    }

    @Test
    void toFuture_single_cancel_before_onSubscribe() throws InterruptedException {
        final CountDownLatch subscribeLatch = new CountDownLatch(1);
        final CountDownLatch cancelLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Single<String> single = new Single<String>() {
            @Override
            protected void subscribeActual(final @NotNull SingleObserver<? super String> observer) {
                subscribeLatch.countDown();
                final Thread thread = new Thread(() -> {
                    try {
                        assertTrue(cancelLatch.await(1, TimeUnit.SECONDS));
                    } catch (final InterruptedException e) {
                        fail(e);
                    }
                    final Disposable disposable = new TestDisposable();
                    observer.onSubscribe(disposable);
                    assertTrue(disposable.isDisposed());
                    completedLatch.countDown();
                });
                thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
                thread.start();
            }
        };

        final CompletableFuture<String> future = RxFutureConverter.toFuture(single);
        assertFalse(future.isDone());

        assertTrue(subscribeLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.cancel(false));
        cancelLatch.countDown();

        assertTrue(completedLatch.await(1, TimeUnit.SECONDS));
        assertTrue(future.isDone());
        assertThrows(CancellationException.class, future::get);
    }

    @Test
    void toCompletable_immediate() {
        final Completable completable = RxFutureConverter.toCompletable(CompletableFuture.completedFuture("test"));

        final AtomicInteger counter = new AtomicInteger();
        completable.subscribe(new TestCompletableObserver() {
            @Override
            public void onComplete() {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                counter.incrementAndGet();
            }
        });
        assertEquals(1, counter.get());
    }

    @Test
    void toCompletable_immediate_error() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new Exception("test"));
        final Completable completable = RxFutureConverter.toCompletable(future);

        final AtomicInteger counter = new AtomicInteger();
        completable.subscribe(new TestCompletableObserver() {
            @Override
            public void onError(final @NotNull Throwable e) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertEquals("test", e.getMessage());
                counter.incrementAndGet();
            }
        });
        assertEquals(1, counter.get());
    }

    @Test
    void toCompletable() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Completable completable = RxFutureConverter.toCompletable(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestCompletableObserver observer = new TestCompletableObserver() {
            @Override
            public void onComplete() {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                counter.incrementAndGet();
            }
        };
        completable.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        future.complete("test");
        assertEquals(1, counter.get());
    }

    @Test
    void toCompletable_error() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Completable completable = RxFutureConverter.toCompletable(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestCompletableObserver observer = new TestCompletableObserver() {
            @Override
            public void onError(final @NotNull Throwable e) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertEquals("test", e.getMessage());
                counter.incrementAndGet();
            }
        };
        completable.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        future.completeExceptionally(new Exception("test"));
        assertEquals(1, counter.get());
    }

    @Test
    void toCompletable_dispose() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Completable completable = RxFutureConverter.toCompletable(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestCompletableObserver observer = new TestCompletableObserver() {
            @Override
            public void onComplete() {
                counter.incrementAndGet();
            }
        };
        completable.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        observer.disposable.dispose();
        assertTrue(observer.disposable.isDisposed());
        assertTrue(future.isCancelled());
        future.complete("test");
        assertEquals(0, counter.get());
    }

    @Test
    void toMaybe_immediate() {
        final Maybe<String> maybe = RxFutureConverter.toMaybe(CompletableFuture.completedFuture(Optional.of("test")));

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(new TestMaybeObserver<String>() {
            @Override
            public void onSuccess(final @NotNull String s) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertEquals("test", s);
                counter.incrementAndGet();
            }
        });
        assertEquals(1, counter.get());
    }

    @Test
    void toMaybe_immediate_empty() {
        final Maybe<String> maybe = RxFutureConverter.toMaybe(CompletableFuture.completedFuture(Optional.empty()));

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(new TestMaybeObserver<String>() {
            @Override
            public void onComplete() {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                counter.incrementAndGet();
            }
        });
        assertEquals(1, counter.get());
    }

    @Test
    void toMaybe_immediate_null() {
        final Maybe<String> maybe = RxFutureConverter.toMaybe(CompletableFuture.completedFuture(null));

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(new TestMaybeObserver<String>() {
            @Override
            public void onError(final @NotNull Throwable e) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertTrue(e instanceof NullPointerException);
                counter.incrementAndGet();
            }
        });
        assertEquals(1, counter.get());
    }

    @Test
    void toMaybe_immediate_error() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        future.completeExceptionally(new Exception("test"));
        final Maybe<String> maybe = RxFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(new TestMaybeObserver<String>() {
            @Override
            public void onError(final @NotNull Throwable e) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertEquals("test", e.getMessage());
                counter.incrementAndGet();
            }
        });
        assertEquals(1, counter.get());
    }

    @Test
    void toMaybe() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final Maybe<String> maybe = RxFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestMaybeObserver<String> observer = new TestMaybeObserver<String>() {
            @Override
            public void onSuccess(final @NotNull String s) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertEquals("test", s);
                counter.incrementAndGet();
            }
        };
        maybe.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        future.complete(Optional.of("test"));
        assertEquals(1, counter.get());
    }

    @Test
    void toMaybe_empty() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final Maybe<String> maybe = RxFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestMaybeObserver<String> observer = new TestMaybeObserver<String>() {
            @Override
            public void onComplete() {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                counter.incrementAndGet();
            }
        };
        maybe.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        future.complete(Optional.empty());
        assertEquals(1, counter.get());
    }

    @Test
    void toMaybe_null() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final Maybe<String> maybe = RxFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestMaybeObserver<String> observer = new TestMaybeObserver<String>() {
            @Override
            public void onError(final @NotNull Throwable e) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertTrue(e instanceof NullPointerException);
                counter.incrementAndGet();
            }
        };
        maybe.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        future.complete(null);
        assertEquals(1, counter.get());
    }

    @Test
    void toMaybe_error() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final Maybe<String> maybe = RxFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestMaybeObserver<String> observer = new TestMaybeObserver<String>() {
            @Override
            public void onError(final @NotNull Throwable e) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertEquals("test", e.getMessage());
                counter.incrementAndGet();
            }
        };
        maybe.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        future.completeExceptionally(new Exception("test"));
        assertEquals(1, counter.get());
    }

    @Test
    void toMaybe_dispose() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final Maybe<String> completable = RxFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestMaybeObserver<String> observer = new TestMaybeObserver<String>() {
            @Override
            public void onSuccess(final @NotNull String s) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                counter.incrementAndGet();
            }
        };
        completable.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        observer.disposable.dispose();
        assertTrue(future.isCancelled());
        future.complete(Optional.of("test"));
        assertEquals(0, counter.get());
    }

    @Test
    void toSingle_immediate() {
        final Single<String> single = RxFutureConverter.toSingle(CompletableFuture.completedFuture("test"));

        final AtomicInteger counter = new AtomicInteger();
        single.subscribe(new TestSingleObserver<String>() {
            @Override
            public void onSuccess(final @NotNull String s) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertEquals("test", s);
                counter.incrementAndGet();
            }
        });
        assertEquals(1, counter.get());
    }

    @Test
    void toSingle_immediate_error() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new Exception("test"));
        final Single<String> single = RxFutureConverter.toSingle(future);

        final AtomicInteger counter = new AtomicInteger();
        single.subscribe(new TestSingleObserver<String>() {
            @Override
            public void onError(final @NotNull Throwable e) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertEquals("test", e.getMessage());
                counter.incrementAndGet();
            }
        });
        assertEquals(1, counter.get());
    }

    @Test
    void toSingle() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Single<String> single = RxFutureConverter.toSingle(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestSingleObserver<String> observer = new TestSingleObserver<String>() {
            @Override
            public void onSuccess(final @NotNull String s) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertEquals("test", s);
                counter.incrementAndGet();
            }
        };
        single.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        future.complete("test");
        assertEquals(1, counter.get());
    }

    @Test
    void toSingle_error() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Single<String> single = RxFutureConverter.toSingle(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestSingleObserver<String> observer = new TestSingleObserver<String>() {
            @Override
            public void onError(final @NotNull Throwable e) {
                assertNotNull(disposable);
                assertTrue(disposable.isDisposed());
                assertEquals("test", e.getMessage());
                counter.incrementAndGet();
            }
        };
        single.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        future.completeExceptionally(new Exception("test"));
        assertEquals(1, counter.get());
    }

    @Test
    void toSingle_dispose() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Single<String> single = RxFutureConverter.toSingle(future);

        final AtomicInteger counter = new AtomicInteger();
        final TestSingleObserver<String> observer = new TestSingleObserver<String>() {
            @Override
            public void onSuccess(final @NotNull String s) {
                counter.incrementAndGet();
            }
        };
        single.subscribe(observer);
        assertNotNull(observer.disposable);
        assertFalse(observer.disposable.isDisposed());
        assertEquals(0, counter.get());

        observer.disposable.dispose();
        assertTrue(future.isCancelled());
        future.complete("test");
        assertEquals(0, counter.get());
    }

    private static class TestDisposable implements Disposable {

        private volatile boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }

    private static abstract class TestCompletableObserver implements CompletableObserver {

        @Nullable Disposable disposable;

        @Override
        public void onSubscribe(final @NotNull Disposable d) {
            disposable = d;
        }

        @Override
        public void onComplete() {}

        @Override
        public void onError(final @NotNull Throwable e) {}
    }

    private static abstract class TestMaybeObserver<T> implements MaybeObserver<T> {

        @Nullable Disposable disposable;

        @Override
        public void onSubscribe(final @NotNull Disposable d) {
            disposable = d;
        }

        @Override
        public void onSuccess(final @NotNull T t) {}

        @Override
        public void onComplete() {}

        @Override
        public void onError(final @NotNull Throwable e) {}
    }

    private static abstract class TestSingleObserver<T> implements SingleObserver<T> {

        @Nullable Disposable disposable;

        @Override
        public void onSubscribe(final @NotNull Disposable d) {
            disposable = d;
        }

        @Override
        public void onSuccess(final @NotNull T t) {}

        @Override
        public void onError(final @NotNull Throwable e) {}
    }
}