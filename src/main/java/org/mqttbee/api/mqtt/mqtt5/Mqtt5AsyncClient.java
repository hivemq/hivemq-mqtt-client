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

package org.mqttbee.api.mqtt.mqtt5;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.*;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.util.FluentBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5AsyncClient extends Mqtt5Client {

    @NotNull CompletableFuture<@NotNull Mqtt5ConnAck> connect(@NotNull Mqtt5Connect connect);

    default @NotNull Mqtt5ConnectBuilder<CompletableFuture<Mqtt5ConnAck>> connect() {
        return new Mqtt5ConnectBuilder<>(this::connect);
    }

    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(@NotNull Mqtt5Subscribe subscribe);

    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            @NotNull Mqtt5Subscribe subscribe, @NotNull Consumer<@NotNull Mqtt5Publish> callback);

    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            @NotNull Mqtt5Subscribe subscribe, @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            @NotNull Executor executor);

    default @NotNull Mqtt5SubscribeAndCallbackBuilder<CompletableFuture<Mqtt5SubAck>> subscribe() {
        return new Mqtt5SubscribeAndCallbackBuilder<>(subscribeAndCallback -> {
            final Mqtt5Subscribe subscribe = subscribeAndCallback.getSubscribe();
            final Consumer<Mqtt5Publish> callback = subscribeAndCallback.getCallback();
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

    void publishes(@NotNull MqttGlobalPublishFilter filter, @NotNull Consumer<@NotNull Mqtt5Publish> callback);

    void publishes(
            @NotNull MqttGlobalPublishFilter filter, @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            @NotNull Executor executor);

    @NotNull CompletableFuture<@NotNull Mqtt5UnsubAck> unsubscribe(@NotNull Mqtt5Unsubscribe unsubscribe);

    default @NotNull Mqtt5UnsubscribeBuilder<CompletableFuture<Mqtt5UnsubAck>> unsubscribe() {
        return new Mqtt5UnsubscribeBuilder<>(this::unsubscribe);
    }

    @NotNull CompletableFuture<@NotNull Mqtt5PublishResult> publish(@NotNull Mqtt5Publish publish);

    default @NotNull Mqtt5PublishBuilder<CompletableFuture<Mqtt5PublishResult>> publish() {
        return new Mqtt5PublishBuilder<>(this::publish);
    }

    @NotNull CompletableFuture<Void> reauth();

    @NotNull CompletableFuture<Void> disconnect(@NotNull Mqtt5Disconnect disconnect);

    default @NotNull Mqtt5DisconnectBuilder<CompletableFuture<Void>> disconnect() {
        return new Mqtt5DisconnectBuilder<>(this::disconnect);
    }

    @Override
    default @NotNull Mqtt5AsyncClient toAsync() {
        return this;
    }

    class Mqtt5SubscribeAndCallback {

        private final @NotNull Mqtt5Subscribe subscribe;
        private final @Nullable Consumer<Mqtt5Publish> callback;
        private final @Nullable Executor executor;

        Mqtt5SubscribeAndCallback(
                final @NotNull Mqtt5Subscribe subscribe, final @Nullable Consumer<Mqtt5Publish> callback,
                final @Nullable Executor executor) {

            this.subscribe = subscribe;
            this.callback = callback;
            this.executor = executor;
        }

        public @NotNull Mqtt5Subscribe getSubscribe() {
            return subscribe;
        }

        public @Nullable Consumer<Mqtt5Publish> getCallback() {
            return callback;
        }

        public @Nullable Executor getExecutor() {
            return executor;
        }
    }

    class Mqtt5SubscribeAndCallbackBuilder<P> extends FluentBuilder<Mqtt5SubscribeAndCallback, P> {

        private final @NotNull Mqtt5SubscribeBuilder<Void> subscribeBuilder = Mqtt5Subscribe.builder();
        private @Nullable Consumer<Mqtt5Publish> callback;
        private @Nullable Executor executor;

        Mqtt5SubscribeAndCallbackBuilder(
                final @Nullable Function<? super Mqtt5SubscribeAndCallback, P> parentConsumer) {

            super(parentConsumer);
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> topicFilter(final @NotNull String topicFilter) {
            subscribeBuilder.topicFilter(topicFilter);
            return this;
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> topicFilter(final @NotNull MqttTopicFilter topicFilter) {
            subscribeBuilder.topicFilter(topicFilter);
            return this;
        }

        public @NotNull MqttTopicFilterBuilder<? extends Mqtt5SubscribeAndCallbackBuilder<P>> topicFilter() {
            return new MqttTopicFilterBuilder<>("", this::topicFilter);
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> qos(final @NotNull MqttQos qos) {
            subscribeBuilder.qos(qos);
            return this;
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> noLocal(final boolean noLocal) {
            subscribeBuilder.noLocal(noLocal);
            return this;
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> retainHandling(
                final @NotNull Mqtt5RetainHandling retainHandling) {

            subscribeBuilder.retainHandling(retainHandling);
            return this;
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> retainAsPublished(final boolean retainAsPublished) {
            subscribeBuilder.retainAsPublished(retainAsPublished);
            return this;
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> addSubscription(
                final @NotNull Mqtt5Subscription subscription) {

            subscribeBuilder.addSubscription(subscription);
            return this;
        }

        public @NotNull Mqtt5SubscriptionBuilder<? extends Mqtt5SubscribeAndCallbackBuilder<P>> addSubscription() {
            return new Mqtt5SubscriptionBuilder<>(this::addSubscription);
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> userProperties(
                final @NotNull Mqtt5UserProperties userProperties) {

            subscribeBuilder.userProperties(userProperties);
            return this;
        }

        public @NotNull Mqtt5UserPropertiesBuilder<? extends Mqtt5SubscribeAndCallbackBuilder<P>> userProperties() {
            return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> callback(final @Nullable Consumer<Mqtt5Publish> callback) {
            this.callback = callback;
            return this;
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> executor(final @Nullable Executor executor) {
            this.executor = executor;
            return this;
        }

        @Override
        public @NotNull Mqtt5SubscribeAndCallback build() {
            if ((callback == null) && (executor != null)) {
                throw new IllegalStateException("Executor must not be given if callback is null");
            }
            return new Mqtt5SubscribeAndCallback(subscribeBuilder.build(), callback, executor);
        }
    }
}
