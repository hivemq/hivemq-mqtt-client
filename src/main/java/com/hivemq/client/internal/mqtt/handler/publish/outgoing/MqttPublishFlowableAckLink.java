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
import com.hivemq.client.internal.rx.FuseableSubscriber;
import io.reactivex.Flowable;
import io.reactivex.plugins.RxJavaPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscriber;

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

    interface LinkCancellable {

        @NotNull LinkCancellable CANCELLED = () -> {};

        void cancelLink();
    }

    private static class AckLinkSubscriber
            extends FuseableSubscriber<MqttPublish, MqttPublishWithFlow, Subscriber<? super MqttPublishWithFlow>>
            implements LinkCancellable {

        static final int STATE_NONE = 0;
        static final int STATE_EMITTING = 1;
        static final int STATE_DONE = 2;
        static final int STATE_CANCEL = 3;
        static final int STATE_CANCELLED = 4;

        private final @NotNull MqttAckFlowableFlow ackFlow;
        private boolean linked;
        private final @NotNull AtomicInteger state = new AtomicInteger();
        private final @NotNull AtomicInteger pollState = new AtomicInteger();
        private long published;
        private @Nullable Throwable error;

        AckLinkSubscriber(
                final @NotNull Subscriber<? super MqttPublishWithFlow> subscriber,
                final @NotNull MqttAckFlowableFlow ackFlow) {

            super(subscriber);
            this.ackFlow = ackFlow;
        }

        private boolean startEmitting(final @NotNull AtomicInteger state) {
            return state.compareAndSet(STATE_NONE, STATE_EMITTING);
        }

        private void stopEmitting(final @NotNull AtomicInteger state) {
            if (!state.compareAndSet(STATE_EMITTING, STATE_NONE)) {
                cancelActual();
            }
        }

        @Override
        public void onNext(final @Nullable MqttPublish publish) {
            if (startEmitting(state)) {
                if (sourceMode == NONE) {
                    assert publish != null;
                    subscriber.onNext(new MqttPublishWithFlow(publish, ackFlow));
                    published++;
                } else {
                    subscriber.onNext(null);
                }
                stopEmitting(state);
            }
        }

        @Override
        public void onComplete() {
            if (state.compareAndSet(STATE_NONE, STATE_DONE)) {
                subscriber.onComplete();
                if (sourceMode == NONE) {
                    ackFlow.onComplete(published);
                }
            }
        }

        @Override
        public void onError(final @NotNull Throwable t) {
            error = t;
            if (state.compareAndSet(STATE_NONE, STATE_DONE)) {
                subscriber.onComplete();
                if (sourceMode == NONE) {
                    ackFlow.onError(t, published);
                }
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public void request(final long n) {
            link();
            super.request(n);
        }

        @Override
        public int requestFusion(final int mode) {
            if (queueSubscription != null) {
                sourceMode = queueSubscription.requestFusion(mode);
            }
            link();
            return sourceMode;
        }

        @Override
        public @Nullable MqttPublishWithFlow poll() {
            assert queueSubscription != null;
            if (!startEmitting(pollState)) {
                return null;
            }
            final MqttPublish publish;
            try {
                publish = queueSubscription.poll();
            } catch (final Throwable e) {
                queueSubscription.cancel();
                pollState.set(STATE_DONE);
                if (state.getAndSet(STATE_DONE) != STATE_DONE) {
                    ackFlow.onError(e, published);
                    if (sourceMode == ASYNC) {
                        subscriber.onComplete();
                    }
                }
                return null;
            }
            if (publish == null) {
                if (sourceMode == SYNC) {
                    pollState.set(STATE_DONE);
                    if (state.getAndSet(STATE_DONE) != STATE_DONE) {
                        ackFlow.onComplete(published);
                    }
                } else { // ASYNC
                    if (state.get() == STATE_DONE) {
                        final Throwable error = this.error;
                        if (error == null) {
                            ackFlow.onComplete(published);
                        } else {
                            ackFlow.onError(error, published);
                        }
                    }
                    stopEmitting(pollState);
                }
                return null;
            }
            stopEmitting(pollState);
            published++;
            return new MqttPublishWithFlow(publish, ackFlow);
        }

        private void link() {
            if (!linked) {
                linked = true;
                ackFlow.link(this);
            }
        }

        @Override
        public void cancelLink() {
            final int previousState = state.getAndSet(STATE_CANCEL);
            if ((previousState == STATE_NONE) && (pollState.getAndSet(STATE_CANCEL) == STATE_NONE)) {
                cancelActual();
            }
        }

        private void cancelActual() {
            if (state.compareAndSet(STATE_CANCEL, STATE_CANCELLED)) {
                assert subscription != null;
                subscription.cancel();
                if (sourceMode != SYNC) {
                    subscriber.onComplete();
                }
            }
        }
    }
}
