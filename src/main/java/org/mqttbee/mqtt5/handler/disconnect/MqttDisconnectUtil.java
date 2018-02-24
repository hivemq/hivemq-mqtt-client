package org.mqttbee.mqtt5.handler.disconnect;

import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.ioc.ChannelComponent;

/**
 * @author Silvio Giebl
 */
public class MqttDisconnectUtil {

    private static MqttDisconnecter getDisconnecter(@NotNull final Channel channel) {
        return ChannelComponent.get(channel).disconnecter();
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

    public static void close(@NotNull final Channel channel, @NotNull final Throwable cause) {
        getDisconnecter(channel).close(channel, cause);
    }

    public static void close(@NotNull final Channel channel, @NotNull final String reason) {
        getDisconnecter(channel).close(channel, reason);
    }

}
