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

package com.hivemq.mqtt.client2.internal.mqtt3;

import com.hivemq.mqtt.client2.MqttGlobalPublishFilter;
import com.hivemq.mqtt.client2.internal.MqttAsyncClient;
import com.hivemq.mqtt.client2.internal.exceptions.mqtt3.Mqtt3ExceptionFactory;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnect;
import com.hivemq.mqtt.client2.internal.message.connect.mqtt3.Mqtt3ConnAckView;
import com.hivemq.mqtt.client2.internal.message.connect.mqtt3.Mqtt3ConnectView;
import com.hivemq.mqtt.client2.internal.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.mqtt.client2.internal.message.disconnect.mqtt3.Mqtt3DisconnectView;
import com.hivemq.mqtt.client2.internal.message.publish.MqttPublish;
import com.hivemq.mqtt.client2.internal.message.publish.mqtt3.Mqtt3PublishResultView;
import com.hivemq.mqtt.client2.internal.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.mqtt.client2.internal.message.publish.mqtt3.Mqtt3PublishViewBuilder;
import com.hivemq.mqtt.client2.internal.message.subscribe.MqttSubscribe;
import com.hivemq.mqtt.client2.internal.message.subscribe.mqtt3.Mqtt3SubAckView;
import com.hivemq.mqtt.client2.internal.message.subscribe.mqtt3.Mqtt3SubscribeViewBuilder;
import com.hivemq.mqtt.client2.internal.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.mqtt.client2.internal.message.unsubscribe.mqtt3.Mqtt3UnsubAckView;
import com.hivemq.mqtt.client2.internal.message.unsubscribe.mqtt3.Mqtt3UnsubscribeViewBuilder;
import com.hivemq.mqtt.client2.internal.util.Checks;
import com.hivemq.mqtt.client2.internal.util.MqttChecks;
import com.hivemq.mqtt.client2.mqtt3.Mqtt3AsyncClient;
import com.hivemq.mqtt.client2.mqtt3.Mqtt3BlockingClient;
import com.hivemq.mqtt.client2.mqtt3.Mqtt3ClientConfig;
import com.hivemq.mqtt.client2.mqtt3.Mqtt3RxClient;
import com.hivemq.mqtt.client2.mqtt3.message.connect.Mqtt3ConnAck;
import com.hivemq.mqtt.client2.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.mqtt.client2.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.mqtt.client2.mqtt3.message.publish.Mqtt3PublishResult;
import com.hivemq.mqtt.client2.mqtt3.message.subscribe.Mqtt3SubAck;
import com.hivemq.mqtt.client2.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.mqtt.client2.mqtt3.message.unsubscribe.Mqtt3UnsubAck;
import com.hivemq.mqtt.client2.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.mqtt.client2.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.mqtt.client2.mqtt5.message.subscribe.Mqtt5SubAck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
public class Mqtt3AsyncClientView implements Mqtt3AsyncClient {

    private static @NotNull CompletableFuture<@NotNull Mqtt3SubAck> handleSubAck(
            final @NotNull CompletableFuture<@NotNull Mqtt5SubAck> future) {

        final CompletableFuture<Mqtt3SubAck> mappedFuture = new CompletableFuture<>();
        future.whenComplete((subAck, throwable) -> {
            if (throwable != null) {
                mappedFuture.completeExceptionally(Mqtt3ExceptionFactory.map(throwable));
            } else {
                mappedFuture.complete(Mqtt3SubAckView.of(subAck));
            }
        });
        return mappedFuture;
    }

    private static @NotNull Consumer<Mqtt5Publish> callbackView(final @NotNull Consumer<Mqtt3Publish> callback) {
        return publish -> callback.accept(Mqtt3PublishView.of(publish));
    }

    private final @NotNull MqttAsyncClient delegate;
    private final @NotNull Mqtt3ClientConfigView clientConfig;

    Mqtt3AsyncClientView(final @NotNull MqttAsyncClient delegate) {
        this.delegate = delegate;
        clientConfig = new Mqtt3ClientConfigView(delegate.getConfig());
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3ConnAck> connect() {
        return connect(Mqtt3ConnectView.DEFAULT);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3ConnAck> connect(final @Nullable Mqtt3Connect connect) {
        final MqttConnect mqttConnect = MqttChecks.connect(connect);

        final CompletableFuture<Mqtt3ConnAck> future = new CompletableFuture<>();
        delegate.connect(mqttConnect).whenComplete((connAck, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(Mqtt3ExceptionFactory.map(throwable));
            } else {
                future.complete(Mqtt3ConnAckView.of(connAck));
            }
        });
        return future;
    }

    @Override
    public Mqtt3ConnectViewBuilder.@NotNull Send<CompletableFuture<Mqtt3ConnAck>> connectWith() {
        return new Mqtt3ConnectViewBuilder.Send<>(this::connect);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(final @Nullable Mqtt3Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);

        return handleSubAck(delegate.subscribe(mqttSubscribe));
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            final @Nullable Mqtt3Subscribe subscribe,
            final @Nullable Consumer<@NotNull Mqtt3Publish> callback) {
        return subscribe(subscribe, callback, false);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            final @Nullable Mqtt3Subscribe subscribe,
            final @Nullable Consumer<@NotNull Mqtt3Publish> callback,
            final @Nullable Executor executor) {
        return subscribe(subscribe, callback, executor, false);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            final @Nullable Mqtt3Subscribe subscribe,
            final @Nullable Consumer<@NotNull Mqtt3Publish> callback,
            final boolean manualAcknowledgement) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);
        Checks.notNull(callback, "Callback");

