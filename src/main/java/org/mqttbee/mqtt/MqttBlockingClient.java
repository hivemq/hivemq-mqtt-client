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

package org.mqttbee.mqtt;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5BlockingClient;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.reactivestreams.Subscription;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Silvio Giebl
 */
public class MqttBlockingClient implements Mqtt5BlockingClient {

    static @NotNull Mqtt5SubAck handleSubAck(final @NotNull Mqtt5SubAck subAck) {
        for (final Mqtt5ReasonCode reasonCode : subAck.getReasonCodes()) {
            if (reasonCode.isError()) {
                throw new Mqtt5MessageException(subAck, "SUBACK contains at least one error code");
            }
        }
        return subAck;
    }

    static @NotNull Mqtt5UnsubAck handleUnsubAck(final @NotNull Mqtt5UnsubAck unsubAck) {
        for (final Mqtt5ReasonCode reasonCode : unsubAck.getReasonCodes()) {
            if (reasonCode.isError()) {
                throw new Mqtt5MessageException(unsubAck, "UNSUBACK contains at least one error code");
            }
        }
        return unsubAck;
    }

    static @NotNull Mqtt5PublishResult handlePublish(final @NotNull Mqtt5PublishResult publishResult) {
        final Throwable error = publishResult.getError();
        if (error != null) {
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            }
            throw new RuntimeException(error);
        }
        return publishResult;
    }

    private final @NotNull MqttRxClient delegate;

    MqttBlockingClient(final @NotNull MqttRxClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Mqtt5ConnAck connect(final @NotNull Mqtt5Connect connect) {
        return delegate.connectUnsafe(connect).blockingGet();
    }

    @Override
    public @NotNull Mqtt5SubAck subscribe(final @NotNull Mqtt5Subscribe subscribe) {
        return handleSubAck(delegate.subscribeUnsafe(subscribe).blockingGet());
    }

    @Override
    public @NotNull Mqtt5Publishes publishes(final @NotNull MqttGlobalPublishFilter filter) {
        return new MqttPublishes(delegate.publishesUnsafe(filter));
    }

    @Override
    public @NotNull Mqtt5UnsubAck unsubscribe(final @NotNull Mqtt5Unsubscribe unsubscribe) {
        return handleUnsubAck(delegate.unsubscribeUnsafe(unsubscribe).blockingGet());
    }

    @Override
    public @NotNull Mqtt5PublishResult publish(final @NotNull Mqtt5Publish publish) {
        return handlePublish(delegate.publishUnsafe(Flowable.just(publish)).singleOrError().blockingGet());
    }

    @Override
    public void reauth() {
        delegate.reauthUnsafe().blockingAwait();
    }

    @Override
    public void disconnect(final @NotNull Mqtt5Disconnect disconnect) {
        delegate.disconnectUnsafe(disconnect).blockingAwait();
    }

    @Override
    public @NotNull MqttClientData getClientData() {
        return delegate.getClientData();
    }

    @Override
    public @NotNull MqttRxClient toRx() {
        return delegate;
    }

    @Override
    public @NotNull MqttAsyncClient toAsync() {
        return delegate.toAsync();
    }

    private static class MqttPublishes implements Mqtt5Publishes, FlowableSubscriber<Mqtt5Publish> {

        private final @NotNull AtomicReference<@Nullable Subscription> subscription = new AtomicReference<>();
        private final @NotNull LinkedList<Entry> entries = new LinkedList<>();
        private @Nullable Mqtt5Publish queuedPublish;
        private boolean cancelled;

        MqttPublishes(final @NotNull Flowable<Mqtt5Publish> publishes) {
            publishes.subscribe(this);
        }

        @Override
        public void onSubscribe(final @NotNull Subscription subscription) {
            if (this.subscription.compareAndSet(null, subscription)) {
                subscription.request(1);
            } else {
                subscription.cancel();
            }
        }

        private void request() {
            final Subscription subscription = this.subscription.get();
            assert subscription != null;
            subscription.request(1);
        }

        @Override
        public void onNext(final @NotNull Mqtt5Publish publish) {
            synchronized (entries) {
                if (cancelled) {
                    return;
                }
                Entry entry;
                while ((entry = entries.poll()) != null) {
                    final boolean success = entry.result.compareAndSet(null, publish);
                    entry.latch.countDown();
                    if (success) {
                        request();
                        return;
                    }
                }
            }
            synchronized (this) {
                final boolean retry;
                synchronized (entries) {
                    retry = !entries.isEmpty();
                }
                if (retry) {
                    onNext(publish);
                } else {
                    queuedPublish = publish;
                }
            }
        }

        @Override
        public void onComplete() {
            onError(new IllegalStateException());
        }

        @Override
        public void onError(final @NotNull Throwable t) {
            synchronized (entries) {
                if (cancelled) {
                    return;
                }
                Entry entry;
                while ((entry = entries.poll()) != null) {
                    entry.result.set(t);
                    entry.latch.countDown();
                }
            }
        }

        @Override
        public @NotNull Mqtt5Publish receive() throws InterruptedException {
            final Entry entry;
            synchronized (this) {
                final Mqtt5Publish publish = receiveNowUnsafe();
                if (publish != null) {
                    return publish;
                }
                entry = addEntry();
            }

            InterruptedException interruptedException = null;
            try {
                entry.latch.await();
            } catch (final InterruptedException e) {
                interruptedException = e;
            }
            final Object result = entry.result.getAndSet(Entry.CANCELLED);
            if (result instanceof Mqtt5Publish) {
                return (Mqtt5Publish) result;
            }
            if (result instanceof Throwable) {
                if (result instanceof RuntimeException) {
                    throw (RuntimeException) result;
                }
                throw new RuntimeException((Throwable) result);
            }
            if (interruptedException != null) {
                throw interruptedException;
            }
            throw new InterruptedException();
        }

        @Override
        public @NotNull Optional<Mqtt5Publish> receive(final long timeout, final @NotNull TimeUnit timeUnit)
                throws InterruptedException {

            if (timeout < 0) {
                throw new IllegalArgumentException("Timeout must be greater than 0.");
            }
            Objects.requireNonNull(timeUnit, "Time unit must not be null.");

            final Entry entry;
            synchronized (this) {
                final Mqtt5Publish publish = receiveNowUnsafe();
                if (publish != null) {
                    return Optional.of(publish);
                }
                entry = addEntry();
            }

            InterruptedException interruptedException = null;
            try {
                entry.latch.await(timeout, timeUnit);
            } catch (final InterruptedException e) {
                interruptedException = e;
            }
            final Object result = entry.result.getAndSet(Entry.CANCELLED);
            if (result instanceof Mqtt5Publish) {
                return Optional.of((Mqtt5Publish) result);
            }
            if (result instanceof Throwable) {
                if (result instanceof RuntimeException) {
                    throw (RuntimeException) result;
                }
                throw new RuntimeException((Throwable) result);
            }
            if (interruptedException != null) {
                throw interruptedException;
            }
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Mqtt5Publish> receiveNow() {
            final Mqtt5Publish publish;
            synchronized (this) {
                publish = receiveNowUnsafe();
            }
            return Optional.ofNullable(publish);
        }

        private @Nullable Mqtt5Publish receiveNowUnsafe() {
            if (queuedPublish != null) {
                queuedPublish = null;
                request();
                return queuedPublish;
            }
            return null;
        }

        private @NotNull Entry addEntry() {
            synchronized (entries) {
                if (cancelled) {
                    throw new CancellationException();
                }
                final Entry entry = new Entry();
                entries.offer(entry);
                return entry;
            }
        }

        @Override
        public void close() {
            final Subscription subscription = this.subscription.getAndSet(SubscriptionHelper.CANCELLED);
            if (subscription != null) {
                subscription.cancel();
            }
            synchronized (entries) {
                if (cancelled) {
                    return;
                }
                cancelled = true;
                Entry entry;
                while ((entry = entries.poll()) != null) {
                    entry.result.set(new CancellationException());
                    entry.latch.countDown();
                }
            }
        }

        private static class Entry {

            static final @NotNull Object CANCELLED = new Object();

            final @NotNull CountDownLatch latch = new CountDownLatch(1);
            final @NotNull AtomicReference<@Nullable Object> result = new AtomicReference<>();
        }
    }
}
