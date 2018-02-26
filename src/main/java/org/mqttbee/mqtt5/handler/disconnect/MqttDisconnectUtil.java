package org.mqttbee.mqtt5.handler.disconnect;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.ChannelClosedException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.mqtt5.ioc.ChannelComponent;

/**
 * @author Silvio Giebl
 */
public class MqttDisconnectUtil {

    public static void close(@NotNull final Channel channel, @NotNull final Throwable cause) {
        fireChannelCloseEvent(channel, cause);
        channel.close();
    }

    public static void close(@NotNull final Channel channel, @NotNull final String reason) {
        close(channel, new ChannelClosedException(reason));
    }

    public static ChannelFuture disconnect(@NotNull final Channel channel, @NotNull final MqttDisconnect disconnect) {
        fireChannelCloseEvent(channel, new Mqtt5MessageException(disconnect, "DISCONNECT through API"));
        return disconnectAndClose(channel, disconnect);
    }

    public static void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final String reasonString) {

        getDisconnecter(channel).disconnect(channel, reasonCode, reasonString);
    }

    public static void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final Throwable cause) {

        getDisconnecter(channel).disconnect(channel, reasonCode, cause);
    }

    static void fireChannelCloseEvent(@NotNull final Channel channel, @NotNull final Throwable cause) {
        channel.config().setAutoRead(false);
        channel.pipeline().fireUserEventTriggered(new ChannelCloseEvent(cause));
    }

    static ChannelFuture disconnectAndClose(@NotNull final Channel channel, @NotNull final MqttDisconnect disconnect) {
        return channel.writeAndFlush(disconnect).addListener(ChannelFutureListener.CLOSE);
    }

    private static MqttDisconnecter getDisconnecter(@NotNull final Channel channel) {
        return ChannelComponent.get(channel).disconnecter();
    }

}
