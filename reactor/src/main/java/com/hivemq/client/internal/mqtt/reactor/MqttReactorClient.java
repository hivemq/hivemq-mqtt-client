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

package com.hivemq.client.internal.mqtt.reactor;

import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectBuilder;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client.internal.mqtt.message.disconnect.MqttDisconnectBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribeBuilder;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribeBuilder;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.reactor.Mqtt5ReactorClient;
import com.hivemq.client.rx.reactor.FluxWithSingle;
import io.reactivex.rxjava3.core.Flowable;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava3Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Silvio Giebl
 */
public class MqttReactorClient implements Mqtt5ReactorClient {

    private final @NotNull Mqtt5RxClient delegate;

    public MqttReactorClient(final @NotNull Mqtt5RxClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Mono<Mqtt5ConnAck> connect() {
        return connect(MqttConnect.DEFAULT);
    }

    @Override
    public @NotNull Mono<Mqtt5ConnAck> connect(final @NotNull Mqtt5Connect connect) {
        return RxJava3Adapter.singleToMono(delegate.connect(connect));
    }

    @Override
    public @NotNull MqttConnectBuilder.Nested<Mono<Mqtt5ConnAck>> connectWith() {
        return new MqttConnectBuilder.Nested<>(this::connect);
    }

    @Override
    public @NotNull Mono<Mqtt5SubAck> subscribe(final @NotNull Mqtt5Subscribe subscribe) {
        return RxJava3Adapter.singleToMono(delegate.subscribe(subscribe));
    }

    @Override
    public @NotNull MqttSubscribeBuilder.Nested<Mono<Mqtt5SubAck>> subscribeWith() {
        return new MqttSubscribeBuilder.Nested<>(this::subscribe);
    }

    @Override
    public @NotNull FluxWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribePublishes(
            final @NotNull Mqtt5Subscribe subscribe) {

        return subscribePublishes(subscribe, false);
    }

    @Override
    public @NotNull FluxWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribePublishes(
            final @NotNull Mqtt5Subscribe subscribe, final boolean manualAcknowledgement) {

        return FluxWithSingle.from(delegate.subscribePublishes(subscribe, manualAcknowledgement));
    }

    @Override
    public @NotNull MqttSubscribePublishesBuilder subscribePublishesWith() {
        return new MqttSubscribePublishesBuilder();
    }

    @Override
    public @NotNull Flux<Mqtt5Publish> publishes(final @NotNull MqttGlobalPublishFilter filter) {
        return publishes(filter, false);
    }

    @Override
    public @NotNull Flux<Mqtt5Publish> publishes(
            final @NotNull MqttGlobalPublishFilter filter, final boolean manualAcknowledgement) {

        return RxJava3Adapter.flowableToFlux(delegate.publishes(filter, manualAcknowledgement));
    }

    @Override
    public @NotNull Mono<Mqtt5UnsubAck> unsubscribe(final @NotNull Mqtt5Unsubscribe unsubscribe) {
        return RxJava3Adapter.singleToMono(delegate.unsubscribe(unsubscribe));
    }

    @Override
    public @NotNull MqttUnsubscribeBuilder.Nested<Mono<Mqtt5UnsubAck>> unsubscribeWith() {
        return new MqttUnsubscribeBuilder.Nested<>(this::unsubscribe);
    }

    @Override
    public @NotNull Flux<Mqtt5PublishResult> publish(final @NotNull Publisher<Mqtt5Publish> publisher) {
        return RxJava3Adapter.flowableToFlux(delegate.publish(Flowable.fromPublisher(publisher)));
    }

    @Override
    public @NotNull Mono<Void> reauth() {
        return RxJava3Adapter.completableToMono(delegate.reauth());
    }

    @Override
    public @NotNull Mono<Void> disconnect() {
        return disconnect(MqttDisconnect.DEFAULT);
    }

    @Override
    public @NotNull Mono<Void> disconnect(final @NotNull Mqtt5Disconnect disconnect) {
        return RxJava3Adapter.completableToMono(delegate.disconnect(disconnect));
    }

    @Override
    public @NotNull MqttDisconnectBuilder.Nested<Mono<Void>> disconnectWith() {
        return new MqttDisconnectBuilder.Nested<>(this::disconnect);
    }

    @Override
    public @NotNull Mqtt5ClientConfig getConfig() {
        return delegate.getConfig();
    }

    @Override
    public @NotNull Mqtt5RxClient toRx() {
        return delegate;
    }

    @Override
    public @NotNull Mqtt5AsyncClient toAsync() {
        return delegate.toAsync();
    }

    @Override
    public @NotNull Mqtt5BlockingClient toBlocking() {
        return delegate.toBlocking();
    }

    private class MqttSubscribePublishesBuilder
            extends MqttSubscribeBuilder.Publishes<FluxWithSingle<Mqtt5Publish, Mqtt5SubAck>> {

        @Override
        public @NotNull FluxWithSingle<Mqtt5Publish, Mqtt5SubAck> applySubscribe() {
            return subscribePublishes(build(), manualAcknowledgement);
        }
    }
}
