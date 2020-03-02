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

package com.hivemq.client.mqtt.mqtt5.auth;

import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5Auth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for enhanced authentication and/or authorization (auth) mechanisms.
 * <p>
 * An enhanced auth mechanism object can be shared by different clients only if it does not store state and is
 * thread-safe.
 * <p>
 * The enhanced auth has two life cycles:
 * <ul>
 * <li>Auth when connecting:
 * <ol>
 * <li>{@link #onAuth}</li>
 * <li>({@link #onContinue})*</li>
 * <li>({@link #onAuthSuccess} | {@link
 * #onAuthRejected} | {@link #onAuthError})</li>
 * </ol>
 * </li>
 * <li>Reauth when connected:
 * <ol>
 * <li>({@link #onReAuth} | {@link #onServerReAuth})</li>
 * <li>({@link #onContinue})*</li>
 * <li>({@link #onReAuthSuccess} | {@link #onReAuthRejected} | {@link #onReAuthError})</li>
 * </ol>
 * </ul>
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public interface Mqtt5EnhancedAuthMechanism {

    /**
     * @return the method of this enhanced auth mechanism. This method MUST always return the same name.
     */
    @NotNull MqttUtf8String getMethod();

    /**
     * @return the maximum time interval in seconds between messages of the enhanced auth handshake (Connect, Auth,
     *         ConnAck, Disconnect). This method MUST always return the same value.
     */
    int getTimeout();

    /**
     * Called when a client connects using this enhanced auth mechanism.
     *
     * @param clientConfig the config of the client.
     * @param connect      the Connect message.
     * @param authBuilder  the builder for the outgoing Auth message.
     * @return a {@link CompletableFuture} succeeding when the required data for auth is added to the builder.
     */
    @NotNull CompletableFuture<Void> onAuth(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Connect connect,
            @NotNull Mqtt5EnhancedAuthBuilder authBuilder);

    /**
     * Called when a client reauthenticates and used this enhanced auth mechanism during connection.
     *
     * @param clientConfig the config of the client.
     * @param authBuilder  the builder for the outgoing Auth message.
     * @return a {@link CompletableFuture} succeeding when the required data for auth is added to the builder.
     */
    @NotNull CompletableFuture<Void> onReAuth(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5AuthBuilder authBuilder);

    /**
     * Called when a server reauthenticates a client and the client used this enhanced auth mechanism during
     * connection.
     * <p>
     * This is an addition to the MQTT 5 specification and so defaults to {@link #onReAuth(Mqtt5ClientConfig,
     * Mqtt5AuthBuilder)}. The feature must be explicitly enabled during client creation.
     *
     * @param clientConfig the config of the client.
     * @param auth         the Auth message sent by the server.
     * @param authBuilder  the builder for the outgoing Auth message.
     * @return a {@link CompletableFuture} succeeding with a boolean indicating whether the client accepts the auth step
     *         and when the required data for auth is added to the builder.
     */
    default @NotNull CompletableFuture<Boolean> onServerReAuth(
            final @NotNull Mqtt5ClientConfig clientConfig, final @NotNull Mqtt5Auth auth,
            final @NotNull Mqtt5AuthBuilder authBuilder) {

        return onReAuth(clientConfig, authBuilder).thenApply(aVoid -> true);
    }

    /**
     * Called when a server requires further data for auth from a client which used this enhanced auth mechanism during
     * connection.
     *
     * @param clientConfig the config of the client.
     * @param auth         the Auth message sent by the server.
     * @param authBuilder  the builder for the outgoing Auth message.
     * @return a {@link CompletableFuture} succeeding with a boolean indicating whether the client accepts the auth step
     *         and when the required data for auth is added to the builder.
     */
    @NotNull CompletableFuture<Boolean> onContinue(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Auth auth, @NotNull Mqtt5AuthBuilder authBuilder);

    /**
     * Called when a server accepted auth of a client which used this enhanced auth mechanism during connection.
     *
     * @param clientConfig the config of the client.
     * @param connAck      the ConnAck message sent by the server.
     * @return a {@link CompletableFuture} succeeding with a boolean indicating whether the client accepts the auth.
     */
    @NotNull CompletableFuture<Boolean> onAuthSuccess(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5ConnAck connAck);

    /**
     * Called when a server accepted reauth of a client which used this enhanced auth mechanism during connection.
     *
     * @param clientConfig the config of the client.
     * @param auth         the Auth message sent by the server.
     * @return a {@link CompletableFuture} succeeding with a boolean indicating whether the client accepts the reauth.
     */
    @NotNull CompletableFuture<Boolean> onReAuthSuccess(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Auth auth);

    /**
     * Called when a server rejected auth of a client which used this enhanced auth mechanism during connection.
     *
     * @param clientConfig the config of the client.
     * @param connAck      the ConnAck message sent by the server.
     */
    void onAuthRejected(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5ConnAck connAck);

    /**
     * Called when a server rejected reauth of a client which used this enhanced auth mechanism during connection.
     *
     * @param clientConfig the config of the client.
     * @param disconnect   the Disconnect message sent by the server.
     */
    void onReAuthRejected(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Disconnect disconnect);

    /**
     * Called when an error occurred during auth of a client which used this enhanced auth mechanism during connection.
     *
     * @param clientConfig the config of the client.
     * @param cause        the error.
     */
    void onAuthError(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Throwable cause);

    /**
     * Called when an error occurred during reauth of a client which used this enhanced auth mechanism during
     * connection.
     *
     * @param clientConfig the config of the client.
     * @param cause        the error.
     */
    void onReAuthError(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Throwable cause);
}
