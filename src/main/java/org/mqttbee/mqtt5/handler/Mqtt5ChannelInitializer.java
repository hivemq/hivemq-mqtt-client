package org.mqttbee.mqtt5.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.MqttClientDataImpl;
import org.mqttbee.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.mqtt.message.connect.MqttConnectImpl;
import org.mqttbee.mqtt5.handler.auth.Mqtt5AuthHandler;
import org.mqttbee.mqtt5.handler.auth.Mqtt5DisconnectOnAuthHandler;
import org.mqttbee.mqtt5.handler.connect.Mqtt5ConnectHandler;
import org.mqttbee.mqtt5.handler.disconnect.Mqtt5DisconnectHandler;
import org.mqttbee.mqtt5.ioc.ChannelComponent;

/**
 * Default channel initializer.
 *
 * @author Silvio Giebl
 */
public class Mqtt5ChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final MqttConnectImpl connect;
    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;
    private final MqttClientDataImpl clientData;

    Mqtt5ChannelInitializer(
            @NotNull final MqttConnectImpl connect, @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            @NotNull final MqttClientDataImpl clientData) {

        this.connect = connect;
        this.connAckEmitter = connAckEmitter;
        this.clientData = clientData;
    }

    @Override
    protected void initChannel(final SocketChannel channel) {
        final ChannelComponent channelComponent = ChannelComponent.create(channel, clientData);

        final ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(MqttEncoder.NAME, channelComponent.encoder());

        if (connect.getRawEnhancedAuthProvider() == null) {
            pipeline.addLast(Mqtt5DisconnectOnAuthHandler.NAME, channelComponent.disconnectOnAuthHandler());
        } else {
            pipeline.addLast(Mqtt5AuthHandler.NAME, channelComponent.authHandler());
        }

        pipeline.addLast(Mqtt5ConnectHandler.NAME, new Mqtt5ConnectHandler(connect, connAckEmitter, clientData));
        pipeline.addLast(Mqtt5DisconnectHandler.NAME, channelComponent.disconnectHandler());
    }

}
