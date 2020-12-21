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

package com.hivemq.client2.internal.mqtt.handler.disconnect;

import com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnectBuilder;
import com.hivemq.client2.mqtt.exceptions.ConnectionClosedException;
import com.hivemq.client2.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client2.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;

/**
 * Util for sending a DISCONNECT message or channel closing without sending a DISCONNECT message from the client side.
 * Fires {@link MqttDisconnectEvent}s.
 *
 * @author Silvio Giebl
 */
public final class MqttDisconnectUtil {

    /**
     * Closes the channel from the client side without sending a DISCONNECT message.
     *
     * @param channel the channel to close.
     * @param reason  the reason why the channel is closed.
     */
    public static void close(final @NotNull Channel channel, final @NotNull String reason) {
        fireDisconnectEvent(channel, new ConnectionClosedException(reason), MqttDisconnectSource.CLIENT);
    }

    /**
     * Closes the channel from the client side without sending a DISCONNECT message.
     *
     * @param channel the channel to close.
     * @param cause   the cause why the channel is closed.
     */
    public static void close(final @NotNull Channel channel, final @NotNull Throwable cause) {
        fireDisconnectEvent(channel, cause, MqttDisconnectSource.CLIENT);
    }

    /**
     * Decides whether a DISCONNECT message is sent before closing a channel from the client side.
     *
     * @param channel      the channel to close.
     * @param reasonCode   the reason code why the channel is closed.
     * @param reasonString the reason string why the channel is closed.
     */
    public static void disconnect(
            final @NotNull Channel channel,
            final @NotNull Mqtt5DisconnectReasonCode reasonCode,
            final @NotNull String reasonString) {

        final MqttDisconnect disconnect =
                new MqttDisconnectBuilder.Default().reasonCode(reasonCode).reasonString(reasonString).build();
        fireDisconnectEvent(
                channel, new Mqtt5DisconnectException(disconnect, reasonString), MqttDisconnectSource.CLIENT);
    }

    /**
     * Decides whether a DISCONNECT message is sent before closing a channel from the client side.
     *
     * @param channel    the channel to close.
     * @param reasonCode the reason code why the channel is closed.
     * @param cause      the cause why the channel is closed.
     */
    public static void disconnect(
            final @NotNull Channel channel,
            final @NotNull Mqtt5DisconnectReasonCode reasonCode,
            final @NotNull Throwable cause) {

        final MqttDisconnect disconnect =
                new MqttDisconnectBuilder.Default().reasonCode(reasonCode).reasonString(cause.getMessage()).build();
        fireDisconnectEvent(channel, new Mqtt5DisconnectException(disconnect, cause), MqttDisconnectSource.CLIENT);
    }

    public static void fireDisconnectEvent(
            final @NotNull Channel channel,
            final @NotNull Throwable cause,
            final @NotNull MqttDisconnectSource source) {

        fireDisconnectEvent(channel, new MqttDisconnectEvent(cause, source));
    }

    static void fireDisconnectEvent(
            final @NotNull Channel channel, final @NotNull MqttDisconnectEvent disconnectEvent) {

        channel.pipeline().fireUserEventTriggered(disconnectEvent);
    }

    private MqttDisconnectUtil() {}
}
