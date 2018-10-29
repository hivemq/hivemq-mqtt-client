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

package org.mqttbee.mqtt;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.exceptions.AlreadyConnectedException;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5RxClient;
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
import org.mqttbee.mqtt.handler.MqttChannelInitializer;
import org.mqttbee.mqtt.handler.auth.MqttReAuthEvent;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.publish.MqttGlobalIncomingPublishFlowable;
import org.mqttbee.mqtt.handler.publish.MqttIncomingAckFlowable;
import org.mqttbee.mqtt.handler.publish.MqttSubscriptionFlowable;
import org.mqttbee.mqtt.handler.subscribe.MqttSubAckSingle;
import org.mqttbee.mqtt.handler.subscribe.MqttUnsubAckSingle;
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
public class MqttRxClient implements Mqtt5RxClient {

    private static final @NotNull Function<Mqtt5Publish, MqttPublish> PUBLISH_MAPPER =
            publish -> MustNotBeImplementedUtil.checkNotImplemented(publish, MqttPublish.class);

    private final @NotNull MqttClientData clientData;

    public MqttRxClient(final @NotNull MqttClientData clientData) {
        this.clientData = clientData;
    }

    @Override
    public @NotNull Single<Mqtt5ConnAck> connect(final @NotNull Mqtt5Connect connect) {
        return connectUnsafe(connect).observeOn(clientData.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Single<Mqtt5ConnAck> connectUnsafe(final @NotNull Mqtt5Connect connect) {
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

            bootstrap.handler(new MqttChannelInitializer(mqttConnect, connAckEmitter, clientData));

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
        });
    }

    @Override
    public @NotNull Single<Mqtt5SubAck> subscribe(final @NotNull Mqtt5Subscribe subscribe) {
        return subscribeUnsafe(subscribe).observeOn(clientData.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Single<Mqtt5SubAck> subscribeUnsafe(final @NotNull Mqtt5Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe =
                MustNotBeImplementedUtil.checkNotImplemented(subscribe, MqttSubscribe.class);

        return new MqttSubAckSingle(mqttSubscribe, clientData);
    }

    @Override
    public @NotNull FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribeStream(
            final @NotNull Mqtt5Subscribe subscribe) {

        return subscribeStreamUnsafe(subscribe).observeOnBoth(clientData.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribeStreamUnsafe(
            final @NotNull Mqtt5Subscribe subscribe) {

        final MqttSubscribe mqttSubscribe =
                MustNotBeImplementedUtil.checkNotImplemented(subscribe, MqttSubscribe.class);

        final Flowable<Mqtt5SubscribeResult> subscriptionFlowable =
                new MqttSubscriptionFlowable(mqttSubscribe, clientData);
        return FlowableWithSingle.split(subscriptionFlowable, Mqtt5Publish.class, Mqtt5SubAck.class);
    }

    @Override
    public @NotNull Flowable<Mqtt5Publish> publishes(final @NotNull MqttGlobalPublishFilter filter) {
        return publishesUnsafe(filter).observeOn(clientData.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Flowable<Mqtt5Publish> publishesUnsafe(final @NotNull MqttGlobalPublishFilter filter) {
        Preconditions.checkNotNull(filter, "Global publish filter must not be null.");

        return new MqttGlobalIncomingPublishFlowable(filter, clientData);
    }

    @Override
    public @NotNull Single<Mqtt5UnsubAck> unsubscribe(final @NotNull Mqtt5Unsubscribe unsubscribe) {
        return unsubscribeUnsafe(unsubscribe).observeOn(clientData.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Single<Mqtt5UnsubAck> unsubscribeUnsafe(final @NotNull Mqtt5Unsubscribe unsubscribe) {
        final MqttUnsubscribe mqttUnsubscribe =
                MustNotBeImplementedUtil.checkNotImplemented(unsubscribe, MqttUnsubscribe.class);

        return new MqttUnsubAckSingle(mqttUnsubscribe, clientData);
    }

    @Override
    public @NotNull Flowable<Mqtt5PublishResult> publish(final @NotNull Flowable<Mqtt5Publish> publishFlowable) {
        return publishHalfSafe(publishFlowable.subscribeOn(clientData.getExecutorConfig().getApplicationScheduler()));
    }

    @NotNull Flowable<Mqtt5PublishResult> publishHalfSafe(final @NotNull Flowable<Mqtt5Publish> publishFlowable) {
        return publishUnsafe(publishFlowable).observeOn(clientData.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Flowable<Mqtt5PublishResult> publishUnsafe(final @NotNull Flowable<Mqtt5Publish> publishFlowable) {
        return new MqttIncomingAckFlowable(publishFlowable.map(PUBLISH_MAPPER), clientData);
    }

    @Override
    public @NotNull Completable reauth() {
        return reauthUnsafe().observeOn(clientData.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Completable reauthUnsafe() {
        return Completable.create(emitter -> {
            final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
            if (clientConnectionData != null) {
                clientConnectionData.getChannel().pipeline().fireUserEventTriggered(new MqttReAuthEvent(emitter));
            } else {
                emitter.onError(new NotConnectedException());
            }
        });
    }

    @Override
    public @NotNull Completable disconnect(final @NotNull Mqtt5Disconnect disconnect) {
        return disconnectUnsafe(disconnect).observeOn(clientData.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Completable disconnectUnsafe(final @NotNull Mqtt5Disconnect disconnect) {
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
        });
    }

    @Override
    public @NotNull MqttClientData getClientData() {
        return clientData;
    }

    @Override
    public @NotNull MqttAsyncClient toAsync() {
        return new MqttAsyncClient(this);
    }

    @Override
    public @NotNull MqttBlockingClient toBlocking() {
        return new MqttBlockingClient(this);
    }
}
