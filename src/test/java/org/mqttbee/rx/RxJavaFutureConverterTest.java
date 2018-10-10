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

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class RxJavaFutureConverterTest {

    @Test
    void toFuture_completable_immediate() {
        final CompletableFuture<Void> future = RxJavaFutureConverter.toFuture(Completable.complete());
        assertTrue(future.isDone());
    }

    @Test
    void toFuture_completable_immediate_error() {
        final CompletableFuture<Void> future = RxJavaFutureConverter.toFuture(Completable.error(new Exception("test")));
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
            emitLatch.await(100, TimeUnit.MILLISECONDS);
            emitter.onComplete();
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Void> future = RxJavaFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
    }

    @Test
    void toFuture_completable_error() throws InterruptedException {
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Completable completable = Completable.create(emitter -> {
            emitLatch.await(100, TimeUnit.MILLISECONDS);
            emitter.onError(new Exception("test"));
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Void> future = RxJavaFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(100, TimeUnit.MILLISECONDS));
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
                emitLatch.await(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
            if (emitter.isDisposed()) {
                completedLatch.countDown();
            }
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Void> future = RxJavaFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        assertTrue(subscribeLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.cancel(false));

        emitLatch.countDown();
        assertTrue(completedLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
        assertThrows(CancellationException.class, future::get);
    }

    @Test
    void toFuture_maybe_immediate() throws ExecutionException, InterruptedException {
        final CompletableFuture<Optional<String>> future = RxJavaFutureConverter.toFuture(Maybe.just("maybe"));
        assertTrue(future.isDone());
        final Optional<String> optional = future.get();
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertEquals("maybe", optional.get());
    }

    @Test
    void toFuture_maybe_immediate_empty() throws ExecutionException, InterruptedException {
        final CompletableFuture<Optional<String>> future = RxJavaFutureConverter.toFuture(Maybe.empty());
        assertTrue(future.isDone());
        final Optional<String> optional = future.get();
        assertNotNull(optional);
        assertFalse(optional.isPresent());
    }

    @Test
    void toFuture_maybe_immediate_error() {
        final CompletableFuture<Optional<String>> future =
                RxJavaFutureConverter.toFuture(Maybe.error(new Exception("test")));
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
            emitLatch.await(100, TimeUnit.MILLISECONDS);
            emitter.onSuccess("maybe");
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Optional<String>> future = RxJavaFutureConverter.toFuture(maybe);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(100, TimeUnit.MILLISECONDS));
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
            emitLatch.await(100, TimeUnit.MILLISECONDS);
            emitter.onComplete();
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Optional<String>> future = RxJavaFutureConverter.toFuture(maybe);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(100, TimeUnit.MILLISECONDS));
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
                emitLatch.await(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
            if (emitter.isDisposed()) {
                completedLatch.countDown();
            }
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Optional<String>> future = RxJavaFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        assertTrue(subscribeLatch.await(100, TimeUnit.MILLISECONDS));
        future.cancel(false);

        emitLatch.countDown();
        assertTrue(completedLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
        assertThrows(CancellationException.class, future::get);
    }

    @Test
    void toFuture_maybe_error() throws InterruptedException {
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Maybe<String> maybe = Maybe.<String>create(emitter -> {
            emitLatch.await(100, TimeUnit.MILLISECONDS);
            emitter.onError(new Exception("test"));
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<Optional<String>> future = RxJavaFutureConverter.toFuture(maybe);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
        assertTrue(future.isCompletedExceptionally());
        final ExecutionException exception = assertThrows(ExecutionException.class, future::get);
        assertEquals("test", exception.getCause().getMessage());
    }

    @Test
    void toFuture_single_immediate() throws ExecutionException, InterruptedException {
        final CompletableFuture<String> future = RxJavaFutureConverter.toFuture(Single.just("single"));
        assertTrue(future.isDone());
        assertEquals("single", future.get());
    }

    @Test
    void toFuture_single_immediate_error() {
        final CompletableFuture<String> future = RxJavaFutureConverter.toFuture(Single.error(new Exception("test")));
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
            emitLatch.await(100, TimeUnit.MILLISECONDS);
            emitter.onSuccess("single");
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<String> future = RxJavaFutureConverter.toFuture(single);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
        assertEquals("single", future.get());
    }

    @Test
    void toFuture_single_error() throws InterruptedException {
        final CountDownLatch emitLatch = new CountDownLatch(1);
        final CountDownLatch completedLatch = new CountDownLatch(1);
        final Single<String> single = Single.<String>create(emitter -> {
            emitLatch.await(100, TimeUnit.MILLISECONDS);
            emitter.onError(new Exception("test"));
            completedLatch.countDown();
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<String> future = RxJavaFutureConverter.toFuture(single);
        assertFalse(future.isDone());

        emitLatch.countDown();
        assertTrue(completedLatch.await(100, TimeUnit.MILLISECONDS));
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
                emitLatch.await(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
            if (emitter.isDisposed()) {
                completedLatch.countDown();
            }
        }).subscribeOn(Schedulers.single());

        final CompletableFuture<String> future = RxJavaFutureConverter.toFuture(completable);
        assertFalse(future.isDone());

        assertTrue(subscribeLatch.await(100, TimeUnit.MILLISECONDS));
        future.cancel(false);

        emitLatch.countDown();
        assertTrue(completedLatch.await(100, TimeUnit.MILLISECONDS));
        assertTrue(future.isDone());
        assertThrows(CancellationException.class, future::get);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toCompletable_immediate() {
        final Completable completable = RxJavaFutureConverter.toCompletable(CompletableFuture.completedFuture("test"));

        final AtomicInteger counter = new AtomicInteger();
        completable.subscribe(counter::incrementAndGet);
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toCompletable_immediate_error() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new Exception("test"));
        final Completable completable = RxJavaFutureConverter.toCompletable(future);

        final AtomicInteger counter = new AtomicInteger();
        completable.subscribe(() -> {
        }, throwable -> {
            assertEquals("test", throwable.getMessage());
            counter.incrementAndGet();
        });
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toCompletable() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Completable completable = RxJavaFutureConverter.toCompletable(future);

        final AtomicInteger counter = new AtomicInteger();
        completable.subscribe(counter::incrementAndGet);
        assertEquals(0, counter.get());

        future.complete("test");
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toCompletable_error() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Completable completable = RxJavaFutureConverter.toCompletable(future);

        final AtomicInteger counter = new AtomicInteger();
        completable.subscribe(() -> {
        }, throwable -> {
            assertEquals("test", throwable.getMessage());
            counter.incrementAndGet();
        });
        assertEquals(0, counter.get());

        future.completeExceptionally(new Exception("test"));
        assertEquals(1, counter.get());
    }

    @Test
    void toCompletable_dispose() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Completable completable = RxJavaFutureConverter.toCompletable(future);

        final AtomicInteger counter = new AtomicInteger();
        final Disposable disposable = completable.subscribe(counter::incrementAndGet);
        assertEquals(0, counter.get());

        disposable.dispose();
        assertTrue(future.isCancelled());
        future.complete("test");
        assertEquals(0, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toMaybe_immediate() {
        final Maybe<String> maybe =
                RxJavaFutureConverter.toMaybe(CompletableFuture.completedFuture(Optional.of("test")));

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(s -> {
            assertEquals("test", s);
            counter.incrementAndGet();
        });
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toMaybe_immediate_empty() {
        final Maybe<String> maybe = RxJavaFutureConverter.toMaybe(CompletableFuture.completedFuture(Optional.empty()));

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(s -> {
        }, throwable -> {
        }, counter::incrementAndGet);
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toMaybe_immediate_null() {
        final Maybe<String> maybe = RxJavaFutureConverter.toMaybe(CompletableFuture.completedFuture(null));

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(s -> {
        }, throwable -> {
            assertTrue(throwable instanceof NullPointerException);
            counter.incrementAndGet();
        });
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toMaybe_immediate_error() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        future.completeExceptionally(new Exception("test"));
        final Maybe<String> maybe = RxJavaFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(s -> {
        }, throwable -> {
            assertEquals("test", throwable.getMessage());
            counter.incrementAndGet();
        });
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toMaybe() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final Maybe<String> maybe = RxJavaFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(s -> {
            assertEquals("test", s);
            counter.incrementAndGet();
        });
        assertEquals(0, counter.get());

        future.complete(Optional.of("test"));
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toMaybe_empty() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final Maybe<String> maybe = RxJavaFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(s -> {
        }, throwable -> {
        }, counter::incrementAndGet);
        assertEquals(0, counter.get());

        future.complete(Optional.empty());
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toMaybe_null() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final Maybe<String> maybe = RxJavaFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(s -> {
        }, throwable -> {
            assertTrue(throwable instanceof NullPointerException);
            counter.incrementAndGet();
        });
        assertEquals(0, counter.get());

        future.complete(null);
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toMaybe_error() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final Maybe<String> maybe = RxJavaFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        maybe.subscribe(s -> {
        }, throwable -> {
            assertEquals("test", throwable.getMessage());
            counter.incrementAndGet();
        });
        assertEquals(0, counter.get());

        future.completeExceptionally(new Exception("test"));
        assertEquals(1, counter.get());
    }

    @Test
    void toMaybe_dispose() {
        final CompletableFuture<Optional<String>> future = new CompletableFuture<>();
        final Maybe<String> completable = RxJavaFutureConverter.toMaybe(future);

        final AtomicInteger counter = new AtomicInteger();
        final Disposable disposable = completable.subscribe(s -> counter.incrementAndGet());
        assertEquals(0, counter.get());

        disposable.dispose();
        assertTrue(future.isCancelled());
        future.complete(Optional.of("test"));
        assertEquals(0, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toSingle_immediate() {
        final Single<String> single = RxJavaFutureConverter.toSingle(CompletableFuture.completedFuture("test"));

        final AtomicInteger counter = new AtomicInteger();
        single.subscribe(s -> {
            assertEquals("test", s);
            counter.incrementAndGet();
        });
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toSingle_immediate_error() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new Exception("test"));
        final Single<String> single = RxJavaFutureConverter.toSingle(future);

        final AtomicInteger counter = new AtomicInteger();
        single.subscribe(s -> {
        }, throwable -> {
            assertEquals("test", throwable.getMessage());
            counter.incrementAndGet();
        });
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toSingle() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Single<String> single = RxJavaFutureConverter.toSingle(future);

        final AtomicInteger counter = new AtomicInteger();
        single.subscribe(s -> {
            assertEquals("test", s);
            counter.incrementAndGet();
        });
        assertEquals(0, counter.get());

        future.complete("test");
        assertEquals(1, counter.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void toSingle_error() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Single<String> single = RxJavaFutureConverter.toSingle(future);

        final AtomicInteger counter = new AtomicInteger();
        single.subscribe(s -> {
        }, throwable -> {
            assertEquals("test", throwable.getMessage());
            counter.incrementAndGet();
        });
        assertEquals(0, counter.get());

        future.completeExceptionally(new Exception("test"));
        assertEquals(1, counter.get());
    }

    @Test
    void toSingle_dispose() {
        final CompletableFuture<String> future = new CompletableFuture<>();
        final Single<String> single = RxJavaFutureConverter.toSingle(future);

        final AtomicInteger counter = new AtomicInteger();
        final Disposable disposable = single.subscribe(s -> counter.incrementAndGet());
        assertEquals(0, counter.get());

        disposable.dispose();
        assertTrue(future.isCancelled());
        future.complete("test");
        assertEquals(0, counter.get());
    }

}