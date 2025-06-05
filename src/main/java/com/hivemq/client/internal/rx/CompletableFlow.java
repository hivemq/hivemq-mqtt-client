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

import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class CompletableFlow implements Disposable {

    private final @NotNull CompletableObserver observer;
    private volatile boolean disposed;

    public CompletableFlow(final @NotNull CompletableObserver observer) {
        this.observer = observer;
    }

    public void onComplete() {
        observer.onComplete();
    }

    public void onError(final @NotNull Throwable error) {
        observer.onError(error);
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public boolean isCancelled() {
        return isDisposed();
    }
}