        return handleSubAck(delegate.subscribe(mqttSubscribe, callbackView(callback), manualAcknowledgement));
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            final @Nullable Mqtt3Subscribe subscribe,
            final @Nullable Consumer<@NotNull Mqtt3Publish> callback,
            final @Nullable Executor executor,
            final boolean manualAcknowledgement) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);
        Checks.notNull(callback, "Callback");
        Checks.notNull(executor, "Executor");

        return handleSubAck(delegate.subscribe(mqttSubscribe, callbackView(callback), executor, manualAcknowledgement));
    }

    @Override
    public @NotNull SubscribeViewBuilder subscribeWith() {
        return new SubscribeViewBuilder();
    }

    @Override
    public void publishes(
            final @Nullable MqttGlobalPublishFilter filter,
            final @Nullable Consumer<@NotNull Mqtt3Publish> callback) {
        publishes(filter, callback, false);
    }

    @Override
    public void publishes(
            final @Nullable MqttGlobalPublishFilter filter,
            final @Nullable Consumer<@NotNull Mqtt3Publish> callback,
            final @Nullable Executor executor) {
        publishes(filter, callback, executor, false);
    }

    @Override
    public void publishes(
            final @Nullable MqttGlobalPublishFilter filter,
            final @Nullable Consumer<@NotNull Mqtt3Publish> callback,
            final boolean manualAcknowledgement) {
        Checks.notNull(filter, "Global publish filter");
        Checks.notNull(callback, "Callback");

        delegate.publishes(filter, callbackView(callback), manualAcknowledgement);
    }

    @Override
    public void publishes(
            final @Nullable MqttGlobalPublishFilter filter,
            final @Nullable Consumer<@NotNull Mqtt3Publish> callback,
            final @Nullable Executor executor,
            final boolean manualAcknowledgement) {
        Checks.notNull(filter, "Global publish filter");
        Checks.notNull(callback, "Callback");
        Checks.notNull(executor, "Executor");

        delegate.publishes(filter, callbackView(callback), executor, manualAcknowledgement);
    }

    @Override
    public @NotNull CompletableFuture<Mqtt3UnsubAck> unsubscribe(final @Nullable Mqtt3Unsubscribe unsubscribe) {
        final MqttUnsubscribe mqttUnsubscribe = MqttChecks.unsubscribe(unsubscribe);

        final CompletableFuture<Mqtt3UnsubAck> future = new CompletableFuture<>();
        delegate.unsubscribe(mqttUnsubscribe).whenComplete((unsubAck, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(Mqtt3ExceptionFactory.map(throwable));
            } else {
                future.complete(Mqtt3UnsubAckView.INSTANCE);
            }
        });
        return future;
    }

    @Override
    public Mqtt3UnsubscribeViewBuilder.@NotNull Send<CompletableFuture<Mqtt3UnsubAck>> unsubscribeWith() {
        return new Mqtt3UnsubscribeViewBuilder.Send<>(this::unsubscribe);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3PublishResult> publish(final @Nullable Mqtt3Publish publish) {
        final MqttPublish mqttPublish = MqttChecks.publish(publish);

        final CompletableFuture<Mqtt3PublishResult> future = new CompletableFuture<>();
        delegate.publish(mqttPublish).whenComplete((publishResult, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(Mqtt3ExceptionFactory.map(throwable));
            } else {
                future.complete(Mqtt3PublishResultView.of(publishResult));
            }
        });
        return future;
    }

    @Override
    public Mqtt3PublishViewBuilder.@NotNull Send<CompletableFuture<Mqtt3PublishResult>> publishWith() {
        return new Mqtt3PublishViewBuilder.Send<>(this::publish);
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnect() {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        delegate.disconnect(Mqtt3DisconnectView.DELEGATE).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(Mqtt3ExceptionFactory.map(throwable));
            } else {
                future.complete(null);
            }
        });
        return future;
    }

    @Override
    public @NotNull Mqtt3ClientConfig getConfig() {
        return clientConfig;
    }

    @Override
    public @NotNull Mqtt3RxClient toRx() {
        return new Mqtt3RxClientView(delegate.toRx());
    }

    @Override
    public @NotNull Mqtt3BlockingClient toBlocking() {
        return new Mqtt3BlockingClientView(delegate.toBlocking());
    }

    public class SubscribeViewBuilder extends Mqtt3SubscribeViewBuilder<SubscribeViewBuilder>
            implements SubscribeBuilder.Start.Complete, SubscribeBuilder.AfterCallback {

        private @Nullable Consumer<Mqtt3Publish> callback;
        private @Nullable Executor executor;
        private boolean manualAcknowledgement;

        @Override
        protected @NotNull SubscribeViewBuilder self() {
            return this;
        }

        @Override
        public @NotNull SubscribeViewBuilder callback(final @Nullable Consumer<Mqtt3Publish> callback) {
            this.callback = Checks.notNull(callback, "Callback");
            return this;
        }

        @Override
        public @NotNull SubscribeViewBuilder executor(final @Nullable Executor executor) {
            this.executor = Checks.notNull(executor, "Executor");
            return this;
        }

        @Override
        public @NotNull SubscribeViewBuilder manualAcknowledgement(final boolean manualAcknowledgement) {
            this.manualAcknowledgement = manualAcknowledgement;
            return this;
        }

        @Override
        public @NotNull CompletableFuture<Mqtt3SubAck> send() {
            final Mqtt3Subscribe subscribe = build();
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
