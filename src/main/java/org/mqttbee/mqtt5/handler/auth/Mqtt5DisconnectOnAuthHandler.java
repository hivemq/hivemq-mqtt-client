package org.mqttbee.mqtt5.handler.auth;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.message.auth.MqttAuthImpl;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckImpl;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Sends a DISCONNECT message if an AUTH message or a CONNACK message with enhanced auth data is received. This handler
 * is added if enhanced auth is not used at connection.
 *
 * @author Silvio Giebl
 */
@ChannelHandler.Sharable
@Singleton
public class Mqtt5DisconnectOnAuthHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "disconnect.on.auth";

    @Inject
    Mqtt5DisconnectOnAuthHandler() {
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttAuthImpl) {
            readAuth(ctx, (MqttAuthImpl) msg);
        } else if (msg instanceof MqttConnAckImpl) {
            readConnAck(ctx, (MqttConnAckImpl) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readAuth(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuthImpl auth) {
        Mqtt5DisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5MessageException(auth, "Server must not send AUTH"));
    }

    private void readConnAck(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttConnAckImpl connAck) {
        if (connAck.getRawEnhancedAuth() != null) {
            Mqtt5DisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(connAck, "Server must not include auth in CONNACK"));
        } else {
            ctx.fireChannelRead(connAck);
        }
    }

}
