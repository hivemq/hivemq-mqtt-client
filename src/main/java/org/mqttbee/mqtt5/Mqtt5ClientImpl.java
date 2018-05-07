/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.mqtt5;

import io.netty.bootstrap.Bootstrap;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.AlreadyConnectedException;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5Client;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.handler.auth.MqttReAuthEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.publish.MqttGlobalIncomingPublishFlow;
import org.mqttbee.mqtt.handler.publish.MqttGlobalIncomingPublishFlowable;
import org.mqttbee.mqtt.handler.publish.MqttIncomingAckFlowable;
import org.mqttbee.mqtt.handler.publish.MqttSubscriptionFlowable;
import org.mqttbee.mqtt.handler.subscribe.MqttSubscriptionHandler;
import org.mqttbee.mqtt.handler.subscribe.MqttUnsubscribeWithFlow;
import org.mqttbee.mqtt.ioc.ChannelComponent;
import org.mqttbee.mqtt.ioc.MqttBeeComponent;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribe;
import org.mqttbee.rx.FlowableWithSingle;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientImpl implements Mqtt5Client {

    private final MqttClientData clientData;

    public Mqtt5ClientImpl(@NotNull final MqttClientData clientData) {
        this.clientData = clientData;
    }

    @NotNull
    @Override
    public Single<Mqtt5ConnAck> connect(@NotNull final Mqtt5Connect connect) {
        final MqttConnect mqttConnect = MustNotBeImplementedUtil.checkNotImplemented(connect, MqttConnect.class);

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

            final Bootstrap bootstrap =
                    MqttBeeComponent.INSTANCE.nettyBootstrap().bootstrap(clientData.getExecutorConfig());

            bootstrap.handler(MqttBeeComponent.INSTANCE.channelInitializerProvider()
                    .get(mqttConnect, connAckEmitter, clientData));

            bootstrap.connect(clientData.getServerHost(), clientData.getServerPort()).addListener(future -> {
                if (!future.isSuccess()) {
                    connAckEmitter.onError(future.cause());
                }
            });
        }).doOnSuccess(connAck -> {
            clientData.setConnected(true);
            clientData.setConnecting(false);

            final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
            assert clientConnectionData != null;
            clientConnectionData.getChannel().closeFuture().addListener(future -> {
                MqttBeeComponent.INSTANCE.nettyBootstrap().free(clientData.getExecutorConfig());
                clientData.setClientConnectionData(null);
                clientData.setServerConnectionData(null);
                clientData.setConnected(false);
            });
        }).doOnError(throwable -> {
            if (!(throwable instanceof AlreadyConnectedException)) {
                MqttBeeComponent.INSTANCE.nettyBootstrap().free(clientData.getExecutorConfig());
                clientData.setClientConnectionData(null);
                clientData.setServerConnectionData(null);
                clientData.setConnecting(false);
            }
        }).observeOn(clientData.getExecutorConfig().getRxJavaScheduler());
    }

    @NotNull
    @Override
    public FlowableWithSingle<Mqtt5SubscribeResult, Mqtt5SubAck, Mqtt5Publish> subscribe(
            @NotNull final Mqtt5Subscribe subscribe) {

        final MqttSubscribe mqttSubscribe =
                MustNotBeImplementedUtil.checkNotImplemented(subscribe, MqttSubscribe.class);

        final Flowable<Mqtt5SubscribeResult> subscriptionFlowable =
                new MqttSubscriptionFlowable(mqttSubscribe, clientData).observeOn(
                        clientData.getExecutorConfig().getRxJavaScheduler());
        return new FlowableWithSingle<>(subscriptionFlowable, Mqtt5SubAck.class, Mqtt5Publish.class);
    }

    @NotNull
    @Override
    public Flowable<Mqtt5Publish> remainingPublishes() {
        return new MqttGlobalIncomingPublishFlowable(MqttGlobalIncomingPublishFlow.TYPE_REMAINING_PUBLISHES, clientData)
            .observeOn(clientData.getExecutorConfig().getRxJavaScheduler());
    }

    @NotNull
    @Override
    public Flowable<Mqtt5Publish> allPublishes() {
        return new MqttGlobalIncomingPublishFlowable(
            MqttGlobalIncomingPublishFlow.TYPE_ALL_PUBLISHES, clientData).observeOn(
            clientData.getExecutorConfig().getRxJavaScheduler()); // TODO all subscriptions?
    }

    @NotNull
    @Override
    public Single<Mqtt5UnsubAck> unsubscribe(@NotNull final Mqtt5Unsubscribe unsubscribe) {
        final MqttUnsubscribe mqttUnsubscribe =
                MustNotBeImplementedUtil.checkNotImplemented(unsubscribe, MqttUnsubscribe.class);

        return Single.<Mqtt5UnsubAck>create(emitter -> {
            final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
            if (clientConnectionData == null) {
                emitter.onError(new NotConnectedException());
            } else {
                final ChannelComponent channelComponent = ChannelComponent.get(clientConnectionData.getChannel());
                final MqttSubscriptionHandler subscriptionHandler = channelComponent.subscriptionHandler();
                subscriptionHandler.unsubscribe(new MqttUnsubscribeWithFlow(mqttUnsubscribe, emitter));
            }
        }).observeOn(clientData.getExecutorConfig().getRxJavaScheduler());
    }

    @NotNull
    @Override
    public Flowable<Mqtt5PublishResult> publish(@NotNull final Flowable<Mqtt5Publish> publishFlowable) {
        return new MqttIncomingAckFlowable(
                publishFlowable.map(
                        publish -> MustNotBeImplementedUtil.checkNotImplemented(publish, MqttPublish.class)),
                clientData).observeOn(clientData.getExecutorConfig().getRxJavaScheduler());
    }

    @NotNull
    @Override
    public Completable reauth() {
        return Completable.create(emitter -> {
            final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
            if (clientConnectionData != null) {
                clientConnectionData.getChannel().pipeline().fireUserEventTriggered(new MqttReAuthEvent(emitter));
            } else {
                emitter.onError(new NotConnectedException());
            }
        }).observeOn(clientData.getExecutorConfig().getRxJavaScheduler());
    }

    @NotNull
    @Override
    public Completable disconnect(@NotNull final Mqtt5Disconnect disconnect) {
        final MqttDisconnect mqttDisconnect =
                MustNotBeImplementedUtil.checkNotImplemented(disconnect, MqttDisconnect.class);

        return Completable.create(emitter -> {
            final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
            if (clientConnectionData != null) {
                MqttDisconnectUtil.disconnect(clientConnectionData.getChannel(), mqttDisconnect).addListener(future -> {
                    if (future.isSuccess()) {
                        emitter.onComplete();
                    } else {
                        emitter.onError(future.cause());
                    }
                });
            } else {
                emitter.onError(new NotConnectedException());
            }
        }).observeOn(clientData.getExecutorConfig().getRxJavaScheduler());
    }

    @NotNull
    @Override
    public MqttClientData getClientData() {
        return clientData;
    }

}
