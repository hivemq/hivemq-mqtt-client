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

package com.hivemq.client.mqtt.mqtt3;

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
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
import com.hivemq.client.rx.FlowableWithSingle;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

/**
 * Reactive API of an {@link Mqtt3Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3RxClient extends Mqtt3Client {

    /**
     * Creates a {@link Single} for connecting this client with the default Connect message.
     *
     * @return see {@link #connect(Mqtt3Connect)}.
     * @see #connect(Mqtt3Connect)
     */
    @CheckReturnValue
    @NotNull Single<Mqtt3ConnAck> connect();

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
     *           <li>succeeds with the ConnAck message if it does not contain an Error Code (connected successfully),
     *           <li>errors with a {@link com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException
     *             Mqtt3ConnAckException} wrapping the ConnAck message if it contains an Error Code or
     *           <li>errors with a different exception if an error occurred before the Connect message was sent or
     *             before the ConnAck message was received.
     *         </ul>
     */
    @CheckReturnValue
    @NotNull Single<Mqtt3ConnAck> connect(@NotNull Mqtt3Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt3Connect)}.
     * <p>
     * Calling {@link Mqtt3ConnectBuilder.Nested#applyConnect()} on the returned builder has the same effect as calling
     * {@link #connect(Mqtt3Connect)} with the result of {@link Mqtt3ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt3Connect)
     */
    @CheckReturnValue
    Mqtt3ConnectBuilder.@NotNull Nested<Single<Mqtt3ConnAck>> connectWith();

    /**
     * Creates a {@link Single} for subscribing this client with the given Subscribe message.
     * <p>
     * The returned {@link Single} represents the source of the SubAck message corresponding to the given Subscribe
     * message. Calling this method does not subscribe yet. Subscribing is performed lazy and asynchronous when
     * subscribing (in terms of Reactive Streams) to the returned {@link Single}.
     * <p>
     * See {@link #publishes(MqttGlobalPublishFilter)} to consume the incoming Publish messages. Alternatively, call
     * {@link #subscribePublishes(Mqtt3Subscribe)} to consume the incoming Publish messages matching the subscriptions
     * of the Subscribe message directly.
     *
     * @param subscribe the Subscribe message sent to the broker during subscribe.
     * @return the {@link Single} which
     *         <ul>
     *           <li>succeeds with the SubAck message if at least one subscription of the Subscribe message was
     *             successful (the SubAck message contains at least one Return Code that is not an Error Code),
     *           <li>errors with a {@link com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3SubAckException
     *             Mqtt3SubAckException} wrapping the SubAck message if it only contains Error Codes or
     *           <li>errors with a different exception if an error occurred before the Subscribe message was sent or
     *             before a SubAck message was received.
     *         </ul>
     */
    @CheckReturnValue
    @NotNull Single<Mqtt3SubAck> subscribe(@NotNull Mqtt3Subscribe subscribe);

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
    @CheckReturnValue
    Mqtt3SubscribeBuilder.Nested.@NotNull Start<Single<Mqtt3SubAck>> subscribeWith();

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
     *           <li>emits the SubAck message as the single and first element if at least one subscription of the
     *             Subscribe message was successful (the SubAck message contains at least one Return Code that is not an
     *             Error Code) and then emits the Publish messages matching the successful subscriptions of the
     *             Subscribe message,
     *           <li>completes when all subscriptions of the Subscribe message were unsubscribed,
     *           <li>errors with a {@link com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3SubAckException
     *             Mqtt3SubAckException} wrapping the SubAck message if it only contains Error Codes or
     *           <li>errors with a different exception if an error occurred before the Subscribe message was sent,
     *             before a SubAck message was received or when a error occurs before all subscriptions of the Subscribe
     *             message were unsubscribed (e.g. {@link com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException
     *             MqttSessionExpiredException}).
     *         </ul>
     * @see #subscribePublishes(Mqtt3Subscribe, boolean)
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck> subscribePublishes(@NotNull Mqtt3Subscribe subscribe);

    /**
     * Creates a {@link FlowableWithSingle} for subscribing this client with the given Subscribe message.
     * <p>
     * The returned {@link FlowableWithSingle} represents the source of the SubAck message corresponding to the given
     * Subscribe message and the source of the incoming Publish messages matching the subscriptions of the Subscribe
     * message. Calling this method does not subscribe yet. Subscribing is performed lazy and asynchronous when
     * subscribing (in terms of Reactive Streams) to the returned {@link FlowableWithSingle}.
     *
     * @param subscribe             the Subscribe message sent to the broker during subscribe.
     * @param manualAcknowledgement whether the Publish messages are acknowledged manually.
     * @return the {@link FlowableWithSingle} which
     *         <ul>
     *           <li>emits the SubAck message as the single and first element if at least one subscription of the
     *             Subscribe message was successful (the SubAck message contains at least one Return Code that is not an
     *             Error Code) and then emits the Publish messages matching the successful subscriptions of the
     *             Subscribe message,
     *           <li>completes when all subscriptions of the Subscribe message were unsubscribed,
     *           <li>errors with a {@link com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3SubAckException
     *             Mqtt3SubAckException} wrapping the SubAck message if it only contains Error Codes or
     *           <li>errors with a different exception if an error occurred before the Subscribe message was sent,
     *             before a SubAck message was received or when a error occurs before all subscriptions of the Subscribe
     *             message were unsubscribed (e.g. {@link com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException
     *             MqttSessionExpiredException}).
     *         </ul>
     * @see #subscribePublishes(Mqtt3Subscribe)
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck> subscribePublishes(
            @NotNull Mqtt3Subscribe subscribe, boolean manualAcknowledgement);

    /**
     * Fluent counterpart of {@link #subscribePublishes(Mqtt3Subscribe, boolean)}.
     * <p>
     * Calling {@link Mqtt3SubscribeBuilder.Nested.Complete#applySubscribe()} on the returned builder has the same
     * effect as calling {@link #subscribePublishes(Mqtt3Subscribe)} with the result of {@link
     * Mqtt3SubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribePublishes(Mqtt3Subscribe, boolean)
     * @since 1.2
     */
    @CheckReturnValue
    Mqtt3SubscribeBuilder.Publishes.@NotNull Start<FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck>> subscribePublishesWith();

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
     *           <li>emits the incoming Publish messages matching the given filter,
     *           <li>never completes but
     *           <li>errors with a {@link com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException
     *             MqttSessionExpiredException} when the MQTT session expires.
     *         </ul>
     * @see #publishes(MqttGlobalPublishFilter, boolean)
     */
    @CheckReturnValue
    @NotNull Flowable<Mqtt3Publish> publishes(final @NotNull MqttGlobalPublishFilter filter);

    /**
     * Creates a {@link Flowable} for globally consuming all incoming Publish messages matching the given filter.
     * <p>
     * The returned {@link Flowable} represents the source of the incoming Publish messages matching the given type.
     * Calling this method does not start consuming yet. This is done lazy and asynchronous when subscribing (in terms
     * of Reactive Streams) to the returned {@link Flowable}.
     *
     * @param filter                the filter with which all incoming Publish messages are filtered.
     * @param manualAcknowledgement whether the Publish messages are acknowledged manually.
     * @return the {@link Flowable} which
     *         <ul>
     *           <li>emits the incoming Publish messages matching the given filter,
     *           <li>never completes but
     *           <li>errors with a {@link com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException
     *             MqttSessionExpiredException} when the MQTT session expires.
     *         </ul>
     * @see #publishes(MqttGlobalPublishFilter)
     * @since 1.2
     */
    @CheckReturnValue
    @NotNull Flowable<Mqtt3Publish> publishes(@NotNull MqttGlobalPublishFilter filter, boolean manualAcknowledgement);

    /**
     * Creates a {@link Completable} for unsubscribing this client with the given Unsubscribe message.
     * <p>
     * The returned {@link Completable} represents the source of the UnsubAck message corresponding to the given
     * Unsubscribe message. Calling this method does not unsubscribe yet. Unsubscribing is performed lazy and
     * asynchronous when subscribing (in terms of Reactive Streams) to the returned {@link Completable}.
     *
     * @param unsubscribe the Unsubscribe message sent to the broker during unsubscribe.
     * @return the {@link Completable} which
     *         <ul>
     *           <li>succeeds when the corresponding UnsubAck message was received or
     *           <li>errors if an error occurred before the Unsubscribe message was sent or before a UnsubAck message
     *             was received.
     *         </ul>
     */
    @CheckReturnValue
    @NotNull Completable unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

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
    @CheckReturnValue
    Mqtt3UnsubscribeBuilder.Nested.@NotNull Start<Completable> unsubscribeWith();

    /**
     * Creates a {@link Flowable} for publishing the Publish messages emitted by the given {@link Flowable}.
     * <p>
     * The returned {@link Flowable} represents the source of {@link Mqtt3PublishResult}s each corresponding to a
     * Publish message emitted by the given {@link Flowable}. Calling this method does not start publishing yet.
     * Publishing is performed lazy and asynchronous. When subscribing (in terms of Reactive Streams) to the returned
     * {@link Flowable} the client subscribes (in terms of Reactive Streams) to the given {@link Flowable}.
     *
     * @param publisher the source of the Publish messages to publish.
     * @return the {@link Flowable} which
     *         <ul>
     *           <li>emits {@link Mqtt3PublishResult}s each corresponding to a Publish message,
     *           <li>completes if the given {@link Flowable} completes, but not before all {@link Mqtt3PublishResult}s
     *             were emitted, or
     *           <li>errors with the same exception if the given {@link Flowable} errors, but not before all
     *             {@link Mqtt3PublishResult}s were emitted.
     *         </ul>
     */
    @CheckReturnValue
    @NotNull Flowable<Mqtt3PublishResult> publish(@NotNull Publisher<Mqtt3Publish> publisher);

    /**
     * Creates a {@link Completable} for disconnecting this client.
     * <p>
     * Calling this method does not disconnect yet. Disconnecting is performed lazy and asynchronous when subscribing
     * (in terms of Reactive Streams) to the returned {@link Completable}.
     *
     * @return the {@link Completable} which
     *         <ul>
     *           <li>completes when the client was successfully disconnected or
     *           <li>errors if not disconnected gracefully.
     *         </ul>
     */
    @CheckReturnValue
    @NotNull Completable disconnect();

    @Override
    @CheckReturnValue
    default @NotNull Mqtt3RxClient toRx() {
        return this;
    }
}
