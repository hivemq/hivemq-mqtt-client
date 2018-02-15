package org.mqttbee.mqtt5;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.MqttClientData;
import org.mqttbee.api.mqtt5.Mqtt5Client;
import org.mqttbee.api.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.api.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.api.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt5.handler.Mqtt5ConnectHandler;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.rx.FlowableWithSingle;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientImpl implements Mqtt5Client {

    private final Mqtt5ClientDataImpl clientData;

    public Mqtt5ClientImpl(@NotNull final Mqtt5ClientDataImpl clientData) {
        this.clientData = clientData;
    }

    @NotNull
    @Override
    public Single<Mqtt5ConnAck> connect(@NotNull final Mqtt5Connect connect) {
        MustNotBeImplementedUtil.checkNotImplemented(connect, Mqtt5ConnectImpl.class);

        return Single.create(connAckEmitter -> {
            if (!clientData.setConnected(true)) {
                connAckEmitter.onError(new IllegalStateException()); // TODO right exception
                return;
            }

            final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            final Bootstrap bootstrap = bootstrap(eventLoopGroup);

            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(final SocketChannel channel) throws Exception {
                    channel.pipeline().addLast(new Mqtt5ConnectHandler(connAckEmitter, clientData));
                }
            });

            final ChannelFuture connectFuture =
                    bootstrap.connect(clientData.getServerHost(), clientData.getServerPort());

            connectFuture.addListener(future -> {
                if (future.isSuccess()) {
                    connectFuture.channel().writeAndFlush(connect);
                } else {
                    clientData.setConnected(false);
                    connAckEmitter.onError(future.cause());
                    eventLoopGroup.shutdownGracefully(); // TODO handle future
                }
            });
        });
    }

    @NotNull
    private Bootstrap bootstrap(@NotNull final EventLoopGroup eventLoopGroup) { // TODO epoll
        return new Bootstrap().group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    }

    @NotNull
    @Override
    public FlowableWithSingle<Mqtt5SubscribeResult, Mqtt5SubAck, Mqtt5Publish> subscribe(
            @NotNull final Mqtt5Subscribe subscribe) {

        return null;
    }

    @NotNull
    @Override
    public Flowable<Mqtt5Publish> remainingPublishes() {
        return null;
    }

    @NotNull
    @Override
    public Flowable<Mqtt5Publish> allPublishes() {
        return null;
    }

    @NotNull
    @Override
    public Single<Mqtt5UnsubAck> unsubscribe(@NotNull final Mqtt5Unsubscribe unsubscribe) {
        return null;
    }

    @Override
    public void publish(@NotNull final Flowable<Mqtt5Publish> publishFlowable) {

    }

    @NotNull
    @Override
    public Completable reauth() {
        return null;
    }

    @NotNull
    @Override
    public Completable disconnect(@NotNull final Mqtt5Disconnect disconnect) {
        return null;
    }

    @NotNull
    @Override
    public MqttClientData getClientData() {
        return clientData;
    }

}
