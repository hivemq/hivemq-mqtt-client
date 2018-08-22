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

package org.mqttbee.mqtt.handler.publish.outgoing;

import io.reactivex.Flowable;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import io.reactivex.plugins.RxJavaPlugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.rx.FuseableSubscriber;
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
        ackFlow.link(ackLinkSubscriber);
    }

    interface LinkCancellable {

        void cancelLink();

    }

    static abstract class AbstractAckLinkSubscriber<S extends Subscriber<? super MqttPublishWithFlow>>
            extends FuseableSubscriber<MqttPublish, MqttPublishWithFlow, S> implements LinkCancellable {

        static final int STATE_NONE = 0;
        static final int STATE_EMITTING = 1;
        static final int STATE_DONE = 2;

        final @NotNull MqttIncomingAckFlow ackFlow;
        private final @NotNull AtomicInteger state = new AtomicInteger();
        long published;

        AbstractAckLinkSubscriber(final @NotNull S subscriber, final @NotNull MqttIncomingAckFlow ackFlow) {
            super(subscriber);
            this.ackFlow = ackFlow;
        }

        @Override
        public void onComplete() {
            if (state.compareAndSet(STATE_NONE, STATE_DONE)) {
                subscriber.onComplete();
                ackFlow.onComplete(published);
            }
        }

        @Override
        public void onError(final @NotNull Throwable t) {
            if (state.compareAndSet(STATE_NONE, STATE_DONE)) {
                subscriber.onComplete();
                ackFlow.onError(t, published);
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public @Nullable MqttPublishWithFlow poll() throws Exception {
            if (state.get() == STATE_DONE) {
                subscription.cancel();
                return null;
            }
            final MqttPublish publish = queueSubscription.poll();
            if (publish == null) {
                return null;
            }
            published++;
            return new MqttPublishWithFlow(publish, ackFlow);
        }

        @Override
        public void cancelLink() {
            if ((state.getAndSet(STATE_DONE) == STATE_NONE) && (sourceMode != SYNC)) {
                subscription.cancel();
                subscriber.onComplete();
            }
        }

        boolean startEmitting() {
            return state.compareAndSet(STATE_NONE, STATE_EMITTING);
        }

        void stopEmitting() {
            if (!state.compareAndSet(STATE_EMITTING, STATE_NONE)) {
                subscription.cancel();
                subscriber.onComplete();
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
        public void onNext(final @NotNull MqttPublish publish) {
            if (startEmitting()) {
                if (sourceMode == ASYNC) {
                    subscriber.onNext(null);
                } else {
                    subscriber.onNext(new MqttPublishWithFlow(publish, ackFlow));
                    published++;
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
        public void onNext(final @NotNull MqttPublish publish) {
            if (!tryOnNext(publish)) {
                subscription.request(1);
            }
        }

        @Override
        public boolean tryOnNext(final @NotNull MqttPublish publish) {
            if (startEmitting()) {
                final boolean consumed;
                if (sourceMode == ASYNC) {
                    consumed = subscriber.tryOnNext(null);
                } else {
                    if (consumed = subscriber.tryOnNext(new MqttPublishWithFlow(publish, ackFlow))) {
                        published++;
                    }
                }
                stopEmitting();
                return consumed;
            }
            return true;
        }

    }

}
