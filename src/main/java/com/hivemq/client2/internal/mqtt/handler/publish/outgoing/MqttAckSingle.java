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

package com.hivemq.client2.internal.mqtt.handler.publish.outgoing;

import com.hivemq.client2.internal.annotations.CallByThread;
import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client2.internal.mqtt.ioc.ClientComponent;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPublishResult;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class MqttAckSingle extends Single<Mqtt5PublishResult> {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttPublish publish;

    public MqttAckSingle(final @NotNull MqttClientConfig clientConfig, final @NotNull MqttPublish publish) {
        this.clientConfig = clientConfig;
        this.publish = publish;
    }

    @Override
    protected void subscribeActual(final @NotNull SingleObserver<? super Mqtt5PublishResult> observer) {
        if (clientConfig.getState().isConnectedOrReconnect()) {
            final ClientComponent clientComponent = clientConfig.getClientComponent();
            final MqttOutgoingQosHandler outgoingQosHandler = clientComponent.outgoingQosHandler();
            final MqttPublishFlowables publishFlowables = outgoingQosHandler.getPublishFlowables();

            final Flow flow = new Flow(observer, clientConfig, outgoingQosHandler);
            observer.onSubscribe(flow);
            publishFlowables.add(Flowable.just(new MqttPublishWithFlow(publish, flow)));
        } else {
            EmptyDisposable.error(MqttClientStateExceptions.notConnected(), observer);
        }
    }

    private static class Flow extends MqttAckFlow implements Disposable {

        private final @NotNull SingleObserver<? super Mqtt5PublishResult> observer;
        private final @NotNull MqttOutgoingQosHandler outgoingQosHandler;

        private @Nullable MqttPublishResult result;

        Flow(
                final @NotNull SingleObserver<? super Mqtt5PublishResult> observer,
                final @NotNull MqttClientConfig clientConfig,
                final @NotNull MqttOutgoingQosHandler outgoingQosHandler) {

            super(clientConfig);
            this.observer = observer;
            this.outgoingQosHandler = outgoingQosHandler;
            init();
        }

        @CallByThread("Netty EventLoop")
        @Override
        void onNext(final @NotNull MqttPublishResult result) {
            if (result.acknowledged()) {
                done(result);
            } else {
                this.result = result;
            }
        }

        @CallByThread("Netty EventLoop")
        @Override
        void acknowledged(final long acknowledged) {
            final MqttPublishResult result = this.result;
            assert (acknowledged == 1) && (result != null) : "a single publish must be acknowledged exactly once";
            this.result = null;
            done(result);
        }

        @CallByThread("Netty EventLoop")
        private void done(final @NotNull MqttPublishResult result) {
            if (setDone()) {
                final Throwable error = result.getRawError();
                if (error == null) {
                    observer.onSuccess(result);
                } else {
                    observer.onError(error);
                }
            }
            outgoingQosHandler.request(1);
        }
    }
}
