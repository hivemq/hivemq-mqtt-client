package org.mqttbee.mqtt5;

import io.netty.bootstrap.Bootstrap;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.AlreadyConnectedException;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5Client;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt.message.connect.MqttConnectImpl;
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
        final MqttConnectImpl connectImpl =
                MustNotBeImplementedUtil.checkNotImplemented(connect, MqttConnectImpl.class);

        return Single.<Mqtt5ConnAck>create(connAckEmitter -> {
            if (!clientData.setConnecting(true)) {
                connAckEmitter.onError(new AlreadyConnectedException(true));
                return;
            }
            if (clientData.isConnected()) {
                clientData.setConnecting(false);
                connAckEmitter.onError(new AlreadyConnectedException(false));
                return;
            }

            final Bootstrap bootstrap = Mqtt5Component.INSTANCE.nettyBootstrap()
                    .bootstrap(clientData.getExecutor(), clientData.getNumberOfNettyThreads());

            bootstrap.handler(
                    Mqtt5Component.INSTANCE.channelInitializerProvider().get(connectImpl, connAckEmitter, clientData));

            bootstrap.connect(clientData.getServerHost(), clientData.getServerPort()).addListener(future -> {
                if (!future.isSuccess()) {
                    connAckEmitter.onError(future.cause());
                }
            });
        }).doOnSuccess(connAck -> {
            clientData.setConnected(true);
            clientData.setConnecting(false);

            clientData.getRawClientConnectionData().getChannel().closeFuture().addListener(future -> {
                Mqtt5Component.INSTANCE.nettyBootstrap().free(clientData.getExecutor());
                clientData.setClientConnectionData(null);
                clientData.setServerConnectionData(null);
                clientData.setConnected(false);
            });
        }).doOnError(throwable -> {
            if (!(throwable instanceof AlreadyConnectedException)) {
                Mqtt5Component.INSTANCE.nettyBootstrap().free(clientData.getExecutor());
                clientData.setClientConnectionData(null);
                clientData.setServerConnectionData(null);
                clientData.setConnecting(false);
            }
        });
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
    public Mqtt5ClientData getClientData() {
        return clientData;
    }

}
