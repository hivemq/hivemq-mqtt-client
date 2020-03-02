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

package com.hivemq.client.internal.rx;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * @author Silvio Giebl
 */
public final class RxFutureConverter {

    public static @NotNull CompletableFuture<Void> toFuture(final @NotNull Completable completable) {
        return new RxCompletableFuture(completable);
    }

    public static <T> @NotNull CompletableFuture<@NotNull Optional<T>> toFuture(final @NotNull Maybe<T> maybe) {
        return new RxMaybeFuture<>(maybe);
    }

    public static <T> @NotNull CompletableFuture<@NotNull T> toFuture(final @NotNull Single<T> single) {
        return new RxSingleFuture<>(single);
    }

    private static abstract class RxFuture<T> extends CompletableFuture<T> {

        volatile @Nullable Disposable disposable;
        volatile boolean cancelled;

        public void onSubscribe(final @NotNull Disposable d) {
            disposable = d;
            if (cancelled) {
                d.dispose();
            }
        }

        public void onError(final @NotNull Throwable e) {
            if (!cancelled) {
                completeExceptionally(e);
            }
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            cancelled = true;
            final Disposable disposable = this.disposable;
            if (disposable != null) {
                disposable.dispose();
            }
            return super.cancel(mayInterruptIfRunning);
        }
    }

    private static class RxCompletableFuture extends RxFuture<Void> implements CompletableObserver {

        RxCompletableFuture(final @NotNull Completable completable) {
            completable.subscribe(this);
        }

        @Override
        public void onComplete() {
            if (!cancelled) {
                complete(null);
            }
        }
    }

    private static class RxMaybeFuture<T> extends RxFuture<Optional<T>> implements MaybeObserver<T> {

        RxMaybeFuture(final @NotNull Maybe<T> maybe) {
            maybe.subscribe(this);
        }

        @Override
        public void onSuccess(final @NotNull T t) {
            if (!cancelled) {
                complete(Optional.of(t));
            }
        }

        @Override
        public void onComplete() {
            if (!cancelled) {
                complete(Optional.empty());
            }
        }
    }

    private static class RxSingleFuture<T> extends RxFuture<T> implements SingleObserver<T> {

        RxSingleFuture(final @NotNull Single<T> single) {
            single.subscribe(this);
        }

        @Override
        public void onSuccess(final @NotNull T t) {
            if (!cancelled) {
                complete(t);
            }
        }
    }

    public static @NotNull Completable toCompletable(final @NotNull CompletableFuture<?> future) {
        return new FutureCompletable(future);
    }

    public static <T> @NotNull Maybe<T> toMaybe(final @NotNull CompletableFuture<@NotNull Optional<T>> future) {
        return new FutureMaybe<>(future);
    }

    public static <T> @NotNull Single<T> toSingle(final @NotNull CompletableFuture<@NotNull T> future) {
        return new FutureSingle<>(future);
    }

    private static final int INITIAL = 0;
    private static final int SUBSCRIBED_OR_COMPLETE = 1;
    private static final int SUBSCRIBED_AND_COMPLETE_OR_CANCELLED = 2;

    private static boolean checkComplete(final @NotNull AtomicInteger done) {
        return !done.compareAndSet(INITIAL, SUBSCRIBED_OR_COMPLETE) &&
                done.compareAndSet(SUBSCRIBED_OR_COMPLETE, SUBSCRIBED_AND_COMPLETE_OR_CANCELLED);
    }

    private static void dispose(final @NotNull AtomicInteger done, final @NotNull CompletableFuture<?> future) {
        done.set(SUBSCRIBED_AND_COMPLETE_OR_CANCELLED);
        future.cancel(false);
    }

    private static boolean isDisposed(final @NotNull AtomicInteger done) {
        return done.get() == SUBSCRIBED_AND_COMPLETE_OR_CANCELLED;
    }

    private static class FutureCompletable extends Completable implements Disposable, BiConsumer<Object, Throwable> {

        private final @NotNull CompletableFuture<?> future;
        private volatile @Nullable CompletableObserver observer;
        private volatile @Nullable Throwable throwable;
        private final @NotNull AtomicInteger done = new AtomicInteger(INITIAL);

        FutureCompletable(final @NotNull CompletableFuture<?> future) {
            this.future = future;
            future.whenComplete(this);
        }

        @Override
        protected void subscribeActual(final @NotNull CompletableObserver observer) {
            this.observer = observer;
            observer.onSubscribe(this);
            if (checkComplete(done)) {
                complete(observer, throwable);
            }
        }

