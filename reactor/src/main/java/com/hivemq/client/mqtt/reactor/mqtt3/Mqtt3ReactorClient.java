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

package com.hivemq.client.mqtt.reactor.mqtt3;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeViewBuilder;
import com.hivemq.client.internal.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeViewBuilder;
import com.hivemq.client.internal.mqtt.reactor.mqtt3.Mqtt3ReactorClientView;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.unsuback.Mqtt3UnsubAck;
import com.hivemq.client.rx.reactor.FluxWithSingle;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactor API of a {@link Mqtt3Client}.
 *
 * @author Silvio Giebl
 * @since 1.2
 */
@DoNotImplement
public interface Mqtt3ReactorClient extends Mqtt3Client {

    /**
     * Turns the API of the given client into a reactor API.
     * <p>
     * The reactor API can be used simultaneously with the other APIs.
     *
     * @param client the client with any API (blocking, async, reactive, reactor).
     * @return a reactor API for the given client.
     */
    static @NotNull Mqtt3ReactorClient from(final @NotNull Mqtt3Client client) {
        if (client instanceof Mqtt3ReactorClient) {
            return (Mqtt3ReactorClient) client;
        }
        return new Mqtt3ReactorClientView(client.toRx());
    }

    /**
     * Creates a {@link Mono} for connecting this client with the default Connect message.
     *
     * @return see {@link #connect(Mqtt3Connect)}.
     * @see #connect(Mqtt3Connect)
     */
    default @NotNull Mono<Mqtt3ConnAck> connect() {
        return connect(Mqtt3ConnectView.DEFAULT);
    }

