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

package com.hivemq.client.internal.mqtt.mqtt3.reactor;

import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeViewBuilder;
import com.hivemq.client.internal.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeViewBuilder;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3RxClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.client.mqtt.mqtt3.reactor.Mqtt3ReactorClient;
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
public class Mqtt3ReactorClientView implements Mqtt3ReactorClient {

    private final @NotNull Mqtt3RxClient delegate;

    public Mqtt3ReactorClientView(final @NotNull Mqtt3RxClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Mono<Mqtt3ConnAck> connect() {
        return connect(Mqtt3ConnectView.DEFAULT);
    }

    public @NotNull Mono<Mqtt3ConnAck> connect(final @NotNull Mqtt3Connect connect) {
        return RxJava2Adapter.singleToMono(delegate.connect(connect));
    }

    @Override
    public @NotNull Mqtt3ConnectViewBuilder.Nested<Mono<Mqtt3ConnAck>> connectWith() {
        return new Mqtt3ConnectViewBuilder.Nested<>(this::connect);
    }

    public @NotNull Mono<Mqtt3SubAck> subscribe(final @NotNull Mqtt3Subscribe subscribe) {
        return RxJava2Adapter.singleToMono(delegate.subscribe(subscribe));
    }

    @Override
    public @NotNull Mqtt3SubscribeViewBuilder.Nested<Mono<Mqtt3SubAck>> subscribeWith() {
        return new Mqtt3SubscribeViewBuilder.Nested<>(this::subscribe);
    }

    public @NotNull FluxWithSingle<Mqtt3Publish, Mqtt3SubAck> subscribeStream(final @NotNull Mqtt3Subscribe subscribe) {
        return FluxWithSingle.from(delegate.subscribeStream(subscribe));
    }

    @Override
    public @NotNull Mqtt3SubscribeViewBuilder.Nested<FluxWithSingle<Mqtt3Publish, Mqtt3SubAck>> subscribeStreamWith() {
        return new Mqtt3SubscribeViewBuilder.Nested<>(this::subscribeStream);
    }

    public @NotNull Flux<Mqtt3Publish> publishes(final @NotNull MqttGlobalPublishFilter filter) {
        return publishes(filter, false);
    }

    @Override
    public @NotNull Flux<Mqtt3Publish> publishes(
            final @NotNull MqttGlobalPublishFilter filter, final boolean manualAcknowledgement) {

        return RxJava2Adapter.flowableToFlux(delegate.publishes(filter, manualAcknowledgement));
    }

    public @NotNull Mono<Void> unsubscribe(final @NotNull Mqtt3Unsubscribe unsubscribe) {
        return RxJava2Adapter.completableToMono(delegate.unsubscribe(unsubscribe));
    }

    @Override
    public @NotNull Mqtt3UnsubscribeViewBuilder.Nested<Mono<Void>> unsubscribeWith() {
        return new Mqtt3UnsubscribeViewBuilder.Nested<>(this::unsubscribe);
    }

    public @NotNull Flux<Mqtt3PublishResult> publish(final @NotNull Publisher<Mqtt3Publish> publisher) {
        return RxJava2Adapter.flowableToFlux(delegate.publish(Flowable.fromPublisher(publisher)));
    }

    public @NotNull Mono<Void> disconnect() {
        return RxJava2Adapter.completableToMono(delegate.disconnect());
    }

    @Override
    public @NotNull Mqtt3ClientConfig getConfig() {
        return delegate.getConfig();
    }

    @Override
    public @NotNull Mqtt3RxClient toRx() {
        return delegate;
    }

    @Override
    public @NotNull Mqtt3AsyncClient toAsync() {
        return delegate.toAsync();
    }

    @Override
    public @NotNull Mqtt3BlockingClient toBlocking() {
        return delegate.toBlocking();
    }
}
