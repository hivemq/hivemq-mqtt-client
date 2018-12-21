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

package org.mqttbee.mqtt.handler.disconnect;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.exceptions.ChannelClosedException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.mqtt.netty.ChannelAttributes;

/**
 * Util for sending a DISCONNECT message or channel closing without sending a DISCONNECT message from the client side.
 * Fires {@link MqttDisconnectEvent}s.
 *
 * @author Silvio Giebl
 */
public class MqttDisconnectUtil {

    /**
     * Closes the channel from the client side without sending a DISCONNECT message.
     *
     * @param channel the channel to close.
     * @param cause   the cause why the channel is closed.
     */
    public static void close(final @NotNull Channel channel, final @NotNull Throwable cause) {
        fireDisconnectEvent(channel, cause, true);
    }

    /**
     * Closes the channel from the client side without sending a DISCONNECT message.
     *
     * @param channel the channel to close.
     * @param reason  the reason why the channel is closed.
     */
    public static void close(final @NotNull Channel channel, final @NotNull String reason) {
        fireDisconnectEvent(channel, new ChannelClosedException(reason), true);
    }

    /**
     * Decides whether a DISCONNECT message is sent before closing a channel from the client side.
     *
     * @param channel      the channel to close.
     * @param reasonCode   the reason code why the channel is closed.
     * @param reasonString the reason string why the channel is closed.
     */
    public static void disconnect(
            final @NotNull Channel channel, final @NotNull Mqtt5DisconnectReasonCode reasonCode,
            final @NotNull String reasonString) {

        final MqttDisconnect disconnect = createDisconnect(channel, reasonCode, reasonString);
        fireDisconnectEvent(channel, new Mqtt5MessageException(disconnect, reasonString), true);
    }

    /**
     * Decides whether a DISCONNECT message is sent before closing a channel from the client side.
     *
     * @param channel    the channel to close.
     * @param reasonCode the reason code why the channel is closed.
     * @param cause      the cause why the channel is closed.
     */
    public static void disconnect(
            final @NotNull Channel channel, final @NotNull Mqtt5DisconnectReasonCode reasonCode,
            final @NotNull Throwable cause) {

        final MqttDisconnect disconnect = createDisconnect(channel, reasonCode, cause.getMessage());
        fireDisconnectEvent(channel, new Mqtt5MessageException(disconnect, cause), true);
    }

    static void fireDisconnectEvent(
            final @NotNull Channel channel, final @NotNull Throwable cause, final boolean fromClient) {

        fireDisconnectEvent(channel, new MqttDisconnectEvent(cause, fromClient));
    }

    static void fireDisconnectEvent(
            final @NotNull Channel channel, final @NotNull MqttDisconnectEvent disconnectEvent) {

        channel.config().setAutoRead(false);
        channel.pipeline().fireUserEventTriggered(disconnectEvent);
    }

    private static @NotNull MqttDisconnect createDisconnect(
            final @NotNull Channel channel, final @NotNull Mqtt5DisconnectReasonCode reasonCode,
            final @Nullable String reasonString) {

        MqttUTF8StringImpl mqttReasonString = null;
        if ((reasonString != null) && ChannelAttributes.sendReasonString(channel)) {
            mqttReasonString = MqttUTF8StringImpl.from(reasonString);
        }

        return new MqttDisconnect(reasonCode, MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null,
                mqttReasonString, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }
}
