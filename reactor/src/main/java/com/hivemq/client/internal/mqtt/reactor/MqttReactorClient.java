/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import com.hivemq.client.mqtt.mqtt5.reactor.Mqtt5ReactorClient;
import com.hivemq.client.rx.reactor.FluxWithSingle;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.adapter.rxjava.RxJava2Adapter;
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

    public @NotNull Mono<Mqtt5ConnAck> connect(final @NotNull Mqtt5Connect connect) {
        return RxJava2Adapter.singleToMono(delegate.connect(connect));
    }

    @Override
    public @NotNull MqttConnectBuilder.Nested<Mono<Mqtt5ConnAck>> connectWith() {
        return new MqttConnectBuilder.Nested<>(this::connect);
    }

    public @NotNull Mono<Mqtt5SubAck> subscribe(final @NotNull Mqtt5Subscribe subscribe) {
        return RxJava2Adapter.singleToMono(delegate.subscribe(subscribe));
    }

    @Override
    public @NotNull MqttSubscribeBuilder.Nested<Mono<Mqtt5SubAck>> subscribeWith() {
        return new MqttSubscribeBuilder.Nested<>(this::subscribe);
    }

    public @NotNull FluxWithSingle<Mqtt5Publish, Mqtt5SubAck> subscribeStream(final @NotNull Mqtt5Subscribe subscribe) {
        return FluxWithSingle.from(delegate.subscribeStream(subscribe));
    }

    @Override
    public @NotNull MqttSubscribeBuilder.Nested<FluxWithSingle<Mqtt5Publish, Mqtt5SubAck>> subscribeStreamWith() {
        return new MqttSubscribeBuilder.Nested<>(this::subscribeStream);
    }

    public @NotNull Flux<Mqtt5Publish> publishes(final @NotNull MqttGlobalPublishFilter filter) {
        return RxJava2Adapter.flowableToFlux(delegate.publishes(filter));
    }

    public @NotNull Mono<Mqtt5UnsubAck> unsubscribe(final @NotNull Mqtt5Unsubscribe unsubscribe) {
        return RxJava2Adapter.singleToMono(delegate.unsubscribe(unsubscribe));
    }

    @Override
    public @NotNull MqttUnsubscribeBuilder.Nested<Mono<Mqtt5UnsubAck>> unsubscribeWith() {
        return new MqttUnsubscribeBuilder.Nested<>(this::unsubscribe);
    }

    public @NotNull Flux<Mqtt5PublishResult> publish(final @NotNull Publisher<Mqtt5Publish> publisher) {
        return RxJava2Adapter.flowableToFlux(delegate.publish(Flowable.fromPublisher(publisher)));
    }

    public @NotNull Mono<Void> reauth() {
        return RxJava2Adapter.completableToMono(delegate.reauth());
    }

    @Override
    public @NotNull Mono<Void> disconnect() {
        return disconnect(MqttDisconnect.DEFAULT);
    }

    public @NotNull Mono<Void> disconnect(final @NotNull Mqtt5Disconnect disconnect) {
        return RxJava2Adapter.completableToMono(delegate.disconnect(disconnect));
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
}
