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

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Silvio Giebl
 */
public interface SingleFlow<T> {

    void onSuccess(@NotNull T t);

    void onError(@NotNull Throwable t);

    boolean isCancelled();

    class DefaultSingleFlow<T> implements SingleFlow<T>, Disposable {

        private final SingleObserver<? super T> observer;
        private final AtomicBoolean disposed = new AtomicBoolean();

        public DefaultSingleFlow(@NotNull final SingleObserver<? super T> observer) {
            this.observer = observer;
        }

        @Override
        public void onSuccess(@NotNull final T t) {
            observer.onSuccess(t);
        }

        @Override
        public void onError(@NotNull final Throwable t) {
            observer.onError(t);
        }

        @Override
        public void dispose() {
            disposed.set(true);
        }

        @Override
        public boolean isDisposed() {
            return disposed.get();
        }

        @Override
        public boolean isCancelled() {
            return isDisposed();
        }

    }

}
