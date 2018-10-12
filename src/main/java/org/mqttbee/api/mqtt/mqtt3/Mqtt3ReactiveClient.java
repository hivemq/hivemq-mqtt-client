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

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttGlobalPublishFlowType;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import org.mqttbee.rx.FlowableWithSingle;

/**
 * MQTT 3 client with a reactive API.
 *
 * @author Silvio Giebl
 */
public interface Mqtt3ReactiveClient extends Mqtt3Client {

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
     *         <li>errors with an {@link org.mqttbee.api.mqtt.mqtt3.exceptions.Mqtt3MessageException
     *         Mqtt3MessageException} wrapping the ConnAck message if it contains an Error Code or</li>
     *         <li>errors with a different exception if an error occurred before the Connect message was sent or before
     *         a ConnAck message was received.</li>
     *         </ul>
     */
    @NotNull Single<Mqtt3ConnAck> connect(@NotNull Mqtt3Connect connect);

    /**
     * Creates a {@link Mqtt3ConnectBuilder} for connecting this client with the Connect message built from the returned
     * builder.
     * <p>
     * Calling {@link Mqtt3ConnectBuilder#done()} has the same effect as calling {@link #connect(Mqtt3Connect)} with the
     * result of {@link Mqtt3ConnectBuilder#build()}.
     *
     * @return the builder for the Connect message.
     * @see #connect(Mqtt3Connect)
     */
    default @NotNull Mqtt3ConnectBuilder<Single<Mqtt3ConnAck>> connect() {
        return new Mqtt3ConnectBuilder<>(this::connect);
    }

    /**
     * Creates a {@link Single} for subscribing this client with the given Subscribe message.
     * <p>
     * The returned {@link Single} represents the source of the SubAck message corresponding to the given Subscribe
     * message. Calling this method does not subscribe yet. Subscribing is performed lazy and asynchronous when
     * subscribing (in terms of Reactive Streams) to the returned {@link Single}.
     * <p>
     * See {@link #publishes(MqttGlobalPublishFlowType)} to consume the Publish messages. Alternatively, call {@link
     * #subscribeWithStream(Mqtt3Subscribe)} to consume the Publish messages matching the subscriptions of the Subscribe
     * message directly.
     *
     * @param subscribe the Subscribe message sent to the broker during subscribe.
     * @return the {@link Single} which
     *         <ul>
     *         <li>succeeds with the SubAck message if at least one subscription of the Subscribe message was
     *         successful (the SubAck message contains at least one Reason Code that is not an Error Code),</li>
     *         <li>errors with an {@link org.mqttbee.api.mqtt.mqtt3.exceptions.Mqtt3MessageException
     *         Mqtt3MessageException} wrapping the SubAck message if it only contains Error Codes or</li>
     *         <li>errors with a different exception if an error occurred before the Subscribe message was sent or
     *         before a SubAck message was received.</li>
     *         </ul>
     */
    @NotNull Single<Mqtt3SubAck> subscribe(@NotNull Mqtt3Subscribe subscribe);

    /**
     * Creates a {@link Mqtt3SubscribeBuilder} for subscribing this client with the Subscribe message built from the
     * returned builder.
     * <p>
     * Calling {@link Mqtt3SubscribeBuilder#done()} has the same effect as calling {@link #subscribe(Mqtt3Subscribe)}
     * with the result of {@link Mqtt3SubscribeBuilder#build()}.
     *
     * @return the builder for the Subscribe message.
     * @see #subscribe(Mqtt3Subscribe)
     */
    default @NotNull Mqtt3SubscribeBuilder<Single<Mqtt3SubAck>> subscribe() {
        return new Mqtt3SubscribeBuilder<>(this::subscribe);
    }

    /**
     * Creates a {@link FlowableWithSingle} for subscribing this client with the given Subscribe message.
     * <p>
     * The returned {@link FlowableWithSingle} represents the source of the SubAck message corresponding to the given
     * Subscribe message and the source of the Publish messages matching the subscriptions of the Subscribe message.
     * Calling this method does not subscribe yet. Subscribing is performed lazy and asynchronous when subscribing (in
     * terms of Reactive Streams) to the returned {@link FlowableWithSingle}.
     *
     * @param subscribe the Subscribe message sent to the broker during subscribe.
     * @return the {@link FlowableWithSingle} which
     *         <ul>
     *         <li>emits the SubAck message as the single and first element if at least one subscription of the
     *         Subscribe message was successful (the SubAck message contains at least one Reason Code that is not an
     *         Error Code) and then emits the Publish messages matching the successful subscriptions of the Subscribe
     *         message,</li>
     *         <li>completes when all subscriptions of the Subscribe message were unsubscribed,</li>
     *         <li>errors with an {@link org.mqttbee.api.mqtt.mqtt3.exceptions.Mqtt3MessageException
     *         Mqtt3MessageException} wrapping the SubAck message if it only contains Error Codes or</li>
     *         <li>errors with a different exception if an error occurred before the Subscribe message was sent,
     *         before a SubAck message was received or when a error occurs before all subscriptions or the Subscribe
     *         messages were unsubscribed.</li>
     *         </ul>
     */
    @NotNull FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck> subscribeWithStream(@NotNull Mqtt3Subscribe subscribe);

