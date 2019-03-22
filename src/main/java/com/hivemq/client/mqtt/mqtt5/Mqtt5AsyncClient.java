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

package com.hivemq.client.mqtt.mqtt5;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.mqtt.MqttAsyncClient;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectBuilder;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnectBuilder;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishBuilder;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribeBuilder;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilderBase;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Asynchronous API of a {@link Mqtt5Client} based on futures and callbacks.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5AsyncClient extends Mqtt5Client {

    /**
     * Connects this client with the default Connect message.
     *
     * @return see {@link #connect(Mqtt5Connect)}.
     * @see #connect(Mqtt5Connect)
     */
    default @NotNull CompletableFuture<@NotNull Mqtt5ConnAck> connect() {
        return connect(MqttConnect.DEFAULT);
    }

    /**
     * Connects this client with the given Connect message.
     *
     * @param connect the Connect message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally with the ConnAck message if it does not contain an Error Code (connected
     *         successfully),</li>
     *         <li>completes exceptionally with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
     *         Mqtt5ConnAckException} wrapping the ConnAck message if it contains an Error Code or</li>
     *         <li>completes exceptionally with a different exception if an error occurred before the Connect message
     *         was sent or before the ConnAck message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt5ConnAck> connect(@NotNull Mqtt5Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt5Connect)}.
     * <p>
     * Calling {@link Mqtt5ConnectBuilder.Send#send()} on the returned builder has the same effect as calling {@link
     * #connect(Mqtt5Connect)} with the result of {@link Mqtt5ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt5Connect)
     */
    default @NotNull Mqtt5ConnectBuilder.Send<CompletableFuture<Mqtt5ConnAck>> connectWith() {
        return new MqttConnectBuilder.Send<>(this::connect);
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
     *         <li>completes exceptionally with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException
     *         Mqtt5SubAckException} wrapping the SubAck message if it contains at least one Error Code or</li>
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
     * Calling {@link Mqtt5SubscribeAndCallbackBuilder.Complete#send()} on the returned builder has the same effect as
     * calling one of the following methods:
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
    default @NotNull Mqtt5SubscribeAndCallbackBuilder.Start subscribeWith() {
        return new MqttAsyncClient.MqttSubscribeAndCallbackBuilder(this);
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
     *         <li>completes exceptionally with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5UnsubAckException
     *         Mqtt5UnsubAckException} wrapping the UnsubAck message if it contains at least one Error Code or</li>
     *         <li>completes exceptionally with a different exception if an error occurred before the Unsubscribe
     *         message was sent or before a UnsubAck message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt5UnsubAck> unsubscribe(@NotNull Mqtt5Unsubscribe unsubscribe);

    /**
     * Fluent counterpart of {@link #unsubscribe(Mqtt5Unsubscribe)}.
     * <p>
     * Calling {@link Mqtt5UnsubscribeBuilder.Send.Complete#send()} on the returned builder has the same effect as
     * calling {@link #unsubscribe(Mqtt5Unsubscribe)} with the result of {@link Mqtt5UnsubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #unsubscribe(Mqtt5Unsubscribe)
     */
    default @NotNull Mqtt5UnsubscribeBuilder.Send.Start<CompletableFuture<Mqtt5UnsubAck>> unsubscribeWith() {
        return new MqttUnsubscribeBuilder.Send<>(this::unsubscribe);
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
     *         <li>completes exceptionally with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubAckException
     *         Mqtt5PubAckException} or {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubRecException
     *         Mqtt5PubRecException} wrapping the acknowledgement message if it contains an Error Code or</li>
     *         <li>completes exceptionally with a different exception if an error occurred before the Publish message
     *         was sent or before an acknowledgement message was received.</li>
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt5PublishResult> publish(@NotNull Mqtt5Publish publish);

    /**
     * Fluent counterpart of {@link #publish(Mqtt5Publish)}.
     * <p>
     * Calling {@link Mqtt5PublishBuilder.Send.Complete#send()} on the returned builder has the same effect as calling
     * {@link #publish(Mqtt5Publish)} with the result of {@link Mqtt5PublishBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #publish(Mqtt5Publish)
     */
    default @NotNull Mqtt5PublishBuilder.Send<CompletableFuture<Mqtt5PublishResult>> publishWith() {
        return new MqttPublishBuilder.Send<>(this::publish);
    }

    /**
     * Re-authenticates this client.
     *
     * @return a {@link CompletableFuture} which
     *         <ul>
     *         <li>completes normally when the client was successfully re-authenticated,</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5AuthException
     *         Mqtt5AuthException} wrapping the Auth message with the Error Code if not re-authenticated successfully
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
        return disconnect(MqttDisconnect.DEFAULT);
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
     * Calling {@link Mqtt5DisconnectBuilder.Send#send()} on the returned builder has the same effect as calling {@link
     * #disconnect(Mqtt5Disconnect)} with the result of {@link Mqtt5DisconnectBuilder#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #disconnect(Mqtt5Disconnect)
     */
    default @NotNull Mqtt5DisconnectBuilder.Send<CompletableFuture<Void>> disconnectWith() {
        return new MqttDisconnectBuilder.Send<>(this::disconnect);
    }

    @Override
    default @NotNull Mqtt5AsyncClient toAsync() {
        return this;
    }

    // @formatter:off
    @DoNotImplement
    interface Mqtt5SubscribeAndCallbackBuilder extends
            Mqtt5SubscribeBuilderBase<Mqtt5SubscribeAndCallbackBuilder.Complete> {
    // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Complete extends
                Mqtt5SubscribeAndCallbackBuilder, Mqtt5SubscribeAndCallbackBuilder.Call,
                Mqtt5SubscribeBuilderBase.Complete<Mqtt5SubscribeAndCallbackBuilder.Complete> {}
        // @formatter:on

        // @formatter:off
        @DoNotImplement
        interface Start extends
                Mqtt5SubscribeAndCallbackBuilder,
                Mqtt5SubscribeBuilderBase.Start<
                        Mqtt5SubscribeAndCallbackBuilder.Complete, Mqtt5SubscribeAndCallbackBuilder.Start.Complete> {
        // @formatter:on

            // @formatter:off
            @DoNotImplement
            interface Complete extends
                    Mqtt5SubscribeAndCallbackBuilder.Start, Mqtt5SubscribeAndCallbackBuilder.Complete,
                    Mqtt5SubscribeBuilderBase.Start.Complete<
                            Mqtt5SubscribeAndCallbackBuilder.Complete,
                            Mqtt5SubscribeAndCallbackBuilder.Start.Complete> {}
            // @formatter:on
        }

        @DoNotImplement
        interface Call {

            @NotNull Ex callback(@NotNull Consumer<Mqtt5Publish> callback);

            @NotNull CompletableFuture<Mqtt5SubAck> send();

            @DoNotImplement
            interface Ex extends Call {

                @NotNull Ex executor(@NotNull Executor executor);
            }
        }
    }
}
