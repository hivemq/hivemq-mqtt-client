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
 * Asynchronous API of a {@link Mqtt5Client} based on futures and callbacks.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5AsyncClient extends Mqtt5Client {

    /**
     * Connects this client with the default Connect message.
     *
     * @return see {@link #connect(Mqtt5Connect)}.
     * @see #connect(Mqtt5Connect)
     */
    default @NotNull CompletableFuture<@NotNull Mqtt5ConnAck> connect() {
        return connect(Mqtt5Connect.DEFAULT);
    }

    /**
     * Connects this client with the given Connect message.
     *
     * @param connect the Connect message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally with the ConnAck message if it does not contain an Error Code (connected
     *         successfully),</li>
     *         <li>completes exceptionally with a {@link org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException
     *         Mqtt5MessageException} wrapping the ConnAck message if it contains an Error Code or</li>
     *         <li>completes exceptionally with a different exception if an error occurred before the Connect message
     *         was sent or before the ConnAck message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt5ConnAck> connect(@NotNull Mqtt5Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt5Connect)}.
     * <p>
     * Calling {@link Mqtt5ConnectBuilder#applyConnect()} on the returned builder has the same effect as calling {@link
     * #connect(Mqtt5Connect)} with the result of {@link Mqtt5ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt5Connect)
     */
    default @NotNull Mqtt5ConnectBuilder<CompletableFuture<Mqtt5ConnAck>> connectWith() {
        return new Mqtt5ConnectBuilder<>(this::connect);
    }

    /**
     * Subscribes this client with the given Subscribe message.
     * <p>
     * See {@link #publishes(MqttGlobalPublishFilter, Consumer)} or {@link #publishes(MqttGlobalPublishFilter, Consumer,
     * Executor)} to consume the incoming Publish messages. Alternatively, call {@link #subscribe(Mqtt5Subscribe,
     * Consumer)} or {@link #subscribe(Mqtt5Subscribe, Consumer, Executor)} to consume the incoming Publish messages
     * matching the subscriptions of the Subscribe message directly.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally with the SubAck message if all subscriptions of the Subscribe message were
     *         successful (the SubAck message contains no Error Codes),</li>
     *         <li>completes exceptionally with a {@link org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException
     *         Mqtt5MessageException} wrapping the SubAck message if it contains at least one Error Code or</li>
     *         <li>completes exceptionally with a different exception if an error occurred before the Subscribe message
     *         was sent or before a SubAck message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(@NotNull Mqtt5Subscribe subscribe);

    /**
     * Subscribes this client with the given Subscribe message and consumes the the incoming Publish messages matching
     * the subscriptions of the Subscribe message with a callback.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @param callback  the callback for consuming the incoming Publish messages matching the subscriptions of the
     *                  Subscribe message.
     * @return see {@link #subscribe(Mqtt5Subscribe)}.
     * @see #subscribe(Mqtt5Subscribe, Consumer, Executor)
     */
    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            @NotNull Mqtt5Subscribe subscribe, @NotNull Consumer<@NotNull Mqtt5Publish> callback);

    /**
     * Subscribes this client with the given Subscribe message and consumes the the incoming Publish messages matching
     * the subscriptions of the Subscribe message with a callback.
     * <p>
     * The future is completed and the callback is executed on the given executor.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @param callback  the callback for consuming the incoming Publish messages matching the subscriptions of the
     *                  Subscribe message.
     * @param executor  the executor where the future is completed and the callback is executed on.
     * @return see {@link #subscribe(Mqtt5Subscribe)}.
     * @see #subscribe(Mqtt5Subscribe, Consumer)
     */
    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            @NotNull Mqtt5Subscribe subscribe, @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            @NotNull Executor executor);

    /**
     * Fluent counterpart of {@link #subscribe(Mqtt5Subscribe)}, {@link #subscribe(Mqtt5Subscribe, Consumer)} and {@link
     * #subscribe(Mqtt5Subscribe, Consumer, Executor)}.
     * <p>
     * Calling {@link Mqtt5SubscribeAndCallbackBuilder#applySubscribe()} on the returned builder has the same effect as
     * calling one of the following methods with the result of {@link Mqtt5SubscribeAndCallbackBuilder#build()}:
     * <ul>
     * <li>{@link #subscribe(Mqtt5Subscribe)} if no callback has been supplied to the builder</li>
     * <li>{@link #subscribe(Mqtt5Subscribe, Consumer)} if only a callback has been supplied to the builder</li>
     * <li>{@link #subscribe(Mqtt5Subscribe, Consumer, Executor)} if a callback and an executor has been supplied to
     * the builder</li>
     * </ul>
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribe(Mqtt5Subscribe)
     * @see #subscribe(Mqtt5Subscribe, Consumer)
     * @see #subscribe(Mqtt5Subscribe, Consumer, Executor)
     */
    default @NotNull Mqtt5SubscribeAndCallbackBuilder<CompletableFuture<Mqtt5SubAck>> subscribeWith() {
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

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter   the filter with which all incoming Publish messages are filtered.
     * @param callback the callback for all incoming Publish messages matching the given filter.
     * @see #publishes(MqttGlobalPublishFilter, Consumer, Executor)
     */
    void publishes(@NotNull MqttGlobalPublishFilter filter, @NotNull Consumer<@NotNull Mqtt5Publish> callback);

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter   the filter with which all incoming Publish messages are filtered.
     * @param callback the callback for all incoming Publish messages matching the given filter.
     * @param executor the executor where the callback is executed on.
     * @see #publishes(MqttGlobalPublishFilter, Consumer)
     */
    void publishes(
            @NotNull MqttGlobalPublishFilter filter, @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            @NotNull Executor executor);

    /**
     * Unsubscribes this client with the given Unsubscribe message.
     *
     * @param unsubscribe the Unsubscribe message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally with the UnsubAck message if all Topic Filters of the Unsubscribe message were
     *         successfully unsubscribed (the UnsubAck message contains no Error Codes),</li>
     *         <li>completes exceptionally with a {@link org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException
     *         Mqtt5MessageException} wrapping the UnsubAck message if it contains at least one Error Code or</li>
     *         <li>completes exceptionally with a different exception if an error occurred before the Unsubscribe
     *         message was sent or before a UnsubAck message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt5UnsubAck> unsubscribe(@NotNull Mqtt5Unsubscribe unsubscribe);

    /**
     * Fluent counterpart of {@link #unsubscribe(Mqtt5Unsubscribe)}.
     * <p>
     * Calling {@link Mqtt5UnsubscribeBuilder#applyUnsubscribe()} on the returned builder has the same effect as calling
     * {@link #unsubscribe(Mqtt5Unsubscribe)} with the result of {@link Mqtt5UnsubscribeBuilder#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #unsubscribe(Mqtt5Unsubscribe)
     */
    default @NotNull Mqtt5UnsubscribeBuilder<CompletableFuture<Mqtt5UnsubAck>> unsubscribeWith() {
        return new Mqtt5UnsubscribeBuilder<>(this::unsubscribe);
    }

    /**
     * Publishes the given Publish message.
     *
     * @param publish the Publish message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally with the {@link Mqtt5PublishResult} if the Publish message was successfully
     *         published (no acknowledgement message contains an Error Code, {@link Mqtt5PublishResult#getError()} will
     *         always be absent),</li>
     *         <li>completes exceptionally with a {@link org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException
     *         Mqtt5MessageException} wrapping the acknowledgement message if it contains an Error Code or</li>
     *         <li>completes exceptionally with a different exception if an error occurred before the Publish
     *         message was sent or before an acknowledgement message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt5PublishResult> publish(@NotNull Mqtt5Publish publish);

    /**
     * Fluent counterpart of {@link #publish(Mqtt5Publish)}.
     * <p>
     * Calling {@link Mqtt5PublishBuilder#applyPublish()} on the returned builder has the same effect as calling {@link
     * #publish(Mqtt5Publish)} with the result of {@link Mqtt5PublishBuilder#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #publish(Mqtt5Publish)
     */
    default @NotNull Mqtt5PublishBuilder<CompletableFuture<Mqtt5PublishResult>> publishWith() {
        return new Mqtt5PublishBuilder<>(this::publish);
    }

    /**
     * Re-authenticates this client.
     *
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally when the client was successfully re-authenticated,</li>
     *         <li>errors with a {@link org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException
     *         Mqtt5MessageException} wrapping the Auth message with the Error Code if not re-authenticated successfully
     *         or</li>
     *         <li>errors with a different exception if an error occurred before the first Auth message was sent or
     *         before the last Auth message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<Void> reauth();

    /**
     * Disconnects this client with the default Disconnect message.
     *
     * @return see {@link #disconnect(Mqtt5Disconnect)}.
     * @see #disconnect(Mqtt5Disconnect)
     */
    default @NotNull CompletableFuture<Void> disconnect() {
        return disconnect(Mqtt5Disconnect.DEFAULT);
    }

    /**
     * Disconnects this client with the given Disconnect message.
     *
     * @param disconnect the Disconnect message sent to the broker.
     * @return the {@link CompletableFuture} which
     *         <ul>
     *         <li>completes when the client was successfully disconnected or</li>
     *         <li>errors if not disconnected gracefully.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<Void> disconnect(@NotNull Mqtt5Disconnect disconnect);

    /**
     * Fluent counterpart of {@link #disconnect(Mqtt5Disconnect)}.
     * <p>
     * Calling {@link Mqtt5DisconnectBuilder#applyDisconnect()} on the returned builder has the same effect as calling
     * {@link #disconnect(Mqtt5Disconnect)} with the result of {@link Mqtt5DisconnectBuilder#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #disconnect(Mqtt5Disconnect)
     */
    default @NotNull Mqtt5DisconnectBuilder<CompletableFuture<Void>> disconnectWith() {
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

        public @NotNull MqttTopicFilterBuilder<Mqtt5SubscribeAndCallbackBuilder<P>> topicFilter() {
            return new MqttTopicFilterBuilder<>(this::topicFilter);
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

        public @NotNull Mqtt5SubscriptionBuilder<Mqtt5SubscribeAndCallbackBuilder<P>> addSubscription() {
            return new Mqtt5SubscriptionBuilder<>(this::addSubscription);
        }

        public @NotNull Mqtt5SubscribeAndCallbackBuilder<P> userProperties(
                final @NotNull Mqtt5UserProperties userProperties) {

            subscribeBuilder.userProperties(userProperties);
            return this;
        }

        public @NotNull Mqtt5UserPropertiesBuilder<Mqtt5SubscribeAndCallbackBuilder<P>> userProperties() {
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
                throw new IllegalStateException("Executor must not be given if callback is null.");
            }
            return new Mqtt5SubscribeAndCallback(subscribeBuilder.build(), callback, executor);
        }

        public @NotNull P applySubscribe() {
            return apply();
        }
    }
}
