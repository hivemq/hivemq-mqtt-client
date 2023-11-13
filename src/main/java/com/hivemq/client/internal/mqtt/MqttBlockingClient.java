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

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectBuilder;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnectBuilder;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribeBuilder;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribeBuilder;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.AsyncRuntimeException;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.internal.util.collections.NodeList;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5UnsubAckException;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;

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
                throw new Mqtt5SubAckException(subAck, "SUBACK contains at least one error code.");
            }
        }
        return subAck;
    }

    static @NotNull Mqtt5UnsubAck handleUnsubAck(final @NotNull Mqtt5UnsubAck unsubAck) {
        for (final Mqtt5ReasonCode reasonCode : unsubAck.getReasonCodes()) {
            if (reasonCode.isError()) {
                throw new Mqtt5UnsubAckException(unsubAck, "UNSUBACK contains at least one error code.");
            }
        }
        return unsubAck;
    }

    private final @NotNull MqttRxClient delegate;

    MqttBlockingClient(final @NotNull MqttRxClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Mqtt5ConnAck connect() {
        return connect(MqttConnect.DEFAULT);
    }

    @Override
    public @NotNull Mqtt5ConnAck connect(final @Nullable Mqtt5Connect connect) {
        final MqttConnect mqttConnect = MqttChecks.connect(connect);
        try {
            return delegate.connectUnsafe(mqttConnect).blockingGet();
        } catch (final RuntimeException e) {
            throw AsyncRuntimeException.fillInStackTrace(e);
        }
    }

    @Override
    public MqttConnectBuilder.@NotNull Send<Mqtt5ConnAck> connectWith() {
        return new MqttConnectBuilder.Send<>(this::connect);
    }

    @Override
    public @NotNull Mqtt5SubAck subscribe(final @Nullable Mqtt5Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);
        try {
            if (!getState().isConnectedOrReconnect()) {
                throw MqttClientStateExceptions.notConnected();
            }
            return handleSubAck(delegate.subscribeUnsafe(mqttSubscribe).blockingGet());
        } catch (final RuntimeException e) {
            throw AsyncRuntimeException.fillInStackTrace(e);
        }
    }

    @Override
    public MqttSubscribeBuilder.@NotNull Send<Mqtt5SubAck> subscribeWith() {
        return new MqttSubscribeBuilder.Send<>(this::subscribe);
    }

    @Override
    public @NotNull Mqtt5Publishes publishes(final @Nullable MqttGlobalPublishFilter filter) {
        return publishes(filter, false);
    }

    @Override
    public @NotNull Mqtt5Publishes publishes(
            final @Nullable MqttGlobalPublishFilter filter, final boolean manualAcknowledgement) {

        Checks.notNull(filter, "Global publish filter");

        return new MqttPublishes(delegate.publishesUnsafe(filter, manualAcknowledgement));
    }

    @Override
    public @NotNull Mqtt5UnsubAck unsubscribe(final @Nullable Mqtt5Unsubscribe unsubscribe) {
        final MqttUnsubscribe mqttUnsubscribe = MqttChecks.unsubscribe(unsubscribe);
        try {
            if (!getState().isConnectedOrReconnect()) {
                throw MqttClientStateExceptions.notConnected();
            }
            return handleUnsubAck(delegate.unsubscribeUnsafe(mqttUnsubscribe).blockingGet());
        } catch (final RuntimeException e) {
            throw AsyncRuntimeException.fillInStackTrace(e);
        }
    }

    @Override
    public MqttUnsubscribeBuilder.@NotNull Send<Mqtt5UnsubAck> unsubscribeWith() {
        return new MqttUnsubscribeBuilder.Send<>(this::unsubscribe);
    }

    @Override
    public @NotNull Mqtt5PublishResult publish(final @Nullable Mqtt5Publish publish) {
        final MqttPublish mqttPublish = MqttChecks.publish(publish);
        try {
            return delegate.publishUnsafe(mqttPublish).blockingGet();
        } catch (final RuntimeException e) {
            throw AsyncRuntimeException.fillInStackTrace(e);
        }
    }

    @Override
    public MqttPublishBuilder.@NotNull Send<Mqtt5PublishResult> publishWith() {
        return new MqttPublishBuilder.Send<>(this::publish);
    }

    @Override
    public void reauth() {
        try {
            delegate.reauthUnsafe().blockingAwait();
        } catch (final RuntimeException e) {
            throw AsyncRuntimeException.fillInStackTrace(e);
        }
    }

    @Override
    public void disconnect() {
        disconnect(MqttDisconnect.DEFAULT);
    }

    @Override
    public void disconnect(final @NotNull Mqtt5Disconnect disconnect) {
        final MqttDisconnect mqttDisconnect = MqttChecks.disconnect(disconnect);
        try {
            delegate.disconnectUnsafe(mqttDisconnect).blockingAwait();
        } catch (final RuntimeException e) {
            throw AsyncRuntimeException.fillInStackTrace(e);
        }
    }

    @Override
    public MqttDisconnectBuilder.@NotNull SendVoid disconnectWith() {
        return new MqttDisconnectBuilder.SendVoid(this::disconnect);
    }

    @Override
    public @NotNull MqttClientConfig getConfig() {
        return delegate.getConfig();
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
        private final @NotNull NodeList<Entry> entries = new NodeList<>();
        private @Nullable Mqtt5Publish queuedPublish;
        private @Nullable Throwable error;

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
                if (error != null) {
                    return;
                }
                final Entry entry = entries.getFirst();
                if (entry == null) {
                    queuedPublish = publish;
                } else {
                    entries.remove(entry);
                    entry.result = publish;
                    entry.latch.countDown();
                    request();
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
                if (error != null) {
                    return;
                }
                error = t;
                for (Entry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    entries.remove(entry);
                    entry.result = t;
                    entry.latch.countDown();
                }
            }
        }

        @Override
        public @NotNull Mqtt5Publish receive() throws InterruptedException {
            final Entry entry;
            synchronized (entries) {
                if (error != null) {
                    throw handleError(error);
                }
                final Mqtt5Publish publish = receiveNowUnsafe();
                if (publish != null) {
                    return publish;
                }
                entry = new Entry();
                entries.add(entry);
            }

            Object result;
            try {
                entry.latch.await();
                result = entry.result;
                assert (result instanceof Mqtt5Publish) || (result instanceof Throwable);
            } catch (final InterruptedException e) {
                result = tryCancel(entry, e);
            }
            if (result instanceof Mqtt5Publish) {
                return (Mqtt5Publish) result;
            }
            if (result instanceof Throwable) {
                if (result instanceof InterruptedException) {
                    throw (InterruptedException) result;
                }
                throw handleError((Throwable) result);
            }
            throw new IllegalStateException("This must not happen and is a bug.");
        }

        @Override
        public @NotNull Optional<Mqtt5Publish> receive(final long timeout, final @Nullable TimeUnit timeUnit)
                throws InterruptedException {

            if (timeout < 0) {
                throw new IllegalArgumentException("Timeout must be greater than 0.");
            }
            Checks.notNull(timeUnit, "Time unit");

            final Entry entry;
            synchronized (entries) {
                if (error != null) {
                    throw handleError(error);
                }
                final Mqtt5Publish publish = receiveNowUnsafe();
                if (publish != null) {
                    return Optional.of(publish);
                }
                entry = new Entry();
                entries.add(entry);
            }

            Object result;
            try {
                if (entry.latch.await(timeout, timeUnit)) {
                    result = entry.result;
                    assert (result instanceof Mqtt5Publish) || (result instanceof Throwable);
                } else {
                    result = tryCancel(entry, null);
                }
            } catch (final InterruptedException e) {
                result = tryCancel(entry, e);
            }
            if (result instanceof Mqtt5Publish) {
                return Optional.of((Mqtt5Publish) result);
            }
            if (result instanceof Throwable) {
                if (result instanceof InterruptedException) {
                    throw (InterruptedException) result;
                }
                throw handleError((Throwable) result);
            }
            return Optional.empty();
        }

        @Override
        public @NotNull Optional<Mqtt5Publish> receiveNow() {
            final Mqtt5Publish publish;
            synchronized (entries) {
                if (error != null) {
                    throw handleError(error);
                }
                publish = receiveNowUnsafe();
            }
            return Optional.ofNullable(publish);
        }

        private @Nullable Mqtt5Publish receiveNowUnsafe() {
            final Mqtt5Publish queuedPublish = this.queuedPublish;
            if (queuedPublish != null) {
                this.queuedPublish = null;
                request();
            }
            return queuedPublish;
        }

        private @Nullable Object tryCancel(final @NotNull Entry entry, final @Nullable Object resultOnCancel) {
            synchronized (entries) {
                final Object result = entry.result;
                if (result == null) {
                    entries.remove(entry);
                    return resultOnCancel;
                } else {
                    assert (result instanceof Mqtt5Publish) || (result instanceof Throwable);
                    return result;
                }
            }
        }

        @Override
        public void close() {
            final Subscription subscription = this.subscription.getAndSet(SubscriptionHelper.CANCELLED);
            if (subscription != null) {
                subscription.cancel();
            }
            synchronized (entries) {
                if (error != null) {
                    return;
                }
                error = new CancellationException();
                for (Entry entry = entries.getFirst(); entry != null; entry = entry.getNext()) {
                    entries.remove(entry);
                    entry.result = error;
                    entry.latch.countDown();
                }
            }
        }

        private @NotNull RuntimeException handleError(final @NotNull Throwable t) {
            if (t instanceof RuntimeException) {
                return AsyncRuntimeException.fillInStackTrace((RuntimeException) t);
            }
            throw new RuntimeException(t);
        }

        private static class Entry extends NodeList.Node<Entry> {

            final @NotNull CountDownLatch latch = new CountDownLatch(1);
            @Nullable Object result = null;
        }
    }
}
