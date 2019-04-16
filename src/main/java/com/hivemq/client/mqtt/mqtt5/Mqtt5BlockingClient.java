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
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribeBuilder;
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
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubscribeBuilder;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Blocking API of a {@link Mqtt5Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5BlockingClient extends Mqtt5Client {

    /**
     * Connects this client with the default Connect message.
     *
     * @return see {@link #connect(Mqtt5Connect)}.
     * @see #connect(Mqtt5Connect)
     */
    default @NotNull Mqtt5ConnAck connect() {
        return connect(MqttConnect.DEFAULT);
    }

    /**
     * Connects this client with the given Connect message.
     *
     * @param connect the Connect message sent to the broker.
     * @return the ConnAck message if it does not contain an Error Code (connected successfully).
     * @throws com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException wrapping the ConnAck message if it contains
     *                                                                       an Error Code.
     */
    @NotNull Mqtt5ConnAck connect(@NotNull Mqtt5Connect connect);

    /**
     * Fluent counterpart of {@link #connect(Mqtt5Connect)}.
     * <p>
     * Calling {@link Mqtt5ConnectBuilder.Send#send()} on the returned builder has the same effect as calling {@link
     * #connect(Mqtt5Connect)} with the result of {@link Mqtt5ConnectBuilder#build()}.
     *
     * @return the fluent builder for the Connect message.
     * @see #connect(Mqtt5Connect)
     */
    default @NotNull Mqtt5ConnectBuilder.Send<Mqtt5ConnAck> connectWith() {
        return new MqttConnectBuilder.Send<>(this::connect);
    }

    /**
     * Subscribes this client with the given Subscribe message.
     * <p>
     * See {@link #publishes(MqttGlobalPublishFilter)} to consume the incoming Publish messages.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @return the SubAck message if all subscriptions of the Subscribe message were successful (the SubAck message
     *         contains no Error Codes).
     * @throws com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException wrapping the SubAck message if it contains
     *                                                                      at least one Error Code.
     */
    @NotNull Mqtt5SubAck subscribe(@NotNull Mqtt5Subscribe subscribe);

    /**
     * Fluent counterpart of {@link #subscribe(Mqtt5Subscribe)}.
     * <p>
     * Calling {@link Mqtt5SubscribeBuilder.Send.Complete#send()} on the returned builder has the same effect as calling
     * {@link #subscribe(Mqtt5Subscribe)} with the result of {@link Mqtt5SubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribe(Mqtt5Subscribe)
     */
    default @NotNull Mqtt5SubscribeBuilder.Send.Start<Mqtt5SubAck> subscribeWith() {
        return new MqttSubscribeBuilder.Send<>(this::subscribe);
    }

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter the filter with which all incoming Publish messages are filtered.
     * @return a {@link Mqtt5Publishes} instance that can be used to receive the Publish messages on the calling
     *         thread.
     */
    @NotNull Mqtt5Publishes publishes(@NotNull MqttGlobalPublishFilter filter);

    /**
     * Unsubscribes this client with the given Unsubscribe message.
     *
     * @param unsubscribe the Unsubscribe message sent to the broker.
     * @return the UnsubAck message if all Topic Filters of the Unsubscribe message were successfully unsubscribed (the
     *         UnsubAck message contains no Error Codes).
     * @throws com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5UnsubAckException wrapping the UnsubAck message if it
     *                                                                        contains at least one Error Code.
     */
    @NotNull Mqtt5UnsubAck unsubscribe(@NotNull Mqtt5Unsubscribe unsubscribe);

    /**
     * Fluent counterpart of {@link #unsubscribe(Mqtt5Unsubscribe)}.
     * <p>
     * Calling {@link Mqtt5UnsubscribeBuilder.Send.Complete#send()} on the returned builder has the same effect as
     * calling {@link #unsubscribe(Mqtt5Unsubscribe)} with the result of {@link Mqtt5UnsubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #unsubscribe(Mqtt5Unsubscribe)
     */
    default @NotNull Mqtt5UnsubscribeBuilder.Send.Start<Mqtt5UnsubAck> unsubscribeWith() {
        return new MqttUnsubscribeBuilder.Send<>(this::unsubscribe);
    }

    /**
     * Publishes the given Publish message.
     *
     * @param publish the Publish message sent to the broker.
     * @return the {@link Mqtt5PublishResult} if the Publish message was successfully published (no acknowledgement
     *         message contains an Error Code, {@link Mqtt5PublishResult#getError()} will always be absent).
     * @throws com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubAckException wrapping the corresponding PubAck message if
     *                                                                      it contains an Error Code.
     * @throws com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubRecException wrapping the corresponding PubRec message if
     *                                                                      it contains an Error Code.
     */
    @NotNull Mqtt5PublishResult publish(@NotNull Mqtt5Publish publish);

    /**
     * Fluent counterpart of {@link #publish(Mqtt5Publish)}.
     * <p>
     * Calling {@link Mqtt5PublishBuilder.Send.Complete#send()} on the returned builder has the same effect as calling
     * {@link #publish(Mqtt5Publish)} with the result of {@link Mqtt5PublishBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #publish(Mqtt5Publish)
     */
    default @NotNull Mqtt5PublishBuilder.Send<Mqtt5PublishResult> publishWith() {
        return new MqttPublishBuilder.Send<>(this::publish);
    }

    /**
     * Re-authenticates this client.
     *
     * @throws com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5AuthException wrapping the Auth message with the Error Code
     *                                                                    if not re-authenticated successfully.
     */
    void reauth();

    /**
     * Disconnects this client with the default Disconnect message.
     *
     * @see #disconnect(Mqtt5Disconnect)
     */
    default void disconnect() {
        disconnect(MqttDisconnect.DEFAULT);
    }

    /**
     * Disconnects this client with the given Disconnect message.
     *
     * @param disconnect the Disconnect message sent to the broker.
     */
    void disconnect(@NotNull Mqtt5Disconnect disconnect);

    /**
     * Fluent counterpart of {@link #disconnect(Mqtt5Disconnect)}.
     * <p>
     * Calling {@link Mqtt5DisconnectBuilder.SendVoid#send()} on the returned builder has the same effect as calling
     * {@link #disconnect(Mqtt5Disconnect)} with the result of {@link Mqtt5DisconnectBuilder#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #disconnect(Mqtt5Disconnect)
     */
    default @NotNull Mqtt5DisconnectBuilder.SendVoid disconnectWith() {
        return new MqttDisconnectBuilder.SendVoid(this::disconnect);
    }

    @Override
    default @NotNull Mqtt5BlockingClient toBlocking() {
        return this;
    }

    /**
     * Resource which queues incoming Publish messages until they are received.
     */
    @DoNotImplement
    interface Mqtt5Publishes extends AutoCloseable {

        /**
         * Receives the next incoming Publish message.
         * <ul>
         * <li>Might return immediately if there is already a Publish message queued in this {@link Mqtt5Publishes}
         * instance.</li>
         * <li>Otherwise blocks the calling thread until a Publish message is received.</li>
         * </ul>
         *
         * @return the received Publish message.
         * @throws InterruptedException if the calling thread is interrupted while waiting for a Publish message to be
         *                              received.
         */
        @NotNull Mqtt5Publish receive() throws InterruptedException;

        /**
         * Receives the next incoming Publish message.
         * <ul>
         * <li>Might return immediately if there is already a Publish message queued in this {@link Mqtt5Publishes}
         * instance.</li>
         * <li>Otherwise blocks the calling thread until a Publish message is received or the given timeout
         * applies.</li>
         * </ul>
         *
         * @param timeout  the time to wait for a Publish messages to be received.
         * @param timeUnit the time unit of the timeout parameter.
         * @return an {@link Optional} containing the received Publish message, or empty if no Publish message was
         *         received in the given timeout period.
         * @throws InterruptedException if the calling thread is interrupted while waiting for a Publish message to be
         *                              received.
         */
        @NotNull Optional<Mqtt5Publish> receive(final long timeout, final @NotNull TimeUnit timeUnit)
                throws InterruptedException;

        /**
         * Receives the next incoming Publish message if it is already queued in this {@link Mqtt5Publishes} instance.
         *
         * @return an {@link Optional} containing the already queued Publish message, or empty if no Publish message was
         *         already queued.
         */
        @NotNull Optional<Mqtt5Publish> receiveNow();

        @Override
        void close();
    }
}
