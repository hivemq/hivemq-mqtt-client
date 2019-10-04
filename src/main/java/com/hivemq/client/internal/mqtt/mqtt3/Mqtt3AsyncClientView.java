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

package com.hivemq.client.internal.mqtt.mqtt3;

import com.hivemq.client.internal.mqtt.MqttAsyncClient;
import com.hivemq.client.internal.mqtt.exceptions.mqtt3.Mqtt3ExceptionFactory;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import com.hivemq.client.internal.mqtt.message.disconnect.mqtt3.Mqtt3DisconnectView;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeViewBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3RxClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
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
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(final @Nullable Mqtt3Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);

        return handleSubAck(delegate.subscribe(mqttSubscribe));
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            final @Nullable Mqtt3Subscribe subscribe, final @Nullable Consumer<@NotNull Mqtt3Publish> callback) {

        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);
        Checks.notNull(callback, "Callback");

        return handleSubAck(delegate.subscribe(mqttSubscribe, callbackView(callback)));
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            final @Nullable Mqtt3Subscribe subscribe, final @Nullable Consumer<@NotNull Mqtt3Publish> callback,
            final @Nullable Executor executor) {

        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);
        Checks.notNull(callback, "Callback");
        Checks.notNull(executor, "Executor");

        return handleSubAck(delegate.subscribe(mqttSubscribe, callbackView(callback), executor));
    }

    @Override
    public void publishes(
            final @Nullable MqttGlobalPublishFilter filter, final @Nullable Consumer<@NotNull Mqtt3Publish> callback) {

        Checks.notNull(filter, "Global publish filter");
        Checks.notNull(callback, "Callback");

        delegate.publishes(filter, callbackView(callback));
    }

    @Override
    public void publishes(
            final @Nullable MqttGlobalPublishFilter filter, final @Nullable Consumer<@NotNull Mqtt3Publish> callback,
            final @Nullable Executor executor) {

        Checks.notNull(filter, "Global publish filter");
        Checks.notNull(callback, "Callback");
        Checks.notNull(executor, "Executor");

        delegate.publishes(filter, callbackView(callback), executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> unsubscribe(final @Nullable Mqtt3Unsubscribe unsubscribe) {
        final MqttUnsubscribe mqttUnsubscribe = MqttChecks.unsubscribe(unsubscribe);

        final CompletableFuture<Void> future = new CompletableFuture<>();
        delegate.unsubscribe(mqttUnsubscribe).whenComplete((unsubAck, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(Mqtt3ExceptionFactory.map(throwable));
            } else {
                future.complete(null);
            }
        });
        return future;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3Publish> publish(final @Nullable Mqtt3Publish publish) {
        final MqttPublish mqttPublish = MqttChecks.publish(publish);

        final CompletableFuture<Mqtt3Publish> future = new CompletableFuture<>();
        delegate.publish(mqttPublish).whenComplete((publishResult, throwable) -> {
            if (throwable != null) {
                future.completeExceptionally(Mqtt3ExceptionFactory.map(throwable));
            } else {
                future.complete(Mqtt3PublishView.of(publishResult.getPublish()));
            }
        });
        return future;
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

    public static class Mqtt3SubscribeViewAndCallbackBuilder
            extends Mqtt3SubscribeViewBuilder<Mqtt3SubscribeViewAndCallbackBuilder>
            implements Mqtt3SubscribeAndCallbackBuilder.Start.Complete, Mqtt3SubscribeAndCallbackBuilder.Call.Ex {

        private final @NotNull Mqtt3AsyncClient client;
        private @Nullable Consumer<Mqtt3Publish> callback;
        private @Nullable Executor executor;

        public Mqtt3SubscribeViewAndCallbackBuilder(final @NotNull Mqtt3AsyncClient client) {
            this.client = client;
        }

        @Override
        protected @NotNull Mqtt3SubscribeViewAndCallbackBuilder self() {
            return this;
        }

        @Override
        public @NotNull Mqtt3SubscribeViewAndCallbackBuilder callback(final @Nullable Consumer<Mqtt3Publish> callback) {
            this.callback = Checks.notNull(callback, "Callback");
            return this;
        }

        @Override
        public @NotNull Mqtt3SubscribeViewAndCallbackBuilder executor(final @Nullable Executor executor) {
            this.executor = Checks.notNull(executor, "Executor");
            return this;
        }

        @Override
        public @NotNull CompletableFuture<Mqtt3SubAck> send() {
            final Mqtt3Subscribe subscribe = build();
            if (callback == null) {
                if (executor != null) {
                    throw new IllegalStateException("Executor must not be given if callback is null.");
                }
                return client.subscribe(subscribe);
            }
            if (executor == null) {
                return client.subscribe(subscribe, callback);
            }
            return client.subscribe(subscribe, callback, executor);
        }
    }
}
