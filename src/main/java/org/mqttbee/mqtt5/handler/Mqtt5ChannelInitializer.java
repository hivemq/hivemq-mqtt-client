package org.mqttbee.mqtt5.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt5.Mqtt5ClientDataImpl;
import org.mqttbee.mqtt5.Mqtt5Component;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5Encoder;
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
        channel.pipeline()
                .addLast(Mqtt5Encoder.NAME, Mqtt5Component.INSTANCE.encoder())
                .addLast(Mqtt5ConnectHandler.NAME, new Mqtt5ConnectHandler(connect, connAckEmitter, clientData));
    }

}
