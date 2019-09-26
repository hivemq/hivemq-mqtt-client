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

package com.hivemq.client.internal.mqtt.reactor.mqtt3;

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
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.unsuback.Mqtt3UnsubAck;
import com.hivemq.client.mqtt.reactor.mqtt3.Mqtt3ReactorClient;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Mqtt3ReactorClientView implements Mqtt3ReactorClient {

    private final @NotNull Mqtt3RxClient delegate;

    public Mqtt3ReactorClientView(final @NotNull Mqtt3RxClient delegate) {
        this.delegate = delegate;
    }

    public @NotNull Mono<Mqtt3ConnAck> connect(final @NotNull Mqtt3Connect connect) {
        return Mono.fromDirect(delegate.connect(connect).toFlowable());
    }

    public @NotNull Mono<Mqtt3SubAck> subscribe(final @NotNull Mqtt3Subscribe subscribe) {
        return Mono.fromDirect(delegate.subscribe(subscribe).toFlowable());
    }

    public @NotNull Flux<Mqtt3Publish> subscribeStream(final @NotNull Mqtt3Subscribe subscribe) {
        return Flux.from(delegate.subscribeStream(subscribe));
    }

    public @NotNull Flux<Mqtt3Publish> publishes(final @NotNull MqttGlobalPublishFilter filter) {
        return Flux.from(delegate.publishes(filter));
    }

    public @NotNull Mono<Mqtt3UnsubAck> unsubscribe(final @NotNull Mqtt3Unsubscribe unsubscribe) {
        return Mono.fromDirect(delegate.unsubscribe(unsubscribe).toFlowable());
    }

    public @NotNull Flux<Mqtt3PublishResult> publish(final @NotNull Publisher<Mqtt3Publish> publishFlowable) {
        return Flux.from(delegate.publish(Flowable.fromPublisher(publishFlowable)));
    }

    public @NotNull Mono<Void> disconnect() {
        return Mono.fromDirect(delegate.disconnect().toFlowable());
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