    /**
     * Creates a {@link Mqtt3SubscribeBuilder} for subscribing this client with the Subscribe message built from the
     * returned builder.
     * <p>
     * Calling {@link Mqtt3SubscribeBuilder#done()} has the same effect as calling {@link
     * #subscribeWithStream(Mqtt3Subscribe)} with the result of {@link Mqtt3SubscribeBuilder#build()}.
     *
     * @return the builder for the Subscribe message.
     * @see #subscribeWithStream(Mqtt3Subscribe)
     */
    default @NotNull Mqtt3SubscribeBuilder<FlowableWithSingle<Mqtt3Publish, Mqtt3SubAck>> subscribeWithStream() {
        return new Mqtt3SubscribeBuilder<>(this::subscribeWithStream);
    }

    /**
     * Creates a {@link Flowable} for globally consuming all Publish messages matching the given type received by this
     * client.
     * <p>
     * The returned {@link Flowable} represents the source of the incoming Publish messages matching the given type.
     * Calling this method does not start consuming yet. This is done lazy and asynchronous when subscribing (in terms
     * of Reactive Streams) to the returned {@link Flowable}.
     *
     * @param type the type of the returned flow of Publish messages.
     * @return the {@link Flowable} which
     *         <ul>
     *         <li>emits the incoming Publish messages matching the given type and</li>
     *         <li>completes when this client is disconnected.</li>
     *         </ul>
     */
    @NotNull Flowable<Mqtt3Publish> publishes(@NotNull MqttGlobalPublishFlowType type);

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
     *         Code)</li>
     *         <li>errors with an {@link org.mqttbee.api.mqtt.mqtt3.exceptions.Mqtt3MessageException
     *         Mqtt3MessageException} wrapping the UnsubAck message if it only contains Error Codes or</li>
     *         <li>errors with a different exception if an error occurred before the Unsubscribe message was sent or
     *         before a UnsubAck message was received.</li>
     *         </ul>
     */
    @NotNull Completable unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

    /**
     * Creates a {@link Mqtt3UnsubscribeBuilder} for unsubscribing this client with the Unsubscribe message built from
     * the returned builder.
     * <p>
     * Calling {@link Mqtt3UnsubscribeBuilder#done()} has the same effect as calling {@link
     * #unsubscribe(Mqtt3Unsubscribe)} with the result of {@link Mqtt3UnsubscribeBuilder#build()}.
     *
     * @return the builder for the Unsubscribe message.
     * @see #unsubscribe(Mqtt3Unsubscribe)
     */
    default @NotNull Mqtt3UnsubscribeBuilder<Completable> unsubscribe() {
        return new Mqtt3UnsubscribeBuilder<>(this::unsubscribe);
    }

    /**
     * Creates a {@link Flowable} for publishing the Publish messages emitted by the given {@link Flowable}.
     * <p>
     * The returned {@link Flowable} represents the source of {@link Mqtt3PublishResult}s each corresponding to a
     * Publish message emitted by the given {@link Flowable}. Calling this method does not start publishing yet.
     * Publishing is performed lazy and asynchronous. When subscribing (in terms of Reactive Streams) to the returned
     * {@link Flowable} the client subscribes (in terms of Reactive Streams) to the given {@link Flowable}.
     *
     * @param publishFlowable the source of the Publish messages to publish.
     * @return the {@link Flowable} which
     *         <ul>
     *         <li>emits {@link Mqtt3PublishResult}s each corresponding to a Publish message,</li>
     *         <li>completes when the given {@link Flowable} completes or</li>
     *         <li>errors with the same exception when the given {@link Flowable} errors.</li>
     *         </ul>
     */
    @NotNull Flowable<Mqtt3PublishResult> publish(@NotNull Flowable<Mqtt3Publish> publishFlowable);

    /**
     * Creates a {@link Completable} for disconnecting this client.
     * <p>
     * Calling this method does not disconnect yet. Disconnecting is performed lazy and asynchronous when subscribing
     * (in terms of Reactive Streams) to the returned {@link Completable}.
     *
     * @return the {@link Completable} which
     *         <ul>
     *         <li>completes when the client was successfully disconnected or</li>
     *         <li>errors if not disconnected successfully.</li>
     *         </ul>
     */
    @NotNull Completable disconnect();

}
