package org.mqttbee.mqtt5.handler.auth;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.message.auth.MqttAuth;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAck;
import org.mqttbee.mqtt5.handler.disconnect.MqttDisconnectUtil;

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
public class Mqtt5DisconnectOnAuthHandler extends ChannelDuplexHandler {

    public static final String NAME = "disconnect.on.auth";

    @Inject
    Mqtt5DisconnectOnAuthHandler() {
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof MqttConnect) {
            writeConnect(ctx, (MqttConnect) msg, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private void writeConnect(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttConnect connect,
            @NotNull final ChannelPromise promise) {

        final MqttClientData clientData = MqttClientData.from(ctx.channel());
        ctx.writeAndFlush(connect.wrap(clientData.getRawClientIdentifier(), null), promise);
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttAuth) {
            readAuth(ctx, (MqttAuth) msg);
        } else if (msg instanceof MqttConnAck) {
            readConnAck(ctx, (MqttConnAck) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readAuth(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttAuth auth) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                new Mqtt5MessageException(auth, "Server must not send AUTH"));
    }

    private void readConnAck(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttConnAck connAck) {
        if (connAck.getRawEnhancedAuth() != null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5MessageException(connAck, "Server must not include auth in CONNACK"));
        } else {
            ctx.fireChannelRead(connAck);
        }
    }

}
