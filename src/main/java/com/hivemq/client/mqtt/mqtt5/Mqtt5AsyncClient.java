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

package com.hivemq.client.mqtt.mqtt5;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilderBase;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubscribeBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Asynchronous API of an {@link Mqtt5Client} based on futures and callbacks.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5AsyncClient extends Mqtt5Client {

    /**
     * Connects this client with the default Connect message.
     *
     * @return see {@link #connect(Mqtt5Connect)}.
     * @see #connect(Mqtt5Connect)
     */
    @NotNull CompletableFuture<@NotNull Mqtt5ConnAck> connect();

    /**
     * Connects this client with the given Connect message.
     *
     * @param connect the Connect message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *           <li>completes normally with the ConnAck message if it does not contain an Error Code (connected
     *             successfully),
     *           <li>completes exceptionally with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
     *             Mqtt5ConnAckException} wrapping the ConnAck message if it contains an Error Code or
     *           <li>completes exceptionally with a different exception if an error occurred before the Connect message
     *             was sent or before the ConnAck message was received.
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
    @CheckReturnValue
    Mqtt5ConnectBuilder.@NotNull Send<CompletableFuture<Mqtt5ConnAck>> connectWith();

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
     *           <li>completes normally with the SubAck message if all subscriptions of the Subscribe message were
     *             successful (the SubAck message contains no Error Codes),
     *           <li>completes exceptionally with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException
     *             Mqtt5SubAckException} wrapping the SubAck message if it contains at least one Error Code or
     *           <li>completes exceptionally with a different exception if an error occurred before the Subscribe
     *             message was sent or before a SubAck message was received.
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(@NotNull Mqtt5Subscribe subscribe);

    /**
     * Subscribes this client with the given Subscribe message and consumes the incoming Publish messages matching the
     * subscriptions of the Subscribe message with a callback.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @param callback  the callback for consuming the incoming Publish messages matching the subscriptions of the
     *                  Subscribe message.
     * @return see {@link #subscribe(Mqtt5Subscribe)}.
     * @see #subscribe(Mqtt5Subscribe, Consumer, Executor)
     * @see #subscribe(Mqtt5Subscribe, Consumer, boolean)
     * @see #subscribe(Mqtt5Subscribe, Consumer, Executor, boolean)
     */
    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            @NotNull Mqtt5Subscribe subscribe, @NotNull Consumer<@NotNull Mqtt5Publish> callback);

    /**
     * Subscribes this client with the given Subscribe message and consumes the incoming Publish messages matching the
     * subscriptions of the Subscribe message with a callback.
     * <p>
     * The future is completed and the callback is executed on the given executor.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @param callback  the callback for consuming the incoming Publish messages matching the subscriptions of the
     *                  Subscribe message.
     * @param executor  the executor where the future is completed and the callback is executed on.
     * @return see {@link #subscribe(Mqtt5Subscribe)}.
     * @see #subscribe(Mqtt5Subscribe, Consumer)
     * @see #subscribe(Mqtt5Subscribe, Consumer, boolean)
     * @see #subscribe(Mqtt5Subscribe, Consumer, Executor, boolean)
     */
    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            @NotNull Mqtt5Subscribe subscribe,
            @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            @NotNull Executor executor);

    /**
     * Subscribes this client with the given Subscribe message and consumes the incoming Publish messages matching the
     * subscriptions of the Subscribe message with a callback.
     *
     * @param subscribe             the Subscribe messages sent to the broker.
     * @param callback              the callback for consuming the incoming Publish messages matching the subscriptions
     *                              of the Subscribe message.
     * @param manualAcknowledgement whether the Publish messages are acknowledged manually.
     * @return see {@link #subscribe(Mqtt5Subscribe)}.
     * @see #subscribe(Mqtt5Subscribe, Consumer)
     * @see #subscribe(Mqtt5Subscribe, Consumer, Executor)
     * @see #subscribe(Mqtt5Subscribe, Consumer, Executor, boolean)
     * @since 1.2
     */
    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            @NotNull Mqtt5Subscribe subscribe,
            @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            boolean manualAcknowledgement);

    /**
     * Subscribes this client with the given Subscribe message and consumes the incoming Publish messages matching the
     * subscriptions of the Subscribe message with a callback.
     * <p>
     * The future is completed and the callback is executed on the given executor.
     *
     * @param subscribe             the Subscribe messages sent to the broker.
     * @param callback              the callback for consuming the incoming Publish messages matching the subscriptions
     *                              of the Subscribe message.
     * @param executor              the executor where the future is completed and the callback is executed on.
     * @param manualAcknowledgement whether the Publish messages are acknowledged manually.
     * @return see {@link #subscribe(Mqtt5Subscribe)}.
     * @see #subscribe(Mqtt5Subscribe, Consumer)
     * @see #subscribe(Mqtt5Subscribe, Consumer, Executor)
     * @see #subscribe(Mqtt5Subscribe, Consumer, boolean)
     * @since 1.2
     */
    @NotNull CompletableFuture<@NotNull Mqtt5SubAck> subscribe(
            @NotNull Mqtt5Subscribe subscribe,
            @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            @NotNull Executor executor,
            final boolean manualAcknowledgement);

    /**
     * Fluent counterpart of {@link #subscribe(Mqtt5Subscribe)}, {@link #subscribe(Mqtt5Subscribe, Consumer, boolean)}
     * and {@link #subscribe(Mqtt5Subscribe, Consumer, Executor, boolean)}.
     * <p>
     * Calling {@link Mqtt5SubscribeAndCallbackBuilder.Complete#send()} on the returned builder has the same effect as
     * calling one of the following methods:
     * <ul>
     *   <li>{@link #subscribe(Mqtt5Subscribe)} if no callback has been supplied to the builder
     *   <li>{@link #subscribe(Mqtt5Subscribe, Consumer)} if only a callback has been supplied to the builder
     *   <li>{@link #subscribe(Mqtt5Subscribe, Consumer, Executor)} if a callback and an executor has been supplied to
     *     the builder
     * </ul>
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribe(Mqtt5Subscribe)
     * @see #subscribe(Mqtt5Subscribe, Consumer, boolean)
     * @see #subscribe(Mqtt5Subscribe, Consumer, Executor, boolean)
     */
    @CheckReturnValue
    Mqtt5SubscribeAndCallbackBuilder.@NotNull Start subscribeWith();

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter   the filter with which all incoming Publish messages are filtered.
     * @param callback the callback for all incoming Publish messages matching the given filter.
     * @see #publishes(MqttGlobalPublishFilter, Consumer, Executor)
     * @see #publishes(MqttGlobalPublishFilter, Consumer, boolean)
     * @see #publishes(MqttGlobalPublishFilter, Consumer, Executor, boolean)
     */
    void publishes(@NotNull MqttGlobalPublishFilter filter, @NotNull Consumer<@NotNull Mqtt5Publish> callback);

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter   the filter with which all incoming Publish messages are filtered.
     * @param callback the callback for all incoming Publish messages matching the given filter.
     * @param executor the executor where the callback is executed on.
     * @see #publishes(MqttGlobalPublishFilter, Consumer)
     * @see #publishes(MqttGlobalPublishFilter, Consumer, boolean)
     * @see #publishes(MqttGlobalPublishFilter, Consumer, Executor, boolean)
     */
    void publishes(
            @NotNull MqttGlobalPublishFilter filter,
            @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            @NotNull Executor executor);

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter                the filter with which all incoming Publish messages are filtered.
     * @param callback              the callback for all incoming Publish messages matching the given filter.
     * @param manualAcknowledgement whether the Publish messages are acknowledged manually.
     * @see #publishes(MqttGlobalPublishFilter, Consumer)
     * @see #publishes(MqttGlobalPublishFilter, Consumer, Executor)
     * @see #publishes(MqttGlobalPublishFilter, Consumer, Executor, boolean)
     * @since 1.2
     */
    void publishes(
            @NotNull MqttGlobalPublishFilter filter,
            @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            boolean manualAcknowledgement);

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter                the filter with which all incoming Publish messages are filtered.
     * @param callback              the callback for all incoming Publish messages matching the given filter.
     * @param executor              the executor where the callback is executed on.
     * @param manualAcknowledgement whether the Publish messages are acknowledged manually.
     * @see #publishes(MqttGlobalPublishFilter, Consumer)
     * @see #publishes(MqttGlobalPublishFilter, Consumer, Executor)
     * @see #publishes(MqttGlobalPublishFilter, Consumer, boolean)
     * @since 1.2
     */
    void publishes(
            @NotNull MqttGlobalPublishFilter filter,
            @NotNull Consumer<@NotNull Mqtt5Publish> callback,
            @NotNull Executor executor,
            boolean manualAcknowledgement);

    /**
     * Unsubscribes this client with the given Unsubscribe message.
     *
     * @param unsubscribe the Unsubscribe message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *           <li>completes normally with the UnsubAck message if all Topic Filters of the Unsubscribe message were
     *             successfully unsubscribed (the UnsubAck message contains no Error Codes),
     *           <li>completes exceptionally with a
     *             {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5UnsubAckException Mqtt5UnsubAckException}
     *             wrapping the UnsubAck message if it contains at least one Error Code or
     *           <li>completes exceptionally with a different exception if an error occurred before the Unsubscribe
     *             message was sent or before a UnsubAck message was received.
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
    @CheckReturnValue
    Mqtt5UnsubscribeBuilder.Send.@NotNull Start<CompletableFuture<Mqtt5UnsubAck>> unsubscribeWith();

    /**
     * Publishes the given Publish message.
     *
     * @param publish the Publish message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *           <li>completes normally with the {@link Mqtt5PublishResult} if the Publish message was successfully
     *             published (no acknowledgement message contains an Error Code, {@link Mqtt5PublishResult#getError()}
     *             will always be absent),
     *           <li>completes exceptionally with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubAckException
     *             Mqtt5PubAckException} or {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubRecException
     *             Mqtt5PubRecException} wrapping the acknowledgement message if it contains an Error Code or
     *           <li>completes exceptionally with a different exception if an error occurred before the Publish message
     *             was sent or before an acknowledgement message was received.
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
    @CheckReturnValue
    Mqtt5PublishBuilder.@NotNull Send<CompletableFuture<Mqtt5PublishResult>> publishWith();

    /**
     * Re-authenticates this client.
     *
     * @return a {@link CompletableFuture} which
     *         <ul>
     *           <li>completes normally when the client was successfully re-authenticated,
     *           <li>errors with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5AuthException Mqtt5AuthException}
     *             wrapping the Auth message with the Error Code if not re-authenticated successfully or
     *           <li>errors with a different exception if an error occurred before the first Auth message was sent or
     *             before the last Auth message was received.
     *         </ul>
     */
    @NotNull CompletableFuture<Void> reauth();

    /**
     * Disconnects this client with the default Disconnect message.
     *
     * @return see {@link #disconnect(Mqtt5Disconnect)}.
     * @see #disconnect(Mqtt5Disconnect)
     */
    @NotNull CompletableFuture<Void> disconnect();

    /**
     * Disconnects this client with the given Disconnect message.
     *
     * @param disconnect the Disconnect message sent to the broker.
     * @return the {@link CompletableFuture} which
     *         <ul>
     *           <li>completes when the client was successfully disconnected or
     *           <li>errors if not disconnected gracefully.
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
    @CheckReturnValue
    Mqtt5DisconnectBuilder.@NotNull Send<CompletableFuture<Void>> disconnectWith();

    @Override
    @CheckReturnValue
    default @NotNull Mqtt5AsyncClient toAsync() {
        return this;
    }

    /**
     * Builder for a {@link Mqtt5Subscribe} and additional arguments that are applied to a {@link
     * #subscribe(Mqtt5Subscribe)}, {@link #subscribe(Mqtt5Subscribe, Consumer, boolean)} or {@link
     * #subscribe(Mqtt5Subscribe, Consumer, Executor, boolean)} call.
     */
    @ApiStatus.NonExtendable
    interface Mqtt5SubscribeAndCallbackBuilder
            extends Mqtt5SubscribeBuilderBase<Mqtt5SubscribeAndCallbackBuilder.Complete> {

        /**
         * {@link Mqtt5SubscribeAndCallbackBuilder} that is complete which means all mandatory fields are set.
         */
        @ApiStatus.NonExtendable
        interface Complete extends Mqtt5SubscribeAndCallbackBuilder, Mqtt5SubscribeAndCallbackBuilder.Call,
                Mqtt5SubscribeBuilderBase.Complete<Mqtt5SubscribeAndCallbackBuilder.Complete> {}

        /**
         * {@link Mqtt5SubscribeAndCallbackBuilder} that provides additional methods for the first subscription.
         */
        // @formatter:off
        @ApiStatus.NonExtendable
        interface Start extends Mqtt5SubscribeAndCallbackBuilder,
                Mqtt5SubscribeBuilderBase.Start<
                        Mqtt5SubscribeAndCallbackBuilder.Complete, Mqtt5SubscribeAndCallbackBuilder.Start.Complete> {
        // @formatter:on

            /**
             * {@link Mqtt5SubscribeAndCallbackBuilder.Start} that is complete which means all mandatory fields are
             * set.
             */
            // @formatter:off
            @ApiStatus.NonExtendable
            interface Complete extends
                    Mqtt5SubscribeAndCallbackBuilder.Start, Mqtt5SubscribeAndCallbackBuilder.Complete,
                    Mqtt5SubscribeBuilderBase.Start.Complete<
                            Mqtt5SubscribeAndCallbackBuilder.Complete,
                            Mqtt5SubscribeAndCallbackBuilder.Start.Complete> {}
            // @formatter:on
        }

        /**
         * Builder for additional arguments alongside the {@link Mqtt5Subscribe} that are applied to a {@link
         * #subscribe(Mqtt5Subscribe, Consumer, boolean)} or {@link #subscribe(Mqtt5Subscribe, Consumer, Executor,
         * boolean)} call.
         */
        @ApiStatus.NonExtendable
        interface Call {

            /**
             * Sets a callback for the matching Publish messages consumed via the subscriptions.
             *
             * @param callback the callback for the matching Publish messages.
             * @return the builder.
             */
            @CheckReturnValue
            @NotNull Ex callback(@NotNull Consumer<Mqtt5Publish> callback);

            /**
             * Builds the {@link Mqtt5Subscribe} and applies it and additional arguments to a {@link
             * #subscribe(Mqtt5Subscribe)}, {@link #subscribe(Mqtt5Subscribe, Consumer, boolean)} or {@link
             * #subscribe(Mqtt5Subscribe, Consumer, Executor, boolean)} call which then sends the Subscribe message.
             *
             * @return see {@link #subscribe(Mqtt5Subscribe)}, {@link #subscribe(Mqtt5Subscribe, Consumer, boolean)} or
             *         {@link #subscribe(Mqtt5Subscribe, Consumer, Executor, boolean)}.
             */
            @NotNull CompletableFuture<Mqtt5SubAck> send();

            /**
             * Builder for additional arguments alongside the {@link Mqtt5Subscribe} that are applied to a {@link
             * #subscribe(Mqtt5Subscribe, Consumer, Executor, boolean)} call.
             */
            @ApiStatus.NonExtendable
            interface Ex extends Call {

                /**
                 * Sets an executor to execute the callback for the matching Publish messages consumed via the
                 * subscriptions.
                 *
                 * @param executor the executor to execute the callback for the matching Publish messages.
                 * @return the builder.
                 */
                @CheckReturnValue
                @NotNull Ex executor(@NotNull Executor executor);

                /**
                 * Sets whether the matching Publish messages consumed via the subscriptions are acknowledged manually.
                 *
                 * @param manualAcknowledgement whether the matching Publish messages are acknowledged manually.
                 * @return the builder.
                 * @since 1.2
                 */
                @CheckReturnValue
                @NotNull Ex manualAcknowledgement(boolean manualAcknowledgement);
            }
        }
    }
}