        @Override
        public void dispose() {
            RxFutureConverter.dispose(done, future);
        }

        @Override
        public boolean isDisposed() {
            return RxFutureConverter.isDisposed(done);
        }

        @Override
        public void accept(final @Nullable Object o, final @Nullable Throwable throwable) {
            this.throwable = throwable;
            if (checkComplete(done)) {
                final CompletableObserver observer = this.observer;
                assert observer != null;
                complete(observer, throwable);
            }
        }

        private static void complete(final @NotNull CompletableObserver observer, final @Nullable Throwable throwable) {
            if (throwable == null) {
                observer.onComplete();
            } else {
                observer.onError(throwable);
            }
        }
    }

    private static class FutureMaybe<T> extends Maybe<T> implements Disposable, BiConsumer<Optional<T>, Throwable> {

        private final @NotNull CompletableFuture<Optional<T>> future;
        private volatile @Nullable MaybeObserver<? super T> observer;
        private volatile @Nullable T t;
        private volatile @Nullable Throwable throwable;
        private final @NotNull AtomicInteger done = new AtomicInteger(INITIAL);

        FutureMaybe(final @NotNull CompletableFuture<Optional<T>> future) {
            this.future = future;
            future.whenComplete(this);
        }

        @Override
        protected void subscribeActual(final @NotNull MaybeObserver<? super T> observer) {
            this.observer = observer;
            observer.onSubscribe(this);
            if (checkComplete(done)) {
                complete(observer, t, throwable);
            }
        }

        @Override
        public void dispose() {
            RxFutureConverter.dispose(done, future);
        }

        @Override
        public boolean isDisposed() {
            return RxFutureConverter.isDisposed(done);
        }

        @Override
        @SuppressWarnings("OptionalAssignedToNull")
        public void accept(final @Nullable Optional<T> t, final @Nullable Throwable throwable) {
            final T t1;
            final Throwable throwable1;
            if (throwable == null) {
                if (t == null) {
                    t1 = null;
                    throwable1 = new NullPointerException();
                } else {
                    t1 = t.orElse(null);
                    throwable1 = null;
                }
            } else {
                t1 = null;
                throwable1 = throwable;
            }
            this.t = t1;
            this.throwable = throwable1;
            if (checkComplete(done)) {
                final MaybeObserver<? super T> observer = this.observer;
                assert observer != null;
                complete(observer, t1, throwable1);
            }
        }

        private static <T> void complete(
                final @NotNull MaybeObserver<? super T> observer, final @Nullable T t,
                final @Nullable Throwable throwable) {

            if (throwable != null) {
                observer.onError(throwable);
            } else if (t != null) {
                observer.onSuccess(t);
            } else {
                observer.onComplete();
            }
        }
    }

    private static class FutureSingle<T> extends Single<T> implements Disposable, BiConsumer<T, Throwable> {

        private final @NotNull CompletableFuture<T> future;
        private volatile @Nullable SingleObserver<? super T> observer;
        private volatile @Nullable T t;
        private volatile @Nullable Throwable throwable;
        private final @NotNull AtomicInteger done = new AtomicInteger(INITIAL);

        FutureSingle(final @NotNull CompletableFuture<T> future) {
            this.future = future;
            future.whenComplete(this);
        }

        @Override
        protected void subscribeActual(final @NotNull SingleObserver<? super T> observer) {
            this.observer = observer;
            observer.onSubscribe(this);
            if (checkComplete(done)) {
                complete(observer, t, throwable);
            }
        }

        @Override
        public void dispose() {
            RxFutureConverter.dispose(done, future);
        }

        @Override
        public boolean isDisposed() {
            return RxFutureConverter.isDisposed(done);
        }

        @Override
        public void accept(final @Nullable T t, final @Nullable Throwable throwable) {
            this.t = t;
            this.throwable = throwable;
            if (checkComplete(done)) {
                final SingleObserver<? super T> observer = this.observer;
                assert observer != null;
                complete(observer, t, throwable);
            }
        }

        private static <T> void complete(
                final @NotNull SingleObserver<? super T> observer, final @Nullable T t,
                final @Nullable Throwable throwable) {

            if (t != null) {
                observer.onSuccess(t);
            } else {
                observer.onError((throwable != null) ? throwable : new NullPointerException());
            }
        }
    }

    private RxFutureConverter() {}
}
