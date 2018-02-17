package org.mqttbee.mqtt5.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.Mqtt5Component;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5Encoder;
import org.mqttbee.mqtt5.handler.auth.Mqtt5AuthHandler;
import org.mqttbee.mqtt5.handler.auth.Mqtt5DisconnectOnAuthHandler;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final Mqtt5ConnectImpl connect;
    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;
    private final Mqtt5ClientDataImpl clientData;

    Mqtt5ChannelInitializer(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            @NotNull final Mqtt5ClientDataImpl clientData) {

        this.connect = connect;
        this.connAckEmitter = connAckEmitter;
        this.clientData = clientData;
    }

    @Override
    protected void initChannel(final SocketChannel channel) {
        final ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(Mqtt5Encoder.NAME, Mqtt5Component.INSTANCE.encoder());
        if (clientData.getRawClientConnectionData().getEnhancedAuthProvider() == null) {
            pipeline.addLast(Mqtt5DisconnectOnAuthHandler.NAME, Mqtt5Component.INSTANCE.disconnectOnAuthHandler());
        } else {
            pipeline.addLast(Mqtt5AuthHandler.NAME, new Mqtt5AuthHandler());
        }
        pipeline.addLast(Mqtt5ConnectHandler.NAME, new Mqtt5ConnectHandler(connect, connAckEmitter, clientData));
    }

}
