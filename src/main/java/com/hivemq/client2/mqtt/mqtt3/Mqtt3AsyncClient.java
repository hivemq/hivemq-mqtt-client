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

package com.hivemq.client2.mqtt.mqtt3;

import com.hivemq.client2.annotations.CheckReturnValue;
import com.hivemq.client2.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client2.mqtt.mqtt3.message.connect.Mqtt3ConnAck;
import com.hivemq.client2.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client2.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client2.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client2.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client2.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import com.hivemq.client2.mqtt.mqtt3.message.subscribe.Mqtt3SubAck;
import com.hivemq.client2.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client2.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilderBase;
import com.hivemq.client2.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubAck;
import com.hivemq.client2.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.client2.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Asynchronous API of an {@link Mqtt3Client} based on futures and callbacks.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3AsyncClient extends Mqtt3Client {

    /**
     * Connects this client with the default Connect message.
     *
     * @return see {@link #connect(Mqtt3Connect)}.
     * @see #connect(Mqtt3Connect)
     */
    @NotNull CompletableFuture<@NotNull Mqtt3ConnAck> connect();

    /**
     * Connects this client with the given Connect message.
     *
     * @param connect the Connect message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *           <li>completes normally with the ConnAck message if it does not contain an Error Code (connected
     *             successfully),
     *           <li>completes exceptionally with a {@link com.hivemq.client2.mqtt.mqtt3.exceptions.Mqtt3ConnAckException
     *             Mqtt3ConnAckException} wrapping the ConnAck message if it contains an Error Code or
     *           <li>completes exceptionally with a different exception if an error occurred before the Connect message
     *             was sent or before the ConnAck message was received.
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt3ConnAck> connect(@NotNull Mqtt3Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt3Connect)}.
     * <p>
     * Calling {@link Mqtt3ConnectBuilder.Send#send()} on the returned builder has the same effect as calling {@link
     * #connect(Mqtt3Connect)} with the result of {@link Mqtt3ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt3Connect)
     */
    @CheckReturnValue
    Mqtt3ConnectBuilder.@NotNull Send<CompletableFuture<Mqtt3ConnAck>> connectWith();

    /**
     * Subscribes this client with the given Subscribe message.
     * <p>
     * See {@link #publishes(MqttGlobalPublishFilter, Consumer)} or {@link #publishes(MqttGlobalPublishFilter, Consumer,
     * Executor)} to consume the incoming Publish messages. Alternatively, call {@link #subscribe(Mqtt3Subscribe,
     * Consumer)} or {@link #subscribe(Mqtt3Subscribe, Consumer, Executor)} to consume the incoming Publish messages
     * matching the subscriptions of the Subscribe message directly.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *           <li>completes normally with the SubAck message if all subscriptions of the Subscribe message were
     *             successful (the SubAck message contains no Error Codes),
     *           <li>completes exceptionally with a {@link com.hivemq.client2.mqtt.mqtt3.exceptions.Mqtt3SubAckException
     *             Mqtt3SubAckException} wrapping the SubAck message if it contains at least one Error Code or
     *           <li>completes exceptionally with a different exception if an error occurred before the Subscribe
     *             message was sent or before a SubAck message was received.
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(@NotNull Mqtt3Subscribe subscribe);

    /**
     * Subscribes this client with the given Subscribe message and consumes the incoming Publish messages matching the
     * subscriptions of the Subscribe message with a callback.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @param callback  the callback for consuming the incoming Publish messages matching the subscriptions of the
     *                  Subscribe message.
     * @return see {@link #subscribe(Mqtt3Subscribe)}.
     * @see #subscribe(Mqtt3Subscribe, Consumer, Executor)
     * @see #subscribe(Mqtt3Subscribe, Consumer, boolean)
     * @see #subscribe(Mqtt3Subscribe, Consumer, Executor, boolean)
     */
    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            @NotNull Mqtt3Subscribe subscribe, @NotNull Consumer<@NotNull Mqtt3Publish> callback);

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
     * @return see {@link #subscribe(Mqtt3Subscribe)}.
     * @see #subscribe(Mqtt3Subscribe, Consumer)
     * @see #subscribe(Mqtt3Subscribe, Consumer, boolean)
     * @see #subscribe(Mqtt3Subscribe, Consumer, Executor, boolean)
     */
    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            @NotNull Mqtt3Subscribe subscribe,
            @NotNull Consumer<@NotNull Mqtt3Publish> callback,
            @NotNull Executor executor);

    /**
     * Subscribes this client with the given Subscribe message and consumes the incoming Publish messages matching the
     * subscriptions of the Subscribe message with a callback.
     *
     * @param subscribe             the Subscribe messages sent to the broker.
     * @param callback              the callback for consuming the incoming Publish messages matching the subscriptions
     *                              of the Subscribe message.
     * @param manualAcknowledgement whether the Publish messages are acknowledged manually.
     * @return see {@link #subscribe(Mqtt3Subscribe)}.
     * @see #subscribe(Mqtt3Subscribe, Consumer)
     * @see #subscribe(Mqtt3Subscribe, Consumer, Executor)
     * @see #subscribe(Mqtt3Subscribe, Consumer, Executor, boolean)
     * @since 1.2
     */
    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            @NotNull Mqtt3Subscribe subscribe,
            @NotNull Consumer<@NotNull Mqtt3Publish> callback,
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
     * @return see {@link #subscribe(Mqtt3Subscribe)}.
     * @see #subscribe(Mqtt3Subscribe, Consumer)
     * @see #subscribe(Mqtt3Subscribe, Consumer, Executor)
     * @see #subscribe(Mqtt3Subscribe, Consumer, boolean)
     * @since 1.2
     */
    @NotNull CompletableFuture<@NotNull Mqtt3SubAck> subscribe(
            @NotNull Mqtt3Subscribe subscribe,
            @NotNull Consumer<@NotNull Mqtt3Publish> callback,
            @NotNull Executor executor,
            boolean manualAcknowledgement);

    /**
     * Fluent counterpart of {@link #subscribe(Mqtt3Subscribe)}, {@link #subscribe(Mqtt3Subscribe, Consumer, boolean)}
     * and {@link #subscribe(Mqtt3Subscribe, Consumer, Executor, boolean)}.
     * <p>
     * Calling {@link SubscribeBuilder.Complete#send()} on the returned builder has the same effect as calling one of
     * the following methods:
     * <ul>
     *   <li>{@link #subscribe(Mqtt3Subscribe)} if no callback has been supplied to the builder
     *   <li>{@link #subscribe(Mqtt3Subscribe, Consumer)} if only a callback has been supplied to the builder
     *   <li>{@link #subscribe(Mqtt3Subscribe, Consumer, Executor)} if a callback and an executor have been supplied to
     *     the builder
     * </ul>
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribe(Mqtt3Subscribe)
     * @see #subscribe(Mqtt3Subscribe, Consumer, boolean)
     * @see #subscribe(Mqtt3Subscribe, Consumer, Executor, boolean)
     */
    @CheckReturnValue
    SubscribeBuilder.@NotNull Start subscribeWith();

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter   the filter with which all incoming Publish messages are filtered.
     * @param callback the callback for all incoming Publish messages matching the given filter.
     * @see #publishes(MqttGlobalPublishFilter, Consumer, Executor)
     * @see #publishes(MqttGlobalPublishFilter, Consumer, boolean)
     * @see #publishes(MqttGlobalPublishFilter, Consumer, Executor, boolean)
     */
    void publishes(@NotNull MqttGlobalPublishFilter filter, @NotNull Consumer<@NotNull Mqtt3Publish> callback);

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
            @NotNull Consumer<@NotNull Mqtt3Publish> callback,
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
            @NotNull Consumer<@NotNull Mqtt3Publish> callback,
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
            @NotNull Consumer<@NotNull Mqtt3Publish> callback,
            @NotNull Executor executor,
            boolean manualAcknowledgement);

    /**
     * Unsubscribes this client with the given Unsubscribe message.
     *
     * @param unsubscribe the Unsubscribe message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *           <li>completes normally with the UnsubAck message or
     *           <li>completes exceptionally if an error occurred before the Unsubscribe message was sent or before a
     *             UnsubAck message was received.
     *         </ul>
     */
    @NotNull CompletableFuture<Mqtt3UnsubAck> unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

    /**
     * Fluent counterpart of {@link #unsubscribe(Mqtt3Unsubscribe)}.
     * <p>
     * Calling {@link Mqtt3UnsubscribeBuilder.Send.Complete#send()} on the returned builder has the same effect as
     * calling {@link #unsubscribe(Mqtt3Unsubscribe)} with the result of {@link Mqtt3UnsubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #unsubscribe(Mqtt3Unsubscribe)
     */
    @CheckReturnValue
    Mqtt3UnsubscribeBuilder.Send.@NotNull Start<CompletableFuture<Mqtt3UnsubAck>> unsubscribeWith();

    /**
     * Publishes the given Publish message.
     *
     * @param publish the Publish message sent to the broker.
     * @return a {@link CompletableFuture} which
     *         <ul>
     *           <li>completes normally with the {@link Mqtt3PublishResult} if the Publish message was successfully
     *             published ({@link Mqtt3PublishResult#getError()} will always be absent) or
     *           <li>completes exceptionally if an error occurred before the Publish message was sent or before an
     *             acknowledgement message was received.
     *         </ul>
     */
    @NotNull CompletableFuture<@NotNull Mqtt3PublishResult> publish(@NotNull Mqtt3Publish publish);

    /**
     * Fluent counterpart of {@link #publish(Mqtt3Publish)}.
     * <p>
     * Calling {@link Mqtt3PublishBuilder.Send.Complete#send()} on the returned builder has the same effect as calling
     * {@link #publish(Mqtt3Publish)} with the result of {@link Mqtt3PublishBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #publish(Mqtt3Publish)
     */
    @CheckReturnValue
    Mqtt3PublishBuilder.@NotNull Send<CompletableFuture<Mqtt3PublishResult>> publishWith();

    /**
     * Disconnects this client.
     *
     * @return the {@link CompletableFuture} which
     *         <ul>
     *           <li>completes when the client was successfully disconnected or
     *           <li>errors if not disconnected gracefully.
     *         </ul>
     */
    @NotNull CompletableFuture<Void> disconnect();

    @Override
    @CheckReturnValue
    default @NotNull Mqtt3AsyncClient toAsync() {
        return this;
    }

    /**
     * Builder for a {@link Mqtt3Subscribe} and additional arguments that are applied to a {@link
     * #subscribe(Mqtt3Subscribe)}, {@link #subscribe(Mqtt3Subscribe, Consumer, boolean)} or {@link
     * #subscribe(Mqtt3Subscribe, Consumer, Executor, boolean)} call.
     */
    @ApiStatus.NonExtendable
    interface SubscribeBuilder extends Mqtt3SubscribeBuilderBase<SubscribeBuilder.Complete> {

        /**
         * {@link SubscribeBuilder} that is complete which means all mandatory fields are set.
         */
        @ApiStatus.NonExtendable
        interface Complete extends SubscribeBuilder, AfterComplete,
                Mqtt3SubscribeBuilderBase.Complete<SubscribeBuilder.Complete> {}

        /**
         * {@link SubscribeBuilder} that provides additional methods for the first subscription.
         */
        @ApiStatus.NonExtendable
        interface Start extends SubscribeBuilder,
                Mqtt3SubscribeBuilderBase.Start<SubscribeBuilder.Complete, SubscribeBuilder.Start.Complete> {

            /**
             * {@link SubscribeBuilder.Start} that is complete which means all mandatory fields are set.
             */
            // @formatter:off
            @ApiStatus.NonExtendable
            interface Complete extends SubscribeBuilder.Start, SubscribeBuilder.Complete,
                    Mqtt3SubscribeBuilderBase.Start.Complete<
                            SubscribeBuilder.Complete, SubscribeBuilder.Start.Complete> {}
            // @formatter:on
        }

        /**
         * Builder for additional arguments alongside the {@link Mqtt3Subscribe} that are applied to a {@link
         * #subscribe(Mqtt3Subscribe, Consumer, boolean)} or {@link #subscribe(Mqtt3Subscribe, Consumer, Executor,
         * boolean)} call.
         */
        @ApiStatus.NonExtendable
        interface AfterComplete {

            /**
             * Sets a callback for the matching Publish messages consumed via the subscriptions.
             *
             * @param callback the callback for the matching Publish messages.
             * @return the builder.
             */
            @CheckReturnValue
            @NotNull AfterCallback callback(@NotNull Consumer<Mqtt3Publish> callback);

            /**
             * Builds the {@link Mqtt3Subscribe} and applies it and additional arguments to a {@link
             * #subscribe(Mqtt3Subscribe)}, {@link #subscribe(Mqtt3Subscribe, Consumer, boolean)} or {@link
             * #subscribe(Mqtt3Subscribe, Consumer, Executor, boolean)} call which then sends the Subscribe message.
             *
             * @return see {@link #subscribe(Mqtt3Subscribe)}, {@link #subscribe(Mqtt3Subscribe, Consumer, boolean)} or
             *         {@link #subscribe(Mqtt3Subscribe, Consumer, Executor, boolean)}.
             */
            @NotNull CompletableFuture<Mqtt3SubAck> send();
        }

        /**
         * Builder for additional arguments alongside the {@link Mqtt3Subscribe} that are applied to a {@link
         * #subscribe(Mqtt3Subscribe, Consumer, Executor, boolean)} call.
         */
        @ApiStatus.NonExtendable
        interface AfterCallback extends AfterComplete {

            /**
             * Sets an executor to execute the callback for the matching Publish messages consumed via the
             * subscriptions.
             *
             * @param executor the executor to execute the callback for the matching Publish messages.
             * @return the builder.
             */
            @CheckReturnValue
            @NotNull AfterCallback executor(@NotNull Executor executor);

            /**
             * Sets whether the matching Publish messages consumed via the subscriptions are acknowledged manually.
             *
             * @param manualAcknowledgement whether the matching Publish messages are acknowledged manually.
             * @return the builder.
             * @since 1.2
             */
            @CheckReturnValue
            @NotNull AfterCallback manualAcknowledgement(boolean manualAcknowledgement);
        }
    }
}
