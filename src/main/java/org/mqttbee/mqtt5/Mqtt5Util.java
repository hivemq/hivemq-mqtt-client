package org.mqttbee.mqtt5;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Util {

    public static void disconnect(
            final Mqtt5DisconnectReasonCode reasonCode, @Nullable final String reasonString,
            @NotNull final Channel channel) {

        channel.config().setAutoRead(false);

        Mqtt5UTF8StringImpl mqttReasonString = null;
        if (reasonString != null) {
            final Boolean sendReasonString = channel.attr(ChannelAttributes.SEND_REASON_STRING).get();
            if ((sendReasonString != null) && sendReasonString) {
                mqttReasonString = Mqtt5UTF8StringImpl.from(reasonString);
            }
        }

        final Mqtt5DisconnectImpl disconnect =
                new Mqtt5DisconnectImpl(reasonCode, Mqtt5DisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null,
                        mqttReasonString, Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES);
        final ChannelFuture disconnectFuture = channel.writeAndFlush(disconnect);

        disconnectFuture.addListener(ChannelFutureListener.CLOSE);
    }

    public static void disconnect(final Mqtt5DisconnectReasonCode reasonCode, @NotNull final Channel channel) {
        disconnect(reasonCode, null, channel);
    }

}
