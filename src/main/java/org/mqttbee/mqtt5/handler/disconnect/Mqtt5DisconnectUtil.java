package org.mqttbee.mqtt5.handler.disconnect;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.exception.Mqtt5MessageException;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5DisconnectEncoder;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;
import org.mqttbee.mqtt5.netty.ChannelAttributes;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectUtil {

    private static void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @Nullable final String reasonString, @Nullable final Throwable cause) {

        Mqtt5UTF8StringImpl mqttReasonString = null;
        if ((reasonString != null) && ChannelAttributes.sendReasonString(channel)) {
            mqttReasonString = Mqtt5UTF8StringImpl.from(reasonString);
        }

        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(reasonCode, Mqtt5DisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null,
                        mqttReasonString, Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES, Mqtt5DisconnectEncoder.PROVIDER);

        channel.config().setAutoRead(false);
        channel.pipeline().fireUserEventTriggered(new Mqtt5MessageException(disconnect, reasonString, cause));
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

    public static void close(@NotNull final Throwable cause, @NotNull final Channel channel) {
        channel.config().setAutoRead(false);
        channel.pipeline().fireUserEventTriggered(new ChannelCloseEvent(cause));
        channel.close();
    }

}
