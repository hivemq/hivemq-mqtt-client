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
import io.reactivex.rxjava3.internal.subscriptions.EmptySubscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 */
public class MqttAckSingleFlowable extends Flowable<Mqtt5PublishResult> {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttPublish publish;

    public MqttAckSingleFlowable(final @NotNull MqttClientConfig clientConfig, final @NotNull MqttPublish publish) {
        this.clientConfig = clientConfig;
        this.publish = publish;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super Mqtt5PublishResult> subscriber) {
        if (clientConfig.getState().isConnectedOrReconnect()) {
            final ClientComponent clientComponent = clientConfig.getClientComponent();
            final MqttOutgoingQosHandler outgoingQosHandler = clientComponent.outgoingQosHandler();
            final MqttPublishFlowables publishFlowables = outgoingQosHandler.getPublishFlowables();

            final Flow flow = new Flow(subscriber, clientConfig, outgoingQosHandler);
            subscriber.onSubscribe(flow);
            publishFlowables.add(Flowable.just(new MqttPublishWithFlow(publish, flow)));
        } else {
            EmptySubscription.error(MqttClientStateExceptions.notConnected(), subscriber);
        }
    }

    private static class Flow extends MqttAckFlow implements Subscription, Runnable {

        private static final int STATE_NONE = 0;
        private static final int STATE_RESULT = 1;
        private static final int STATE_REQUESTED = 2;
        private static final int STATE_CANCELLED = 3;

        private final @NotNull Subscriber<? super Mqtt5PublishResult> subscriber;
        private final @NotNull MqttOutgoingQosHandler outgoingQosHandler;

        private final @NotNull AtomicInteger state = new AtomicInteger(STATE_NONE);
        private @Nullable MqttPublishResult result;

        Flow(
                final @NotNull Subscriber<? super Mqtt5PublishResult> subscriber,
                final @NotNull MqttClientConfig clientConfig,
                final @NotNull MqttOutgoingQosHandler outgoingQosHandler) {

            super(clientConfig);
            this.subscriber = subscriber;
            this.outgoingQosHandler = outgoingQosHandler;
            init();
        }

        @CallByThread("Netty EventLoop")
        @Override
        void onNext(final @NotNull MqttPublishResult result) {
            switch (state.get()) {
                case STATE_NONE:
                    if (state.compareAndSet(STATE_NONE, STATE_RESULT)) {
                        this.result = result;
                    } else {
                        onNext(result);
                    }
                    break;
                case STATE_REQUESTED:
                    subscriber.onNext(result);
                    done(result);
                    break;
                case STATE_CANCELLED:
                    done(result);
                    break;
            }
        }

        @CallByThread("Netty EventLoop")
        private void done(final @NotNull MqttPublishResult result) {
            if (result.acknowledged()) {
                acknowledged(1);
            }
        }

        @CallByThread("Netty EventLoop")
        @Override
        void acknowledged(final long acknowledged) {
            assert acknowledged == 1 : "a single publish must be acknowledged exactly once";
            if (setDone()) {
                subscriber.onComplete();
            }
            outgoingQosHandler.request(1);
        }

        @Override
        public void request(final long n) {
            if ((n > 0) && (state.getAndSet(STATE_REQUESTED) == STATE_RESULT)) {
                eventLoop.execute(this);
            }
        }

        @Override
        protected void onCancel() {
            if (state.getAndSet(STATE_CANCELLED) == STATE_RESULT) {
                eventLoop.execute(this);
            }
        }

        @CallByThread("Netty EventLoop")
        @Override
        public void run() {
            final MqttPublishResult result = this.result;
            if (result != null) {
                this.result = null;
                if (!isCancelled()) {
                    subscriber.onNext(result);
                }
                done(result);
            }
        }
    }
}
