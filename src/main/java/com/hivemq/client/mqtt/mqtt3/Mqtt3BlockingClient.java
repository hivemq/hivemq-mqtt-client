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
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Blocking API of an {@link Mqtt3Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt3BlockingClient extends Mqtt3Client {

    /**
     * Connects this client with the default Connect message.
     *
     * @return see {@link #connect(Mqtt3Connect)}.
     * @see #connect(Mqtt3Connect)
     */
    @NotNull Mqtt3ConnAck connect();

    /**
     * Connects this client with the given Connect message.
     *
     * @param connect the Connect message sent to the broker.
     * @return the ConnAck message if it does not contain an Error Code (connected successfully).
     * @throws com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3ConnAckException wrapping the ConnAck message if it contains
     *                                                                       an Error Code.
     */
    @NotNull Mqtt3ConnAck connect(@NotNull Mqtt3Connect connect);

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
    Mqtt3ConnectBuilder.@NotNull Send<Mqtt3ConnAck> connectWith();

    /**
     * Subscribes this client with the given Subscribe message.
     * <p>
     * See {@link #publishes(MqttGlobalPublishFilter)} to consume the incoming Publish messages.
     *
     * @param subscribe the Subscribe messages sent to the broker.
     * @return the SubAck message if all subscriptions of the Subscribe message were successful (the SubAck message
     *         contains no Error Codes).
     * @throws com.hivemq.client.mqtt.mqtt3.exceptions.Mqtt3SubAckException wrapping the SubAck message if it contains
     *                                                                      at least one Error Code.
     */
    @NotNull Mqtt3SubAck subscribe(@NotNull Mqtt3Subscribe subscribe);

    /**
     * Fluent counterpart of {@link #subscribe(Mqtt3Subscribe)}.
     * <p>
     * Calling {@link Mqtt3SubscribeBuilder.Send.Complete#send()} on the returned builder has the same effect as calling
     * {@link #subscribe(Mqtt3Subscribe)} with the result of {@link Mqtt3SubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Subscribe message.
     * @see #subscribe(Mqtt3Subscribe)
     */
    @CheckReturnValue
    Mqtt3SubscribeBuilder.Send.@NotNull Start<Mqtt3SubAck> subscribeWith();

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter the filter with which all incoming Publish messages are filtered.
     * @return a {@link Publishes} instance that can be used to receive the Publish messages on the calling thread.
     * @see #publishes(MqttGlobalPublishFilter, boolean)
     */
    @NotNull Publishes publishes(final @NotNull MqttGlobalPublishFilter filter);

    /**
     * Globally consumes all incoming Publish messages matching the given filter.
     *
     * @param filter                the filter with which all incoming Publish messages are filtered.
     * @param manualAcknowledgement whether the Publish messages are acknowledged manually.
     * @return a {@link Publishes} instance that can be used to receive the Publish messages on the calling thread.
     * @see #publishes(MqttGlobalPublishFilter)
     * @since 1.2
     */
    @NotNull Publishes publishes(@NotNull MqttGlobalPublishFilter filter, boolean manualAcknowledgement);

    /**
     * Unsubscribes this client with the given Unsubscribe message.
     *
     * @param unsubscribe the Unsubscribe message sent to the broker.
     */
    void unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

    /**
     * Fluent counterpart of {@link #unsubscribe(Mqtt3Unsubscribe)}.
     * <p>
     * Calling {@link Mqtt3UnsubscribeBuilder.SendVoid.Complete#send()} on the returned builder has the same effect as
     * calling {@link #unsubscribe(Mqtt3Unsubscribe)} with the result of {@link Mqtt3UnsubscribeBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #unsubscribe(Mqtt3Unsubscribe)
     */
    @CheckReturnValue
    Mqtt3UnsubscribeBuilder.SendVoid.@NotNull Start unsubscribeWith();

    /**
     * Publishes the given Publish message.
     *
     * @param publish the Publish message sent to the broker.
     */
    void publish(@NotNull Mqtt3Publish publish);

    /**
     * Fluent counterpart of {@link #publish(Mqtt3Publish)}.
     * <p>
     * Calling {@link Mqtt3PublishBuilder.SendVoid.Complete#send()} on the returned builder has the same effect as
     * calling {@link #publish(Mqtt3Publish)} with the result of {@link Mqtt3PublishBuilder.Complete#build()}.
     *
     * @return the fluent builder for the Unsubscribe message.
     * @see #publish(Mqtt3Publish)
     */
    @CheckReturnValue
    Mqtt3PublishBuilder.@NotNull SendVoid publishWith();

    /**
     * Disconnects this client with the given Disconnect message.
     */
    void disconnect();

    @Override
    @CheckReturnValue
    default @NotNull Mqtt3BlockingClient toBlocking() {
        return this;
    }

    /**
     * Resource which queues incoming Publish messages until they are received.
     */
    @DoNotImplement
    interface Publishes extends AutoCloseable {

        /**
         * Receives the next incoming Publish message.
         * <ul>
         *   <li>Might return immediately if there is already a Publish message queued in this {@link Publishes}
         *     instance.
         *   <li>Otherwise blocks the calling thread until a Publish message is received.
         * </ul>
         *
         * @return the received Publish message.
         * @throws InterruptedException if the calling thread is interrupted while waiting for a Publish message to be
         *                              received.
         */
        @NotNull Mqtt3Publish receive() throws InterruptedException;

        /**
         * Receives the next incoming Publish message.
         * <ul>
         *   <li>Might return immediately if there is already a Publish message queued in this {@link Publishes}
         *     instance.
         *   <li>Otherwise blocks the calling thread until a Publish message is received or the given timeout applies.
         * </ul>
         *
         * @param timeout  the time to wait for a Publish messages to be received.
         * @param timeUnit the time unit of the timeout parameter.
         * @return an {@link Optional} containing the received Publish message, or empty if no Publish message was
         *         received in the given timeout period.
         * @throws InterruptedException if the calling thread is interrupted while waiting for a Publish message to be
         *                              received.
         */
        @NotNull Optional<Mqtt3Publish> receive(final long timeout, final @NotNull TimeUnit timeUnit)
                throws InterruptedException;

        /**
         * Receives the next incoming Publish message if it is already queued in this {@link Publishes} instance.
         *
         * @return an {@link Optional} containing the already queued Publish message, or empty if no Publish message was
         *         already queued.
         */
        @NotNull Optional<Mqtt3Publish> receiveNow();

        @Override
        void close();
    }
}
