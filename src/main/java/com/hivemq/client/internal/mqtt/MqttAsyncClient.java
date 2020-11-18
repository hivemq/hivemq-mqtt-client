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
import com.hivemq.client.internal.rx.RxFutureConverter;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
public class MqttAsyncClient implements Mqtt5AsyncClient {

    private static @NotNull CompletableFuture<@NotNull Mqtt5SubAck> handleSubAck(
            final @NotNull CompletableFuture<@NotNull Mqtt5SubAck> future, final @NotNull MqttSubscribe subscribe) {

        if (subscribe.getSubscriptions().size() == 1) {
            return future;
        }
        final CompletableFuture<Mqtt5SubAck> mappedFuture = new CompletableFuture<>();
        future.whenComplete((subAck, throwable) -> {
            if (throwable != null) {
                mappedFuture.completeExceptionally(throwable);
            } else {
                try {
                    mappedFuture.complete(MqttBlockingClient.handleSubAck(subAck));
                } catch (final Throwable t) {
                    mappedFuture.completeExceptionally(t);
                }
            }
        });
        return mappedFuture;
    }

    private static @NotNull CompletableFuture<@NotNull Mqtt5UnsubAck> handleUnsubAck(
            final @NotNull CompletableFuture<@NotNull Mqtt5UnsubAck> future,
            final @NotNull MqttUnsubscribe unsubscribe) {

        if (unsubscribe.getTopicFilters().size() == 1) {
            return future;
        }
        final CompletableFuture<Mqtt5UnsubAck> mappedFuture = new CompletableFuture<>();
        future.whenComplete((unsubAck, throwable) -> {
            if (throwable != null) {
                mappedFuture.completeExceptionally(throwable);
            } else {
                try {
                    mappedFuture.complete(MqttBlockingClient.handleUnsubAck(unsubAck));
                } catch (final Throwable t) {
                    mappedFuture.completeExceptionally(t);
                }
            }
        });
        return mappedFuture;
    }

    private final @NotNull MqttRxClient delegate;

    MqttAsyncClient(final @NotNull MqttRxClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5ConnAck> connect() {
        return connect(MqttConnect.DEFAULT);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5ConnAck> connect(final @Nullable Mqtt5Connect connect) {
        final MqttConnect mqttConnect = MqttChecks.connect(connect);

        return RxFutureConverter.toFuture(delegate.connect(mqttConnect));
    }

    @Override
    public MqttConnectBuilder.@NotNull Send<CompletableFuture<Mqtt5ConnAck>> connectWith() {
        return new MqttConnectBuilder.Send<>(this::connect);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(final @Nullable Mqtt5Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);

        return handleSubAck(RxFutureConverter.toFuture(delegate.subscribe(mqttSubscribe)), mqttSubscribe);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            final @Nullable Mqtt5Subscribe subscribe, final @Nullable Consumer<@NotNull Mqtt5Publish> callback) {

        return subscribe(subscribe, callback, false);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            final @Nullable Mqtt5Subscribe subscribe,
            final @Nullable Consumer<@NotNull Mqtt5Publish> callback,
            final @Nullable Executor executor) {

        return subscribe(subscribe, callback, executor, false);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            final @Nullable Mqtt5Subscribe subscribe,
            final @Nullable Consumer<@NotNull Mqtt5Publish> callback,
            final boolean manualAcknowledgement) {

        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);
        Checks.notNull(callback, "Callback");

        return handleSubAck(delegate.subscribePublishes(mqttSubscribe, manualAcknowledgement)
                .subscribeSingleFuture(new CallbackSubscriber(callback)), mqttSubscribe);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            final @Nullable Mqtt5Subscribe subscribe,
            final @Nullable Consumer<@NotNull Mqtt5Publish> callback,
            final @Nullable Executor executor,
            final boolean manualAcknowledgement) {

        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);
        Checks.notNull(callback, "Callback");
        Checks.notNull(executor, "Executor");

        return handleSubAck(delegate.subscribePublishesUnsafe(mqttSubscribe, manualAcknowledgement)
                .observeBothOn(Schedulers.from(executor), true)
                .subscribeSingleFuture(new CallbackSubscriber(callback)), mqttSubscribe);
    }

    @Override
    public @NotNull SubscribeBuilder subscribeWith() {
        return new SubscribeBuilder();
    }

    @Override
    public void publishes(
            final @Nullable MqttGlobalPublishFilter filter, final @Nullable Consumer<@NotNull Mqtt5Publish> callback) {

        publishes(filter, callback, false);
    }

    @Override
    public void publishes(
            final @Nullable MqttGlobalPublishFilter filter,
            final @Nullable Consumer<@NotNull Mqtt5Publish> callback,
            final @Nullable Executor executor) {

        publishes(filter, callback, executor, false);
    }

