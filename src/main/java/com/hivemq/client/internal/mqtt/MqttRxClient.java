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

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.internal.mqtt.handler.auth.MqttReAuthCompletable;
import com.hivemq.client.internal.mqtt.handler.connect.MqttConnAckSingle;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectCompletable;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttGlobalIncomingPublishFlowable;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttSubscribedPublishFlowable;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttIncomingAckFlowable;
import com.hivemq.client.internal.mqtt.handler.subscribe.MqttSubAckSingle;
import com.hivemq.client.internal.mqtt.handler.subscribe.MqttUnsubAckSingle;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import com.hivemq.client.rx.FlowableWithSingle;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class MqttRxClient implements Mqtt5RxClient {

    private static final @NotNull Function<Mqtt5Publish, MqttPublish> PUBLISH_MAPPER = MqttChecks::publish;

    private final @NotNull MqttClientConfig clientConfig;

    public MqttRxClient(final @NotNull MqttClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    @Override
    public @NotNull Single<Mqtt5ConnAck> connect(final @Nullable Mqtt5Connect connect) {
        return connectUnsafe(connect).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Single<Mqtt5ConnAck> connectUnsafe(final @Nullable Mqtt5Connect connect) {
        final MqttConnect mqttConnect = MqttChecks.connect(connect);

        return new MqttConnAckSingle(clientConfig, mqttConnect);
    }

    @Override
    public @NotNull Single<Mqtt5SubAck> subscribe(final @Nullable Mqtt5Subscribe subscribe) {
        return subscribeUnsafe(subscribe).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Single<Mqtt5SubAck> subscribeUnsafe(final @Nullable Mqtt5Subscribe subscribe) {
        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);

        return new MqttSubAckSingle(mqttSubscribe, clientConfig);
    }

    @Override
    public @NotNull FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribeStream(
            final @Nullable Mqtt5Subscribe subscribe) {

        return subscribeStreamUnsafe(subscribe).observeOnBoth(
                clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribeStreamUnsafe(
            final @Nullable Mqtt5Subscribe subscribe) {

        final MqttSubscribe mqttSubscribe = MqttChecks.subscribe(subscribe);

        return new MqttSubscribedPublishFlowable(mqttSubscribe, clientConfig);
    }

    @Override
    public @NotNull Flowable<Mqtt5Publish> publishes(final @Nullable MqttGlobalPublishFilter filter) {
        return publishesUnsafe(filter).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Flowable<Mqtt5Publish> publishesUnsafe(final @Nullable MqttGlobalPublishFilter filter) {
        Checks.notNull(filter, "Global publish filter");

        return new MqttGlobalIncomingPublishFlowable(filter, clientConfig);
    }

    @Override
    public @NotNull Single<Mqtt5UnsubAck> unsubscribe(final @Nullable Mqtt5Unsubscribe unsubscribe) {
        return unsubscribeUnsafe(unsubscribe).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Single<Mqtt5UnsubAck> unsubscribeUnsafe(final @Nullable Mqtt5Unsubscribe unsubscribe) {
        final MqttUnsubscribe mqttUnsubscribe = MqttChecks.unsubscribe(unsubscribe);

        return new MqttUnsubAckSingle(mqttUnsubscribe, clientConfig);
    }

    @Override
    public @NotNull Flowable<Mqtt5PublishResult> publish(final @Nullable Flowable<Mqtt5Publish> publishFlowable) {
        Checks.notNull(publishFlowable, "Publish flowable");

        return publishHalfSafe(publishFlowable.subscribeOn(clientConfig.getExecutorConfig().getApplicationScheduler()));
    }

    @NotNull Flowable<Mqtt5PublishResult> publishHalfSafe(final @NotNull Flowable<Mqtt5Publish> publishFlowable) {
        return publishUnsafe(publishFlowable).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Flowable<Mqtt5PublishResult> publishUnsafe(final @NotNull Flowable<Mqtt5Publish> publishFlowable) {
        return new MqttIncomingAckFlowable(publishFlowable.map(PUBLISH_MAPPER), clientConfig);
    }

    @Override
    public @NotNull Completable reauth() {
        return reauthUnsafe().observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Completable reauthUnsafe() {
        return new MqttReAuthCompletable(clientConfig);
    }

    @Override
    public @NotNull Completable disconnect(final @Nullable Mqtt5Disconnect disconnect) {
        return disconnectUnsafe(disconnect).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Completable disconnectUnsafe(final @Nullable Mqtt5Disconnect disconnect) {
        final MqttDisconnect mqttDisconnect = MqttChecks.disconnect(disconnect);

        return new MqttDisconnectCompletable(clientConfig, mqttDisconnect);
    }

    @Override
    public @NotNull MqttClientConfig getConfig() {
        return clientConfig;
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
