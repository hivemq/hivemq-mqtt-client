package org.mqttbee.mqtt5.handler.disconnect;

import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.ChannelClosedException;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

/**
 * @author Silvio Giebl
 */
public interface MqttDisconnecter {

    default void close(@NotNull final Channel channel, @NotNull final Throwable cause) {
        channel.config().setAutoRead(false);
        channel.pipeline().fireUserEventTriggered(new ChannelCloseEvent(cause));
        channel.close();
    }

    default void close(@NotNull final Channel channel, @NotNull final String reason) {
        close(channel, new ChannelClosedException(reason));
    }

    void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final String reasonString);

    void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode, @NotNull final Throwable cause);

}