    @Override
    public void publishes(
            final @Nullable MqttGlobalPublishFilter filter,
            final @Nullable Consumer<@NotNull Mqtt5Publish> callback,
            final boolean manualAcknowledgement) {

        Checks.notNull(filter, "Global publish filter");
        Checks.notNull(callback, "Callback");

        delegate.publishes(filter, manualAcknowledgement).subscribe(new CallbackSubscriber(callback));
    }

    @Override
    public void publishes(
            final @Nullable MqttGlobalPublishFilter filter,
            final @Nullable Consumer<@NotNull Mqtt5Publish> callback,
            final @Nullable Executor executor,
            final boolean manualAcknowledgement) {

        Checks.notNull(filter, "Global publish filter");
        Checks.notNull(callback, "Callback");
        Checks.notNull(executor, "Executor");

        delegate.publishesUnsafe(filter, manualAcknowledgement)
                .observeOn(Schedulers.from(executor), true)
                .subscribe(new CallbackSubscriber(callback));
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5UnsubAck> unsubscribe(
            final @Nullable Mqtt5Unsubscribe unsubscribe) {

        final MqttUnsubscribe mqttUnsubscribe = MqttChecks.unsubscribe(unsubscribe);

        return handleUnsubAck(RxFutureConverter.toFuture(delegate.unsubscribe(mqttUnsubscribe)), mqttUnsubscribe);
    }

    @Override
    public MqttUnsubscribeBuilder.@NotNull Send<CompletableFuture<Mqtt5UnsubAck>> unsubscribeWith() {
        return new MqttUnsubscribeBuilder.Send<>(this::unsubscribe);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5PublishResult> publish(final @Nullable Mqtt5Publish publish) {
        final MqttPublish mqttPublish = MqttChecks.publish(publish);

        return RxFutureConverter.toFuture(delegate.publish(mqttPublish));
    }

    @Override
    public MqttPublishBuilder.@NotNull Send<CompletableFuture<Mqtt5PublishResult>> publishWith() {
        return new MqttPublishBuilder.Send<>(this::publish);
    }

    @Override
    public @NotNull CompletableFuture<Void> reauth() {
        return RxFutureConverter.toFuture(delegate.reauth());
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnect() {
        return disconnect(MqttDisconnect.DEFAULT);
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnect(final @Nullable Mqtt5Disconnect disconnect) {
        final MqttDisconnect mqttDisconnect = MqttChecks.disconnect(disconnect);

        return RxFutureConverter.toFuture(delegate.disconnect(mqttDisconnect));
    }

    @Override
    public MqttDisconnectBuilder.@NotNull Send<CompletableFuture<Void>> disconnectWith() {
        return new MqttDisconnectBuilder.Send<>(this::disconnect);
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
    public @NotNull MqttBlockingClient toBlocking() {
        return delegate.toBlocking();
    }

    private static class CallbackSubscriber implements FlowableSubscriber<Mqtt5Publish> {

        private final @NotNull Consumer<Mqtt5Publish> callback;

        private CallbackSubscriber(final @NotNull Consumer<Mqtt5Publish> callback) {
            this.callback = callback;
        }

        @Override
        public void onSubscribe(final @NotNull Subscription s) {
            s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(final @NotNull Mqtt5Publish publish) {
            callback.accept(publish);
        }

        @Override
        public void onComplete() {}

        @Override
        public void onError(final @NotNull Throwable t) {}
    }

    private class SubscribeBuilder extends MqttSubscribeBuilder<SubscribeBuilder>
            implements Mqtt5AsyncClient.SubscribeBuilder.Start.Complete,
            Mqtt5AsyncClient.SubscribeBuilder.AfterCallback {

        private @Nullable Consumer<Mqtt5Publish> callback;
        private @Nullable Executor executor;
        private boolean manualAcknowledgement;

        @Override
        protected @NotNull SubscribeBuilder self() {
            return this;
        }

        @Override
        public @NotNull SubscribeBuilder callback(final @Nullable Consumer<Mqtt5Publish> callback) {
            this.callback = Checks.notNull(callback, "Callback");
            return this;
        }

        @Override
        public @NotNull SubscribeBuilder executor(final @Nullable Executor executor) {
            this.executor = Checks.notNull(executor, "Executor");
            return this;
        }

        @Override
        public @NotNull SubscribeBuilder manualAcknowledgement(final boolean manualAcknowledgement) {
            this.manualAcknowledgement = manualAcknowledgement;
            return this;
        }

        @Override
        public @NotNull CompletableFuture<Mqtt5SubAck> send() {
            final Mqtt5Subscribe subscribe = build();
            if (callback == null) {
                Checks.state(executor == null, "Executor must not be given if callback is null.");
                Checks.state(!manualAcknowledgement, "Manual acknowledgement must not be true if callback is null.");
                return subscribe(subscribe);
            }
            if (executor == null) {
                return subscribe(subscribe, callback, manualAcknowledgement);
            }
            return subscribe(subscribe, callback, executor, manualAcknowledgement);
        }
    }
}
