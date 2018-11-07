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

package org.mqttbee.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3AsyncClient;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3BlockingClient;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3ClientData;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3RxClient;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt.MqttAsyncClient;
import org.mqttbee.mqtt.message.connect.connack.mqtt3.Mqtt3ConnAckView;
import org.mqttbee.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import org.mqttbee.mqtt.message.disconnect.mqtt3.Mqtt3DisconnectView;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeView;
import org.mqttbee.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeViewBuilder;
import org.mqttbee.mqtt.message.subscribe.suback.mqtt3.Mqtt3SubAckView;
import org.mqttbee.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeView;
import org.mqttbee.mqtt.mqtt3.exceptions.Mqtt3ExceptionFactory;
import org.mqttbee.util.Checks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt3AsyncClientView implements Mqtt3AsyncClient {

    private static final @NotNull BiFunction<Mqtt5ConnAck, Throwable, Mqtt3ConnAck> CONNACK_MAPPER =
            (connAck, throwable) -> {
                if (throwable != null) {
                    throw new CompletionException(Mqtt3ExceptionFactory.map(throwable));
                }
                return Mqtt3ConnAckView.of(connAck);
            };

    private static final @NotNull BiFunction<Mqtt5SubAck, Throwable, Mqtt3SubAck> SUBACK_MAPPER =
            (subAck, throwable) -> {
                if (throwable != null) {
                    throw new CompletionException(Mqtt3ExceptionFactory.map(throwable));
                }
                return Mqtt3SubAckView.of(subAck);
            };

    private static final @NotNull BiFunction<Mqtt5UnsubAck, Throwable, Void> UNSUBACK_MAPPER =
            (unsubAck, throwable) -> {
                if (throwable != null) {
                    throw new CompletionException(Mqtt3ExceptionFactory.map(throwable));
                }
                return null;
            };

    private static final @NotNull BiFunction<Mqtt5PublishResult, Throwable, Mqtt3Publish> PUBLISH_RESULT_MAPPER =
            (publishResult, throwable) -> {
                if (throwable != null) {
                    throw new CompletionException(Mqtt3ExceptionFactory.map(throwable));
                }
                return Mqtt3PublishView.of(publishResult.getPublish());
            };

    private static final @NotNull Function<Throwable, Void> DISCONNECT_MAPPER = throwable -> {
        throw new CompletionException(Mqtt3ExceptionFactory.map(throwable));
    };

    private static @NotNull Consumer<Mqtt5Publish> callbackView(final @NotNull Consumer<Mqtt3Publish> callback) {
        return publish -> callback.accept(Mqtt3PublishView.of(publish));
    }

    private final @NotNull MqttAsyncClient delegate;
    private final @NotNull Mqtt3ClientDataView clientData;

    public Mqtt3AsyncClientView(final @NotNull MqttAsyncClient delegate) {
        this.delegate = delegate;
        clientData = new Mqtt3ClientDataView(delegate.getClientData());
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3ConnAck> connect(final @NotNull Mqtt3Connect connect) {
        return delegate.connect(Mqtt3ConnectView.delegate(connect)).handle(CONNACK_MAPPER);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(final @NotNull Mqtt3Subscribe subscribe) {
        return delegate.subscribe(Mqtt3SubscribeView.delegate(subscribe)).handle(SUBACK_MAPPER);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            final @NotNull Mqtt3Subscribe subscribe, final @NotNull Consumer<@NotNull Mqtt3Publish> callback) {

        Checks.notNull(callback, "Callback");

        return delegate.subscribe(Mqtt3SubscribeView.delegate(subscribe), callbackView(callback)).handle(SUBACK_MAPPER);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            final @NotNull Mqtt3Subscribe subscribe, final @NotNull Consumer<@NotNull Mqtt3Publish> callback,
            final @NotNull Executor executor) {

        Checks.notNull(callback, "Callback");
        Checks.notNull(executor, "Executor");

        return delegate.subscribe(Mqtt3SubscribeView.delegate(subscribe), callbackView(callback), executor)
                .handle(SUBACK_MAPPER);
    }

    @Override
    public void publishes(
            final @NotNull MqttGlobalPublishFilter filter, final @NotNull Consumer<@NotNull Mqtt3Publish> callback) {

        Checks.notNull(callback, "Callback");

        delegate.publishes(filter, callbackView(callback));
    }

    @Override
    public void publishes(
            final @NotNull MqttGlobalPublishFilter filter, final @NotNull Consumer<@NotNull Mqtt3Publish> callback,
            final @NotNull Executor executor) {

        Checks.notNull(callback, "Callback");
        Checks.notNull(executor, "Executor");

        delegate.publishes(filter, callbackView(callback), executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> unsubscribe(final @NotNull Mqtt3Unsubscribe unsubscribe) {
        return delegate.unsubscribe(Mqtt3UnsubscribeView.delegate(unsubscribe)).handle(UNSUBACK_MAPPER);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt3Publish> publish(final @NotNull Mqtt3Publish publish) {
        return delegate.publish(Mqtt3PublishView.delegate(publish)).handle(PUBLISH_RESULT_MAPPER);
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnect() {
        return delegate.disconnect(Mqtt3DisconnectView.delegate()).exceptionally(DISCONNECT_MAPPER);
    }

    @Override
    public @NotNull Mqtt3ClientData getClientData() {
        return clientData;
    }

    @Override
    public @NotNull Mqtt3RxClient toRx() {
        return new Mqtt3RxClientView(delegate.toRx());
    }

    @Override
    public @NotNull Mqtt3BlockingClient toBlocking() {
        return new Mqtt3BlockingClientView(delegate.toBlocking());
    }

    // @formatter:off
    public static class Mqtt3SubscribeViewAndCallbackBuilder
            extends Mqtt3SubscribeViewBuilder<Mqtt3SubscribeViewAndCallbackBuilder>
            implements Mqtt3SubscribeAndCallbackBuilder.Complete,
                       Mqtt3SubscribeAndCallbackBuilder.Start.Complete,
                       Mqtt3SubscribeAndCallbackBuilder.Call.Ex {
    // @formatter:on

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
        public @NotNull Mqtt3SubscribeViewAndCallbackBuilder callback(
                final @NotNull Consumer<Mqtt3Publish> callback) {

            this.callback = Checks.notNull(callback, "Callback");
            return this;
        }

        @Override
        public @NotNull Mqtt3SubscribeViewAndCallbackBuilder executor(final @NotNull Executor executor) {
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