    /**
     * Creates a {@link Mono} for connecting this client with the given Connect message.
     * <p>
     * The returned {@link Mono} represents the source of the ConnAck message corresponding to the given Connect
     * message. Calling this method does not connect yet. Connecting is performed lazy and asynchronous when subscribing
     * (in terms of Reactive Streams) to the returned {@link Mono}.
     *
     * @param connect the Connect message sent to the broker during connect.
     * @return the {@link Mono} which
     *         <ul>
     *         <li>succeeds with the ConnAck message if it does not contain an Error Code (connected
     *         successfully),</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException
     *         Mqtt3ConnAckException} wrapping the ConnAck message if it contains an Error Code or</li>
     *         <li>errors with a different exception if an error occurred before the Connect message was sent or before
     *         the ConnAck message was received.</li>
     *         </ul>
     */
    @NotNull Mono<Mqtt3ConnAck> connect(@NotNull Mqtt3Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt3Connect)}.
     * <p>
     * Calling {@link Mqtt3ConnectBuilder.Nested#applyConnect()} on the returned builder has the same effect as calling
     * {@link #connect(Mqtt3Connect)} with the result of {@link Mqtt3ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt3Connect)
     */
    default @NotNull Mqtt3ConnectBuilder.Nested<Mono<Mqtt3ConnAck>> connectWith() {
        return new Mqtt3ConnectViewBuilder.Nested<>(this::connect);
    }

    /**
     * Creates a {@link Mono} for subscribing this client with the given Subscribe message.
     * <p>
     * The returned {@link Mono} represents the source of the SubAck message corresponding to the given Subscribe
     * message. Calling this method does not subscribe yet. Subscribing is performed lazy and asynchronous when
     * subscribing (in terms of Reactive Streams) to the returned {@link Mono}.
     * <p>
     * See {@link #publishes(MqttGlobalPublishFilter)} to consume the incoming Publish messages. Alternatively, call
     * {@link #subscribeStream(Mqtt3Subscribe)} to consume the incoming Publish messages matching the subscriptions of
     * the Subscribe message directly.
     *
     * @param subscribe the Subscribe message sent to the broker during subscribe.
     * @return the {@link Mono} which
     *         <ul>
     *         <li>succeeds with the SubAck message if at least one subscription of the Subscribe message was
     *         successful (the SubAck message contains at least one Return Code that is not an Error Code),</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3SubAckException
     *         Mqtt3SubAckException} wrapping the SubAck message if it only contains Error Codes or</li>
     *         <li>errors with a different exception if an error occurred before the Subscribe message was sent or
     *         before a SubAck message was received.</li>
     *         </ul>
     */
    @NotNull Mono<Mqtt3SubAck> subscribe(@NotNull Mqtt3Subscribe subscribe);

    /**
     * Fluent counterpart of {@link #subscribe(Mqtt3Subscribe)}.
     * <p>
     * Calling {@link Mqtt3SubscribeBuilder.Nested.Complete#applySubscribe()} on the returned builder has the same
     * effect as calling {@link #subscribe(Mqtt3Subscribe)} with the result of {@link
     * Mqtt3SubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribe(Mqtt3Subscribe)
     */
    default @NotNull Mqtt3SubscribeBuilder.Nested.Start<Mono<Mqtt3SubAck>> subscribeWith() {
        return new Mqtt3SubscribeViewBuilder.Nested<>(this::subscribe);
    }

    /**
     * Creates a {@link FluxWithSingle} for subscribing this client with the given Subscribe message.
     * <p>
     * The returned {@link FluxWithSingle} represents the source of the SubAck message corresponding to the given
     * Subscribe message and the source of the incoming Publish messages matching the subscriptions of the Subscribe
     * message. Calling this method does not subscribe yet. Subscribing is performed lazy and asynchronous when
     * subscribing (in terms of Reactive Streams) to the returned {@link FluxWithSingle}.
     *
     * @param subscribe the Subscribe message sent to the broker during subscribe.
     * @return the {@link FluxWithSingle} which
     *         <ul>
     *         <li>emits the SubAck message as the single and first element if at least one subscription of the
     *         Subscribe message was successful (the SubAck message contains at least one Return Code that is not an
     *         Error Code) and then emits the Publish messages matching the successful subscriptions of the Subscribe
     *         message,</li>
     *         <li>completes when all subscriptions of the Subscribe message were unsubscribed,</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3SubAckException
     *         Mqtt3SubAckException} wrapping the SubAck message if it only contains Error Codes or</li>
     *         <li>errors with a different exception if an error occurred before the Subscribe message was sent,
     *         before a SubAck message was received or when a error occurs before all subscriptions of the Subscribe
     *         message were unsubscribed (e.g. {@link com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException
     *         MqttSessionExpiredException}).</li>
     *         </ul>
     */
    @NotNull FluxWithSingle<Mqtt3Publish, Mqtt3SubAck> subscribeStream(@NotNull Mqtt3Subscribe subscribe);

    /**
     * Fluent counterpart of {@link #subscribeStream(Mqtt3Subscribe)}.
     * <p>
     * Calling {@link Mqtt3SubscribeBuilder.Nested.Complete#applySubscribe()} on the returned builder has the same
     * effect as calling {@link #subscribeStream(Mqtt3Subscribe)} with the result of {@link
     * Mqtt3SubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribeStream(Mqtt3Subscribe)
     */
    default @NotNull Mqtt3SubscribeBuilder.Nested.Start<FluxWithSingle<Mqtt3Publish, Mqtt3SubAck>> subscribeStreamWith() {
        return new Mqtt3SubscribeViewBuilder.Nested<>(this::subscribeStream);
    }

    /**
     * Creates a {@link Flux} for globally consuming all incoming Publish messages matching the given filter.
     * <p>
     * The returned {@link Flux} represents the source of the incoming Publish messages matching the given type. Calling
     * this method does not start consuming yet. This is done lazy and asynchronous when subscribing (in terms of
     * Reactive Streams) to the returned {@link Flux}.
     *
     * @param filter the filter with which all incoming Publish messages are filtered.
     * @return the {@link Flux} which
     *         <ul>
     *         <li>emits the incoming Publish messages matching the given filter,</li>
     *         <li>never completes but</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException
     *         MqttSessionExpiredException} when the MQTT session expires.</li>
     *         </ul>
     */
    @NotNull Flux<Mqtt3Publish> publishes(@NotNull MqttGlobalPublishFilter filter);

    /**
     * Creates a {@link Mono} for unsubscribing this client with the given Unsubscribe message.
     * <p>
     * The returned {@link Mono} represents the source of the UnsubAck message corresponding to the given Unsubscribe
     * message. Calling this method does not unsubscribe yet. Unsubscribing is performed lazy and asynchronous when
     * subscribing (in terms of Reactive Streams) to the returned {@link Mono}.
     *
     * @param unsubscribe the Unsubscribe message sent to the broker during unsubscribe.
     * @return the {@link Mono} which
     *         <ul>
     *         <li>succeeds when the corresponding UnsubAck message was received or</li>
     *         <li>errors if an error occurred before the Unsubscribe message was sent or before a UnsubAck message was
     *         received.</li>
     *         </ul>
     */
    @NotNull Mono<Mqtt3UnsubAck> unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

    /**
     * Fluent counterpart of {@link #unsubscribe(Mqtt3Unsubscribe)}.
     * <p>
     * Calling {@link Mqtt3UnsubscribeBuilder.Nested.Complete#applyUnsubscribe()} on the returned builder has the same
     * effect as calling {@link #unsubscribe(Mqtt3Unsubscribe)} with the result of {@link
     * Mqtt3UnsubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #unsubscribe(Mqtt3Unsubscribe)
     */
    default @NotNull Mqtt3UnsubscribeBuilder.Nested.Start<Mono<Mqtt3UnsubAck>> unsubscribeWith() {
        return new Mqtt3UnsubscribeViewBuilder.Nested<>(this::unsubscribe);
    }

    /**
     * Creates a {@link Flux} for publishing the Publish messages emitted by the given {@link Publisher}.
     * <p>
     * The returned {@link Flux} represents the source of {@link Mqtt3PublishResult}s each corresponding to a Publish
     * message emitted by the given {@link Publisher}. Calling this method does not start publishing yet. Publishing is
     * performed lazy and asynchronous. When subscribing (in terms of Reactive Streams) to the returned {@link Flux} the
     * client subscribes (in terms of Reactive Streams) to the given {@link Publisher}.
     *
     * @param publishFlowable the source of the Publish messages to publish.
     * @return the {@link Flux} which
     *         <ul>
     *         <li>emits {@link Mqtt3PublishResult}s each corresponding to a Publish message,</li>
     *         <li>completes when the given {@link Publisher} completes,</li>
     *         <li>errors with the same exception when the given {@link Publisher} errors or</li>
     *         <li>errors with a different exception if an error occurred before all Publish messages of the given
     *         {@link Publisher} are answered by a {@link Mqtt3PublishResult} (e.g. {@link
     *         com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException MqttSessionExpiredException}).</li>
     *         </ul>
     */
    @NotNull Flux<Mqtt3PublishResult> publish(@NotNull Publisher<Mqtt3Publish> publishFlowable);

    /**
     * Creates a {@link Mono} for disconnecting this client.
     * <p>
     * Calling this method does not disconnect yet. Disconnecting is performed lazy and asynchronous when subscribing
     * (in terms of Reactive Streams) to the returned {@link Mono}.
     *
     * @return the {@link Mono} which
     *         <ul>
     *         <li>completes when the client was successfully disconnected or</li>
     *         <li>errors if not disconnected gracefully.</li>
     *         </ul>
     */
    @NotNull Mono<Void> disconnect();
}
