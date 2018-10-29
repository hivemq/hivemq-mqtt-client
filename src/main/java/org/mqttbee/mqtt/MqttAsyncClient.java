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
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5AsyncClient;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.rx.RxJavaFutureConverter;
import org.reactivestreams.Subscription;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class MqttAsyncClient implements Mqtt5AsyncClient {

    private static final @NotNull Function<Mqtt5SubAck, Mqtt5SubAck> SUBACK_HANDLER = MqttBlockingClient::handleSubAck;
    private static final @NotNull Function<Mqtt5UnsubAck, Mqtt5UnsubAck> UNSUBACK_HANDLER =
            MqttBlockingClient::handleUnsubAck;
    private static final @NotNull Function<Mqtt5PublishResult, Mqtt5PublishResult> PUBLISH_HANDLER =
            MqttBlockingClient::handlePublish;

    private final @NotNull MqttRxClient delegate;

    MqttAsyncClient(final @NotNull MqttRxClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5ConnAck> connect(final @NotNull Mqtt5Connect connect) {
        return RxJavaFutureConverter.toFuture(delegate.connect(connect));
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(final @NotNull Mqtt5Subscribe subscribe) {
        return RxJavaFutureConverter.toFuture(delegate.subscribe(subscribe));
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            final @NotNull Mqtt5Subscribe subscribe, final @NotNull Consumer<@NotNull Mqtt5Publish> callback) {

        Objects.requireNonNull(callback, "Callback must not be null.");

        return delegate.subscribeStream(subscribe)
                .subscribeSingleFuture(new CallbackSubscriber(callback))
                .thenApply(SUBACK_HANDLER);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            final @NotNull Mqtt5Subscribe subscribe, final @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            final @NotNull Executor executor) {

        Objects.requireNonNull(callback, "Callback must not be null.");
        Objects.requireNonNull(executor, "Executor must not be null.");

        return delegate.subscribeStreamUnsafe(subscribe)
                .observeOnBoth(Schedulers.from(executor))
                .subscribeSingleFuture(new CallbackSubscriber(callback))
                .thenApply(SUBACK_HANDLER);
    }

    @Override
    public void publishes(
            final @NotNull MqttGlobalPublishFilter filter, final @NotNull Consumer<@NotNull Mqtt5Publish> callback) {

        Objects.requireNonNull(callback, "Callback must not be null.");

        delegate.publishes(filter).subscribe(new CallbackSubscriber(callback));
    }

    @Override
    public void publishes(
            final @NotNull MqttGlobalPublishFilter filter, final @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            final @NotNull Executor executor) {

        Objects.requireNonNull(callback, "Callback must not be null.");
        Objects.requireNonNull(executor, "Executor must not be null.");

        delegate.publishesUnsafe(filter)
                .observeOn(Schedulers.from(executor))
                .subscribe(new CallbackSubscriber(callback));
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5UnsubAck> unsubscribe(final @NotNull Mqtt5Unsubscribe unsubscribe) {
        return RxJavaFutureConverter.toFuture(delegate.unsubscribe(unsubscribe)).thenApply(UNSUBACK_HANDLER);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Mqtt5PublishResult> publish(final @NotNull Mqtt5Publish publish) {
        return RxJavaFutureConverter.toFuture(delegate.publishHalfSafe(Flowable.just(publish)).singleOrError())
                .thenApply(PUBLISH_HANDLER);
    }

    @Override
    public @NotNull CompletableFuture<Void> reauth() {
        return RxJavaFutureConverter.toFuture(delegate.reauth());
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnect(final @NotNull Mqtt5Disconnect disconnect) {
        return RxJavaFutureConverter.toFuture(delegate.disconnect(disconnect));
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
}
