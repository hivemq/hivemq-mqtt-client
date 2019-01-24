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

package org.mqttbee.internal.mqtt.handler.publish.outgoing;

import io.reactivex.Flowable;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import io.reactivex.plugins.RxJavaPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.internal.mqtt.message.publish.MqttPublish;
import org.mqttbee.internal.rx.FuseableSubscriber;
import org.reactivestreams.Subscriber;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 */
public class MqttPublishFlowableAckLink extends Flowable<MqttPublishWithFlow> {

    private final @NotNull Flowable<MqttPublish> source;
    private final @NotNull MqttIncomingAckFlow ackFlow;

    MqttPublishFlowableAckLink(
            final @NotNull Flowable<MqttPublish> source, final @NotNull MqttIncomingAckFlow ackFlow) {

        this.source = source;
        this.ackFlow = ackFlow;
    }

    @Override
    protected void subscribeActual(final @NotNull Subscriber<? super MqttPublishWithFlow> s) {
        final AbstractAckLinkSubscriber<? extends Subscriber<? super MqttPublishWithFlow>> ackLinkSubscriber;
        if (s instanceof ConditionalSubscriber) {
            @SuppressWarnings("unchecked") final ConditionalSubscriber<? super MqttPublishWithFlow> cs =
                    (ConditionalSubscriber<? super MqttPublishWithFlow>) s;
            ackLinkSubscriber = new AckLinkConditionalSubscriber(cs, ackFlow);
        } else {
            ackLinkSubscriber = new AckLinkSubscriber(s, ackFlow);
        }
        source.subscribe(ackLinkSubscriber);
    }

    interface LinkCancellable {

        @NotNull LinkCancellable CANCELLED = () -> {};

        void cancelLink();
    }

    static abstract class AbstractAckLinkSubscriber<S extends Subscriber<? super MqttPublishWithFlow>>
            extends FuseableSubscriber<MqttPublish, MqttPublishWithFlow, S> implements LinkCancellable {

        static final int STATE_NONE = 0;
        static final int STATE_EMITTING = 1;
        static final int STATE_DONE = 2;
        static final int STATE_CANCEL = 3;
        static final int STATE_CANCELLED = 4;

        final @NotNull MqttIncomingAckFlow ackFlow;
        private boolean linked;
        private final @NotNull AtomicInteger state = new AtomicInteger();
        private final @NotNull AtomicInteger pollState = new AtomicInteger();
        long published;
        private @Nullable Throwable error;

        AbstractAckLinkSubscriber(final @NotNull S subscriber, final @NotNull MqttIncomingAckFlow ackFlow) {
            super(subscriber);
            this.ackFlow = ackFlow;
        }

        boolean startEmitting() {
            return startEmitting(state);
        }

        private boolean startEmitting(final @NotNull AtomicInteger state) {
            return state.compareAndSet(STATE_NONE, STATE_EMITTING);
        }

        void stopEmitting() {
            stopEmitting(state);
        }

        private void stopEmitting(final @NotNull AtomicInteger state) {
            assert subscription != null;
            if (!state.compareAndSet(STATE_EMITTING, STATE_NONE)) {
                cancelActual();
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
                        if (error == null) {
                            ackFlow.onComplete(published);
                        } else {
                            ackFlow.onError(error);
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
            final int state = this.state.getAndSet(STATE_CANCEL);
            if ((state == STATE_NONE) && (pollState.getAndSet(STATE_CANCEL) == STATE_NONE)) {
                cancelActual();
            } else if (state == STATE_DONE) {
                if (this.state.compareAndSet(STATE_CANCEL, STATE_CANCELLED)) {
                    ackFlow.onLinkCancelled();
                }
            }
        }

        private void cancelActual() {
            if (state.compareAndSet(STATE_CANCEL, STATE_CANCELLED)) {
                assert subscription != null;
                subscription.cancel();
                if (sourceMode != SYNC) {
                    subscriber.onComplete();
                }
                ackFlow.onLinkCancelled();
            }
        }
    }

    private static class AckLinkSubscriber extends AbstractAckLinkSubscriber<Subscriber<? super MqttPublishWithFlow>> {

        AckLinkSubscriber(
                final @NotNull Subscriber<? super MqttPublishWithFlow> subscriber,
                final @NotNull MqttIncomingAckFlow ackFlow) {

            super(subscriber, ackFlow);
        }

        @Override
        public void onNext(final @Nullable MqttPublish publish) {
            if (startEmitting()) {
                if (sourceMode == NONE) {
                    assert publish != null;
                    subscriber.onNext(new MqttPublishWithFlow(publish, ackFlow));
                    published++;
                } else {
                    subscriber.onNext(null);
                }
                stopEmitting();
            }
        }
    }

    private static class AckLinkConditionalSubscriber
            extends AbstractAckLinkSubscriber<ConditionalSubscriber<? super MqttPublishWithFlow>>
            implements ConditionalSubscriber<MqttPublish> {

        AckLinkConditionalSubscriber(
                final @NotNull ConditionalSubscriber<? super MqttPublishWithFlow> subscriber,
                final @NotNull MqttIncomingAckFlow ackFlow) {

            super(subscriber, ackFlow);
        }

        @Override
        public void onNext(final @Nullable MqttPublish publish) {
            assert subscription != null;
            if (!tryOnNext(publish)) {
                subscription.request(1);
            }
        }

        @Override
        public boolean tryOnNext(final @Nullable MqttPublish publish) {
            if (startEmitting()) {
                final boolean consumed;
                if (sourceMode == NONE) {
                    assert publish != null;
                    if (consumed = subscriber.tryOnNext(new MqttPublishWithFlow(publish, ackFlow))) {
                        published++;
                    }
                } else {
                    consumed = subscriber.tryOnNext(null);
                }
                stopEmitting();
                return consumed;
            }
            return true;
        }
    }
}
