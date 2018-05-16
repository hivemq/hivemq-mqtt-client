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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.ChannelClosedException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.ioc.ChannelComponent;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;

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
        fireChannelCloseEvent(channel, cause, false);
        channel.close();
    }

    /**
     * Closes the channel from the client side without sending a DISCONNECT message.
     *
     * @param channel the channel to close.
     * @param reason  the reason why the channel is closed.
     */
    public static void close(@NotNull final Channel channel, @NotNull final String reason) {
        close(channel, new ChannelClosedException(reason));
    }

    /**
     * Disconnects the client through the API.
     *
     * @param channel    the channel to close.
     * @param disconnect the DISCONNECT message to send.
     * @return the channel future succeeding when the DISCONNECT message is written.
     */
    public static ChannelFuture disconnect(@NotNull final Channel channel, @NotNull final MqttDisconnect disconnect) {
        fireChannelCloseEvent(channel, new Mqtt5MessageException(disconnect, "DISCONNECT through API"), false);
        return disconnectAndClose(channel, disconnect);
    }

    /**
     * @see MqttDisconnecter#disconnect(Channel, Mqtt5DisconnectReasonCode, String).
     */
    public static void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final String reasonString) {

        getDisconnecter(channel).disconnect(channel, reasonCode, reasonString);
    }

    /**
     * @see MqttDisconnecter#disconnect(Channel, Mqtt5DisconnectReasonCode, Throwable).
     */
    public static void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final Throwable cause) {

        getDisconnecter(channel).disconnect(channel, reasonCode, cause);
    }

    static void fireChannelCloseEvent(
            @NotNull final Channel channel, @NotNull final Throwable cause, final boolean fromServer) {

        channel.config().setAutoRead(false);
        channel.pipeline().fireUserEventTriggered(new ChannelCloseEvent(cause, fromServer));
    }

    static ChannelFuture disconnectAndClose(@NotNull final Channel channel, @NotNull final MqttDisconnect disconnect) {
        return channel.writeAndFlush(disconnect).addListener(ChannelFutureListener.CLOSE);
    }

    private static MqttDisconnecter getDisconnecter(@NotNull final Channel channel) {
        return ChannelComponent.get(channel).disconnecter();
    }

}
