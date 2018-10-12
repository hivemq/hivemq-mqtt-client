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

package org.mqttbee.api.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttGlobalPublishFlowType;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscriptionBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.unsuback.Mqtt3UnsubAck;
import org.mqttbee.util.FluentBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public interface Mqtt3AsyncClient extends Mqtt3Client {

    @NotNull CompletableFuture<@NotNull Mqtt3ConnAck> connect(@NotNull Mqtt3Connect connect);

    default @NotNull Mqtt3ConnectBuilder<CompletableFuture<Mqtt3ConnAck>> connect() {
        return new Mqtt3ConnectBuilder<>(this::connect);
    }

    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(@NotNull Mqtt3Subscribe subscribe);

    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            @NotNull Mqtt3Subscribe subscribe, @NotNull Consumer<@NotNull Mqtt3Publish> callback);

    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            @NotNull Mqtt3Subscribe subscribe, @NotNull Consumer<@NotNull Mqtt3Publish> callback,
            @NotNull Executor executor);

    default @NotNull Mqtt3SubscribeAndCallbackBuilder<CompletableFuture<Mqtt3SubAck>> subscribe() {
        return new Mqtt3SubscribeAndCallbackBuilder<>(subscribeAndCallback -> {
            final Mqtt3Subscribe subscribe = subscribeAndCallback.getSubscribe();
            final Consumer<Mqtt3Publish> callback = subscribeAndCallback.getCallback();
            if (callback == null) {
                return subscribe(subscribe);
            }
            final Executor executor = subscribeAndCallback.getExecutor();
            if (executor == null) {
                return subscribe(subscribe, callback);
            }
            return subscribe(subscribe, callback, executor);
        });
    }

    void publishes(@NotNull MqttGlobalPublishFlowType type, @NotNull Consumer<@NotNull Mqtt3Publish> callback);

    void publishes(
            @NotNull MqttGlobalPublishFlowType type, @NotNull Consumer<@NotNull Mqtt3Publish> callback,
            @NotNull Executor executor);

    @NotNull CompletableFuture<@NotNull Mqtt3UnsubAck> unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

    default @NotNull Mqtt3UnsubscribeBuilder<CompletableFuture<Mqtt3UnsubAck>> unsubscribe() {
        return new Mqtt3UnsubscribeBuilder<>(this::unsubscribe);
    }

    @NotNull CompletableFuture<@NotNull Mqtt3PublishResult> publish(@NotNull Mqtt3Publish publish);

    default @NotNull Mqtt3PublishBuilder<CompletableFuture<Mqtt3PublishResult>> publish() {
        return new Mqtt3PublishBuilder<>(this::publish);
    }

    @NotNull CompletableFuture<Void> disconnect();

    class Mqtt3SubscribeAndCallback {

        private final @NotNull Mqtt3Subscribe subscribe;
        private final @Nullable Consumer<Mqtt3Publish> callback;
        private final @Nullable Executor executor;

        Mqtt3SubscribeAndCallback(
                final @NotNull Mqtt3Subscribe subscribe, final @Nullable Consumer<Mqtt3Publish> callback,
                final @Nullable Executor executor) {

            this.subscribe = subscribe;
            this.callback = callback;
            this.executor = executor;
        }

        public @NotNull Mqtt3Subscribe getSubscribe() {
            return subscribe;
        }

        public @Nullable Consumer<Mqtt3Publish> getCallback() {
            return callback;
        }

        public @Nullable Executor getExecutor() {
            return executor;
        }
    }

    class Mqtt3SubscribeAndCallbackBuilder<P> extends FluentBuilder<Mqtt3SubscribeAndCallback, P> {

        private final @NotNull Mqtt3SubscribeBuilder<Void> subscribeBuilder = Mqtt3Subscribe.builder();
        private @Nullable Consumer<Mqtt3Publish> callback;
        private @Nullable Executor executor;

        Mqtt3SubscribeAndCallbackBuilder(
                final @Nullable Function<? super Mqtt3SubscribeAndCallback, P> parentConsumer) {

            super(parentConsumer);
        }

        public @NotNull Mqtt3SubscribeAndCallbackBuilder<P> addSubscription(
                final @NotNull Mqtt3Subscription subscription) {

            subscribeBuilder.addSubscription(subscription);
            return this;
        }

        public @NotNull Mqtt3SubscriptionBuilder<? extends Mqtt3SubscribeAndCallbackBuilder<P>> addSubscription() {
            return new Mqtt3SubscriptionBuilder<>(this::addSubscription);
        }

        public @NotNull Mqtt3SubscribeAndCallbackBuilder<P> callback(final @Nullable Consumer<Mqtt3Publish> callback) {
            this.callback = callback;
            return this;
        }

        public @NotNull Mqtt3SubscribeAndCallbackBuilder<P> executor(final @Nullable Executor executor) {
            this.executor = executor;
            return this;
        }

        @Override
        public @NotNull Mqtt3SubscribeAndCallback build() {
            if ((callback == null) && (executor != null)) {
                throw new IllegalStateException("Executor must not be given if callback is null");
            }
            return new Mqtt3SubscribeAndCallback(subscribeBuilder.build(), callback, executor);
        }
    }
}
