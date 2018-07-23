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
import io.reactivex.CompletableEmitter;
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
 * Fires {@link ChannelCloseEvent}s.
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
    public static void close(@NotNull final Channel channel, @NotNull final Throwable cause) {
        fireChannelCloseEvent(channel, cause);
    }

    /**
     * Closes the channel from the client side without sending a DISCONNECT message.
     *
     * @param channel the channel to close.
     * @param reason  the reason why the channel is closed.
     */
    public static void close(@NotNull final Channel channel, @NotNull final String reason) {
        fireChannelCloseEvent(channel, new ChannelClosedException(reason));
    }

    /**
     * Disconnects the client through the API.
     *
     * @param channel            the channel to close.
     * @param disconnect         the DISCONNECT message to send.
     * @param completableEmitter the emitter to indicate success or error.
     */
    public static void disconnect(
            @NotNull final Channel channel, @NotNull final MqttDisconnect disconnect,
            @NotNull final CompletableEmitter completableEmitter) {

        final Throwable cause = new Mqtt5MessageException(disconnect, "DISCONNECT through API");
        fireChannelCloseEvent(channel, new ChannelCloseEvent(cause, true, completableEmitter));
    }

    /**
     * Decides whether a DISCONNECT message is sent before closing a channel from the client side.
     *
     * @param channel      the channel to close.
     * @param reasonCode   the reason code why the channel is closed.
     * @param reasonString the reason string why the channel is closed.
     */
    public static void disconnect(
            @NotNull final Channel channel, @NotNull final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final String reasonString) {

        final MqttDisconnect disconnect = createDisconnect(channel, reasonCode, reasonString);
        fireChannelCloseEvent(channel, new Mqtt5MessageException(disconnect, reasonString));
    }

    /**
     * Decides whether a DISCONNECT message is sent before closing a channel from the client side.
     *
     * @param channel    the channel to close.
     * @param reasonCode the reason code why the channel is closed.
     * @param cause      the cause why the channel is closed.
     */
    public static void disconnect(
            @NotNull final Channel channel, @NotNull final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final Throwable cause) {

        final MqttDisconnect disconnect = createDisconnect(channel, reasonCode, cause.getMessage());
        fireChannelCloseEvent(channel, new Mqtt5MessageException(disconnect, cause));
    }

    private static void fireChannelCloseEvent(@NotNull final Channel channel, @NotNull final Throwable cause) {
        fireChannelCloseEvent(channel, new ChannelCloseEvent(cause, true, null));
    }

    static void fireChannelCloseEvent(
            @NotNull final Channel channel, @NotNull final ChannelCloseEvent channelCloseEvent) {

        channel.config().setAutoRead(false);
        channel.pipeline().fireUserEventTriggered(channelCloseEvent);
    }

    @NotNull
    private static MqttDisconnect createDisconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @Nullable final String reasonString) {

        MqttUTF8StringImpl mqttReasonString = null;
        if ((reasonString != null) && ChannelAttributes.sendReasonString(channel)) {
            mqttReasonString = MqttUTF8StringImpl.from(reasonString);
        }

        return new MqttDisconnect(reasonCode, MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null,
                mqttReasonString, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

}
