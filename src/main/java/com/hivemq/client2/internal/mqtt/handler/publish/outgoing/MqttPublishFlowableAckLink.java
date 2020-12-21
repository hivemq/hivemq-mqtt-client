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

import com.hivemq.client2.internal.logging.InternalLogger;
import com.hivemq.client2.internal.logging.InternalLoggerFactory;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPublish;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
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

        private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(AckLinkSubscriber.class);

        static final int STATE_NONE = 0;
        static final int STATE_IN_PROGRESS = 1;
        static final int STATE_DONE = 2;
        static final int STATE_CANCELLED = 3;

        private final @NotNull Subscriber<? super MqttPublishWithFlow> subscriber;
        private final @NotNull MqttAckFlowableFlow ackFlow;
        private @Nullable Subscription subscription;
        private final @NotNull AtomicInteger state = new AtomicInteger(STATE_NONE);
        private final @NotNull AtomicInteger requestState = new AtomicInteger(STATE_NONE);
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
            ackFlow.link(this);
        }

        @Override
        public void onNext(final @NotNull MqttPublish publish) {
            if (state.compareAndSet(STATE_NONE, STATE_IN_PROGRESS)) {
                subscriber.onNext(new MqttPublishWithFlow(publish, ackFlow));
                published++;
                if (!state.compareAndSet(STATE_IN_PROGRESS, STATE_NONE)) {
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
            if (requestState.compareAndSet(STATE_NONE, STATE_IN_PROGRESS)) {
                subscription.request(n);
                if (!requestState.compareAndSet(STATE_IN_PROGRESS, STATE_NONE)) {
                    subscription.cancel();
                }
            }
        }

        @Override
        public void cancel() {
            LOGGER.error("MqttPublishFlowables is global and must never cancel. This must not happen and is a bug.");
        }

        @Override
        public void cancelLink() {
            if (state.getAndSet(STATE_CANCELLED) == STATE_NONE) {
                cancelActual();
            }
        }

        private void cancelActual() {
            assert subscription != null;
            if (requestState.getAndSet(STATE_CANCELLED) == STATE_NONE) {
                subscription.cancel();
            }
            subscriber.onComplete();
        }
    }
}
