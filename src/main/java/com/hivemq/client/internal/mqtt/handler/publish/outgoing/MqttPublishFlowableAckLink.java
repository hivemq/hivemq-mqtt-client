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

package com.hivemq.client.internal.mqtt.handler.publish.outgoing;

import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.plugins.RxJavaPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 */
public class MqttPublishFlowableAckLink extends Flowable<MqttPublishWithFlow> {

    private final @NotNull Flowable<MqttPublish> source;
    private final @NotNull MqttAckFlowableFlow ackFlow;

    MqttPublishFlowableAckLink(
            final @NotNull Flowable<MqttPublish> source, final @NotNull MqttAckFlowableFlow ackFlow) {

        this.source = source;
        this.ackFlow = ackFlow;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super MqttPublishWithFlow> s) {
        source.subscribe(new AckLinkSubscriber(s, ackFlow));
    }

    interface LinkedFlow {

        @NotNull LinkedFlow CANCELLED = () -> {};

        void cancelLink();
    }

    private static class AckLinkSubscriber implements FlowableSubscriber<MqttPublish>, Subscription, LinkedFlow {

        static final int STATE_NONE = 0;
        static final int STATE_EMITTING = 1;
        static final int STATE_DONE = 2;
        static final int STATE_CANCEL = 3;
        static final int STATE_CANCELLED = 4;

        private final @NotNull Subscriber<? super MqttPublishWithFlow> subscriber;
        private final @NotNull MqttAckFlowableFlow ackFlow;
        private @Nullable Subscription subscription;
        private boolean linked;
        private final @NotNull AtomicInteger state = new AtomicInteger();
        private long published;

        AckLinkSubscriber(
                final @NotNull Subscriber<? super MqttPublishWithFlow> subscriber,
                final @NotNull MqttAckFlowableFlow ackFlow) {

            this.subscriber = subscriber;
            this.ackFlow = ackFlow;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            this.subscription = subscription;
            subscriber.onSubscribe(this);
            link();
        }

        @Override
        public void onNext(final @NotNull MqttPublish publish) {
            if (state.compareAndSet(STATE_NONE, STATE_EMITTING)) {
                subscriber.onNext(new MqttPublishWithFlow(publish, ackFlow));
                published++;
                if (!state.compareAndSet(STATE_EMITTING, STATE_NONE)) {
                    cancelActual();
                }
            }
        }

        @Override
        public void onComplete() {
            if (state.compareAndSet(STATE_NONE, STATE_DONE)) {
                subscriber.onComplete();
                ackFlow.onComplete(published);
            }
        }

        @Override
        public void onError(final @NotNull Throwable error) {
            if (state.compareAndSet(STATE_NONE, STATE_DONE)) {
                subscriber.onComplete();
                ackFlow.onError(error, published);
            } else {
                RxJavaPlugins.onError(error);
            }
        }

        @Override
        public void request(final long n) {
            assert subscription != null;
            link();
            subscription.request(n);
        }

        @Override
        public void cancel() {
            assert subscription != null;
            subscription.cancel();
        }

        private void link() {
            if (!linked) {
                linked = true;
                ackFlow.link(this);
            }
        }

        @Override
        public void cancelLink() {
            if (state.getAndSet(STATE_CANCEL) == STATE_NONE) {
                cancelActual();
            }
        }

        private void cancelActual() {
            if (state.compareAndSet(STATE_CANCEL, STATE_CANCELLED)) {
                assert subscription != null;
                subscription.cancel();
                subscriber.onComplete();
            }
        }
    }
}
