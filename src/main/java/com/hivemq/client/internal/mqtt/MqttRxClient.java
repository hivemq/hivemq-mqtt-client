/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.internal.mqtt.handler.auth.MqttReAuthCompletable;
import com.hivemq.client.internal.mqtt.handler.connect.MqttConnAckSingle;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectCompletable;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttGlobalIncomingPublishFlowable;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttSubscribedPublishFlowable;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttAckFlowable;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttAckSingle;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttAckSingleFlowable;
import com.hivemq.client.internal.mqtt.handler.subscribe.MqttSubAckSingle;
import com.hivemq.client.internal.mqtt.handler.subscribe.MqttUnsubAckSingle;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectBuilder;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnectBuilder;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribeBuilder;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribeBuilder;
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
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.internal.fuseable.ScalarCallable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

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
    public @NotNull Single<Mqtt5ConnAck> connect() {
        return connect(MqttConnect.DEFAULT);
    }

    @Override
    public @NotNull Single<Mqtt5ConnAck> connect(final @Nullable Mqtt5Connect connect) {
        return connect(MqttChecks.connect(connect));
    }

    @NotNull Single<Mqtt5ConnAck> connect(final @NotNull MqttConnect connect) {
        return connectUnsafe(connect).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Single<Mqtt5ConnAck> connectUnsafe(final @NotNull MqttConnect connect) {
        return new MqttConnAckSingle(clientConfig, connect);
    }

    @Override
    public MqttConnectBuilder.@NotNull Nested<Single<Mqtt5ConnAck>> connectWith() {
        return new MqttConnectBuilder.Nested<>(this::connect);
    }

    @Override
    public @NotNull Single<Mqtt5SubAck> subscribe(final @Nullable Mqtt5Subscribe subscribe) {
        return subscribe(MqttChecks.subscribe(subscribe));
    }

    @NotNull Single<Mqtt5SubAck> subscribe(final @NotNull MqttSubscribe subscribe) {
        return subscribeUnsafe(subscribe).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Single<Mqtt5SubAck> subscribeUnsafe(final @NotNull MqttSubscribe subscribe) {
        return new MqttSubAckSingle(subscribe, clientConfig);
    }

    @Override
    public MqttSubscribeBuilder.@NotNull Nested<Single<Mqtt5SubAck>> subscribeWith() {
        return new MqttSubscribeBuilder.Nested<>(this::subscribe);
    }

    @Override
    public @NotNull FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribePublishes(
            final @Nullable Mqtt5Subscribe subscribe) {

        return subscribePublishes(subscribe, false);
    }

    @Override
    public @NotNull FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribePublishes(
            final @Nullable Mqtt5Subscribe subscribe, final boolean manualAcknowledgement) {

        return subscribePublishes(MqttChecks.subscribe(subscribe), manualAcknowledgement);
    }

    @NotNull FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribePublishes(
            final @NotNull MqttSubscribe subscribe, final boolean manualAcknowledgement) {

        return subscribePublishesUnsafe(subscribe, manualAcknowledgement).observeOnBoth(
                clientConfig.getExecutorConfig().getApplicationScheduler(), true);
    }

    @NotNull FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribePublishesUnsafe(
            final @NotNull MqttSubscribe subscribe, final boolean manualAcknowledgement) {

        return new MqttSubscribedPublishFlowable(subscribe, clientConfig, manualAcknowledgement);
    }

    @Override
    public @NotNull MqttSubscribePublishesBuilder subscribePublishesWith() {
        return new MqttSubscribePublishesBuilder();
    }

    @Override
    public @NotNull Flowable<Mqtt5Publish> publishes(final @Nullable MqttGlobalPublishFilter filter) {
        return publishes(filter, false);
    }

    @Override
    public @NotNull Flowable<Mqtt5Publish> publishes(
            final @Nullable MqttGlobalPublishFilter filter, final boolean manualAcknowledgement) {

        Checks.notNull(filter, "Global publish filter");

        return publishesUnsafe(filter, manualAcknowledgement).observeOn(
                clientConfig.getExecutorConfig().getApplicationScheduler(), true);
    }

    @NotNull Flowable<Mqtt5Publish> publishesUnsafe(
            final @NotNull MqttGlobalPublishFilter filter, final boolean manualAcknowledgement) {

        return new MqttGlobalIncomingPublishFlowable(filter, clientConfig, manualAcknowledgement);
    }

    @Override
    public @NotNull Single<Mqtt5UnsubAck> unsubscribe(final @Nullable Mqtt5Unsubscribe unsubscribe) {
        return unsubscribe(MqttChecks.unsubscribe(unsubscribe));
    }

    @NotNull Single<Mqtt5UnsubAck> unsubscribe(final @NotNull MqttUnsubscribe unsubscribe) {
        return unsubscribeUnsafe(unsubscribe).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Single<Mqtt5UnsubAck> unsubscribeUnsafe(final @NotNull MqttUnsubscribe unsubscribe) {
        return new MqttUnsubAckSingle(unsubscribe, clientConfig);
    }

    @Override
    public MqttUnsubscribeBuilder.@NotNull Nested<Single<Mqtt5UnsubAck>> unsubscribeWith() {
        return new MqttUnsubscribeBuilder.Nested<>(this::unsubscribe);
    }

    @NotNull Single<Mqtt5PublishResult> publish(final @NotNull MqttPublish publish) {
        return publishUnsafe(publish).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Single<Mqtt5PublishResult> publishUnsafe(final @NotNull MqttPublish publish) {
        return new MqttAckSingle(clientConfig, publish);
    }

    @Override
    public @NotNull Flowable<Mqtt5PublishResult> publish(final @NotNull Publisher<Mqtt5Publish> publisher) {
        Checks.notNull(publisher, "Publisher");

        return publish(publisher, PUBLISH_MAPPER);
    }

    public <P> @NotNull Flowable<Mqtt5PublishResult> publish(
            final @NotNull Publisher<P> publisher, final @NotNull Function<P, MqttPublish> publishMapper) {

        final Scheduler applicationScheduler = clientConfig.getExecutorConfig().getApplicationScheduler();
        if (publisher instanceof ScalarCallable) {
            //noinspection unchecked
            final P publish = ((ScalarCallable<P>) publisher).call();
            if (publish == null) {
                return Flowable.empty();
            }
            final MqttPublish mqttPublish;
            try {
                mqttPublish = publishMapper.apply(publish);
            } catch (final Throwable t) {
                return Flowable.error(t);
            }
            return new MqttAckSingleFlowable(clientConfig, mqttPublish).observeOn(applicationScheduler, true);
        }
        return new MqttAckFlowable(
                clientConfig,
                Flowable.fromPublisher(publisher).subscribeOn(applicationScheduler).map(publishMapper)).observeOn(
                applicationScheduler, true);
    }

    @Override
    public @NotNull Completable reauth() {
        return reauthUnsafe().observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Completable reauthUnsafe() {
        return new MqttReAuthCompletable(clientConfig);
    }

    @Override
    public @NotNull Completable disconnect() {
        return disconnect(MqttDisconnect.DEFAULT);
    }

    @Override
    public @NotNull Completable disconnect(final @Nullable Mqtt5Disconnect disconnect) {
        return disconnect(MqttChecks.disconnect(disconnect));
    }

    @NotNull Completable disconnect(final @NotNull MqttDisconnect disconnect) {
        return disconnectUnsafe(disconnect).observeOn(clientConfig.getExecutorConfig().getApplicationScheduler());
    }

    @NotNull Completable disconnectUnsafe(final @NotNull MqttDisconnect disconnect) {
        return new MqttDisconnectCompletable(clientConfig, disconnect);
    }

    @Override
    public MqttDisconnectBuilder.@NotNull Nested<Completable> disconnectWith() {
        return new MqttDisconnectBuilder.Nested<>(this::disconnect);
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

    private class MqttSubscribePublishesBuilder
            extends MqttSubscribeBuilder.Publishes<FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck>> {

        @Override
        public @NotNull FlowableWithSingle<Mqtt5Publish, Mqtt5SubAck> applySubscribe() {
            return subscribePublishes(build(), manualAcknowledgement);
        }
    }
}
