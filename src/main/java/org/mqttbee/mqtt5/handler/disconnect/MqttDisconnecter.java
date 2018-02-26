package org.mqttbee.mqtt5.handler.disconnect;

import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

/**
 * MQTT disconnecter which decides whether a DISCONNECT message is sent before closing a channel from the client side.
 * Fires {@link ChannelCloseEvent}s.
 *
 * @author Silvio Giebl
 */
public interface MqttDisconnecter {

    /**
     * Decides whether a DISCONNECT message is sent before closing a channel from the client side.
     *
     * @param channel      the channel to close.
     * @param reasonCode   the reason code why the channel is closed.
     * @param reasonString the reason string why the channel is closed.
     */
    void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final String reasonString);

    /**
     * Decides whether a DISCONNECT message is sent before closing a channel from the client side.
     *
     * @param channel    the channel to close.
     * @param reasonCode the reason code why the channel is closed.
     * @param cause      the cause why the channel is closed.
     */
    void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode, @NotNull final Throwable cause);

}
