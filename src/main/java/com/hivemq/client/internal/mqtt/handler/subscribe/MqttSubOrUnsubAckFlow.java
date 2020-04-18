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

package com.hivemq.client.internal.mqtt.handler.subscribe;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.handler.util.FlowWithEventLoop;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
class MqttSubOrUnsubAckFlow<T> extends FlowWithEventLoop implements MqttSubscriptionFlow<T>, Disposable {

    private final @NotNull SingleObserver<? super T> observer;

    MqttSubOrUnsubAckFlow(
            final @NotNull SingleObserver<? super T> observer, final @NotNull MqttClientConfig clientConfig) {

        super(clientConfig);
        this.observer = observer;
    }

    @Override
    public void onSuccess(final @NotNull T t) {
        if (setDone()) {
            observer.onSuccess(t);
        }
    }

    @Override
    public void onError(final @NotNull Throwable error) {
        if (setDone()) {
            observer.onError(error);
        }
    }
}
