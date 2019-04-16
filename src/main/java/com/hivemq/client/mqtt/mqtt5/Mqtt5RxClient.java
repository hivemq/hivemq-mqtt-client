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
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectBuilder;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnectBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribeBuilder;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribeBuilder;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import com.hivemq.client.rx.FlowableWithSingle;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;

/**
 * Reactive API of a {@link Mqtt5Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5RxClient extends Mqtt5Client {

    /**
     * Creates a {@link Single} for connecting this client with the default Connect message.
     *
     * @return see {@link #connect(Mqtt5Connect)}.
     * @see #connect(Mqtt5Connect)
     */
    default @NotNull Single<Mqtt5ConnAck> connect() {
        return connect(MqttConnect.DEFAULT);
    }

    /**
     * Creates a {@link Single} for connecting this client with the given Connect message.
     * <p>
     * The returned {@link Single} represents the source of the ConnAck message corresponding to the given Connect
     * message. Calling this method does not connect yet. Connecting is performed lazy and asynchronous when subscribing
     * (in terms of Reactive Streams) to the returned {@link Single}.
     *
     * @param connect the Connect message sent to the broker during connect.
     * @return the {@link Single} which
     *         <ul>
     *         <li>succeeds with the ConnAck message if it does not contain an Error Code (connected
     *         successfully),</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
     *         Mqtt5ConnAckException} wrapping the ConnAck message if it contains an Error Code or</li>
     *         <li>errors with a different exception if an error occurred before the Connect message was sent or before
     *         the ConnAck message was received.</li>
     *         </ul>
     */
    @NotNull Single<Mqtt5ConnAck> connect(@NotNull Mqtt5Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt5Connect)}.
     * <p>
     * Calling {@link Mqtt5ConnectBuilder.Nested#applyConnect()} on the returned builder has the same effect as calling
     * {@link #connect(Mqtt5Connect)} with the result of {@link Mqtt5ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt5Connect)
     */
    default @NotNull Mqtt5ConnectBuilder.Nested<Single<Mqtt5ConnAck>> connectWith() {
        return new MqttConnectBuilder.Nested<>(this::connect);
    }

    /**
     * Creates a {@link Single} for subscribing this client with the given Subscribe message.
     * <p>
     * The returned {@link Single} represents the source of the SubAck message corresponding to the given Subscribe
     * message. Calling this method does not subscribe yet. Subscribing is performed lazy and asynchronous when
     * subscribing (in terms of Reactive Streams) to the returned {@link Single}.
     * <p>
     * See {@link #publishes(MqttGlobalPublishFilter)} to consume the incoming Publish messages. Alternatively, call
     * {@link #subscribeStream(Mqtt5Subscribe)} to consume the incoming Publish messages matching the subscriptions of
     * the Subscribe message directly.
     *
     * @param subscribe the Subscribe message sent to the broker during subscribe.
     * @return the {@link Single} which
     *         <ul>
     *         <li>succeeds with the SubAck message if at least one subscription of the Subscribe message was
     *         successful (the SubAck message contains at least one Reason Code that is not an Error Code),</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException
     *         Mqtt5SubAckException} wrapping the SubAck message if it only contains Error Codes or</li>
     *         <li>errors with a different exception if an error occurred before the Subscribe message was sent or
     *         before a SubAck message was received.</li>
     *         </ul>
     */
    @NotNull Single<Mqtt5SubAck> subscribe(@NotNull Mqtt5Subscribe subscribe);

    /**
     * Fluent counterpart of {@link #subscribe(Mqtt5Subscribe)}.
     * <p>
     * Calling {@link Mqtt5SubscribeBuilder.Nested.Complete#applySubscribe()} on the returned builder has the same
     * effect as calling {@link #subscribe(Mqtt5Subscribe)} with the result of {@link
     * Mqtt5SubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribe(Mqtt5Subscribe)
     */
    default @NotNull Mqtt5SubscribeBuilder.Nested.Start<Single<Mqtt5SubAck>> subscribeWith() {
        return new MqttSubscribeBuilder.Nested<>(this::subscribe);
    }

    /**
     * Creates a {@link FlowableWithSingle} for subscribing this client with the given Subscribe message.
     * <p>
     * The returned {@link FlowableWithSingle} represents the source of the SubAck message corresponding to the given
     * Subscribe message and the source of the incoming Publish messages matching the subscriptions of the Subscribe
     * message. Calling this method does not subscribe yet. Subscribing is performed lazy and asynchronous when
     * subscribing (in terms of Reactive Streams) to the returned {@link FlowableWithSingle}.
     *
     * @param subscribe the Subscribe message sent to the broker during subscribe.
     * @return the {@link FlowableWithSingle} which
     *         <ul>
     *         <li>emits the SubAck message as the single and first element if at least one subscription of the
     *         Subscribe message was successful (the SubAck message contains at least one Reason Code that is not an
     *         Error Code) and then emits the Publish messages matching the successful subscriptions of the Subscribe
     *         message,</li>
     *         <li>completes when all subscriptions of the Subscribe message were unsubscribed,</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException
     *         Mqtt5SubAckException} wrapping the SubAck message if it only contains Error Codes or</li>
     *         <li>errors with a different exception if an error occurred before the Subscribe message was sent,
     *         before a SubAck message was received or when a error occurs before all subscriptions of the Subscribe
     *         message were unsubscribed (e.g. {@link com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException
     *         MqttSessionExpiredException}).</li>
     *         </ul>
     */
    @NotNull FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribeStream(@NotNull Mqtt5Subscribe subscribe);

    /**
     * Fluent counterpart of {@link #subscribeStream(Mqtt5Subscribe)}.
     * <p>
     * Calling {@link Mqtt5SubscribeBuilder.Nested.Complete#applySubscribe()} on the returned builder has the same
     * effect as calling {@link #subscribeStream(Mqtt5Subscribe)} with the result of {@link
     * Mqtt5SubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribeStream(Mqtt5Subscribe)
     */
    default @NotNull Mqtt5SubscribeBuilder.Nested.Start<FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck>> subscribeStreamWith() {
        return new MqttSubscribeBuilder.Nested<>(this::subscribeStream);
    }

    /**
     * Creates a {@link Flowable} for globally consuming all incoming Publish messages matching the given filter.
     * <p>
     * The returned {@link Flowable} represents the source of the incoming Publish messages matching the given type.
     * Calling this method does not start consuming yet. This is done lazy and asynchronous when subscribing (in terms
     * of Reactive Streams) to the returned {@link Flowable}.
     *
     * @param filter the filter with which all incoming Publish messages are filtered.
     * @return the {@link Flowable} which
     *         <ul>
     *         <li>emits the incoming Publish messages matching the given filter,</li>
     *         <li>never completes but</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException
     *         MqttSessionExpiredException} when the MQTT session expires.</li>
     *         </ul>
     */
    @NotNull Flowable<Mqtt5Publish> publishes(@NotNull MqttGlobalPublishFilter filter);

    /**
     * Creates a {@link Single} for unsubscribing this client with the given Unsubscribe message.
     * <p>
     * The returned {@link Single} represents the source of the UnsubAck message corresponding to the given Unsubscribe
     * message. Calling this method does not unsubscribe yet. Unsubscribing is performed lazy and asynchronous when
     * subscribing (in terms of Reactive Streams) to the returned {@link Single}.
     *
     * @param unsubscribe the Unsubscribe message sent to the broker during unsubscribe.
     * @return the {@link Single} which
     *         <ul>
     *         <li>succeeds with the UnsubAck message if at least one Topic Filter of the Unsubscribe message was
     *         successfully unsubscribed (the UnsubAck message contains at least one Reason Code that is not an Error
     *         Code),</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5UnsubAckException
     *         Mqtt5UnsubAckException} wrapping the UnsubAck message if it only contains Error Codes or</li>
     *         <li>errors with a different exception if an error occurred before the Unsubscribe message was sent or
     *         before a UnsubAck message was received.</li>
     *         </ul>
     */
    @NotNull Single<Mqtt5UnsubAck> unsubscribe(@NotNull Mqtt5Unsubscribe unsubscribe);

    /**
     * Fluent counterpart of {@link #unsubscribe(Mqtt5Unsubscribe)}.
     * <p>
     * Calling {@link Mqtt5UnsubscribeBuilder.Nested.Complete#applyUnsubscribe()} on the returned builder has the same
     * effect as calling {@link #unsubscribe(Mqtt5Unsubscribe)} with the result of {@link
     * Mqtt5UnsubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #unsubscribe(Mqtt5Unsubscribe)
     */
    default @NotNull Mqtt5UnsubscribeBuilder.Nested.Start<Single<Mqtt5UnsubAck>> unsubscribeWith() {
        return new MqttUnsubscribeBuilder.Nested<>(this::unsubscribe);
    }

    /**
     * Creates a {@link Flowable} for publishing the Publish messages emitted by the given {@link Flowable}.
     * <p>
     * The returned {@link Flowable} represents the source of {@link Mqtt5PublishResult}s each corresponding to a
     * Publish message emitted by the given {@link Flowable}. Calling this method does not start publishing yet.
     * Publishing is performed lazy and asynchronous. When subscribing (in terms of Reactive Streams) to the returned
     * {@link Flowable} the client subscribes (in terms of Reactive Streams) to the given {@link Flowable}.
     *
     * @param publishFlowable the source of the Publish messages to publish.
     * @return the {@link Flowable} which
     *         <ul>
     *         <li>emits {@link Mqtt5PublishResult}s each corresponding to a Publish message,</li>
     *         <li>completes when the given {@link Flowable} completes,</li>
     *         <li>errors with the same exception when the given {@link Flowable} errors or</li>
     *         <li>errors with a different exception if an error occurred before all Publish messages of the given
     *         {@link Flowable} are answered by a {@link Mqtt5PublishResult} (e.g. {@link
     *         com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException MqttSessionExpiredException}).</li>
     *         </ul>
     */
    @NotNull Flowable<Mqtt5PublishResult> publish(@NotNull Flowable<Mqtt5Publish> publishFlowable);

    /**
     * Creates a {@link Completable} for re-authenticating this client.
     * <p>
     * Calling this method does not re-authenticate yet. Re-authenticating is performed lazy and asynchronous when
     * subscribing (in terms of Reactive Streams) to the returned {@link Completable}.
     *
     * @return the {@link Completable} which
     *         <ul>
     *         <li>completes when the client was successfully re-authenticated,</li>
     *         <li>errors with a {@link com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5AuthException
     *         Mqtt5AuthException} wrapping the Auth message with the Error Code if not re-authenticated successfully
     *         or</li>
     *         <li>errors with a different exception if an error occurred before the first Auth message was sent or
     *         before the last Auth message was received.</li>
     *         </ul>
     */
    @NotNull Completable reauth();

    /**
     * Creates a {@link Completable} for disconnecting this client with the default Disconnect message.
     *
     * @return see {@link #disconnect(Mqtt5Disconnect)}.
     * @see #disconnect(Mqtt5Disconnect)
     */
    default @NotNull Completable disconnect() {
        return disconnect(MqttDisconnect.DEFAULT);
    }

    /**
     * Creates a {@link Completable} for disconnecting this client with the given Disconnect message.
     * <p>
     * Calling this method does not disconnect yet. Disconnecting is performed lazy and asynchronous when subscribing
     * (in terms of Reactive Streams) to the returned {@link Completable}.
     *
     * @param disconnect the Disconnect message sent to the broker during disconnect.
     * @return the {@link Completable} which
     *         <ul>
     *         <li>completes when the client was successfully disconnected or</li>
     *         <li>errors if not disconnected gracefully.</li>
     *         </ul>
     */
    @NotNull Completable disconnect(@NotNull Mqtt5Disconnect disconnect);

    /**
     * Fluent counterpart of {@link #disconnect(Mqtt5Disconnect)}.
     * <p>
     * Calling {@link Mqtt5DisconnectBuilder.Nested#applyDisconnect()} on the returned builder has the same effect as
     * calling {@link #disconnect(Mqtt5Disconnect)} with the result of {@link Mqtt5DisconnectBuilder#build()}.
     *
     * @return the builder for the Disconnect message.
     * @see #disconnect(Mqtt5Disconnect)
     */
    default @NotNull Mqtt5DisconnectBuilder.Nested<Completable> disconnectWith() {
        return new MqttDisconnectBuilder.Nested<>(this::disconnect);
    }

    @Override
    default @NotNull Mqtt5RxClient toRx() {
        return this;
    }
}
