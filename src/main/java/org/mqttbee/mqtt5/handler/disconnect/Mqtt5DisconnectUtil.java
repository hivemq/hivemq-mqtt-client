package org.mqttbee.mqtt5.handler.disconnect;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.exception.ChannelClosedException;
import org.mqttbee.api.mqtt5.exception.Mqtt5MessageException;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5DisconnectEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl;
import org.mqttbee.mqtt5.netty.ChannelAttributes;

/**
 * Util for sending a DISCONNECT message and channel closing from the client side. Fires {@link ChannelCloseEvent}s.
 *
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectUtil {

    private static void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @Nullable final String reasonString, @Nullable final Throwable cause) {

        MqttUTF8StringImpl mqttReasonString = null;
        if ((reasonString != null) && ChannelAttributes.sendReasonString(channel)) {
            mqttReasonString = MqttUTF8StringImpl.from(reasonString);
        }

        final MqttDisconnectImpl disconnect =
                new MqttDisconnectImpl(reasonCode, MqttDisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null,
                        mqttReasonString, MqttUserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5DisconnectEncoder.PROVIDER);

        channel.config().setAutoRead(false);
        channel.pipeline()
                .fireUserEventTriggered(
                        new ChannelCloseEvent(new Mqtt5MessageException(disconnect, reasonString, cause)));
        channel.writeAndFlush(disconnect).addListener(ChannelFutureListener.CLOSE);
    }

    public static void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final String reasonString) {

        disconnect(channel, reasonCode, reasonString, null);
    }

    public static void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final Throwable cause) {

        disconnect(channel, reasonCode, cause.getMessage(), cause);
    }

    public static void close(@NotNull final Channel channel, @NotNull final Throwable cause) {
        channel.config().setAutoRead(false);
        channel.pipeline().fireUserEventTriggered(new ChannelCloseEvent(cause));
        channel.close();
    }

    public static void close(@NotNull final Channel channel, @NotNull final String reason) {
        close(channel, new ChannelClosedException(reason));
    }

}
