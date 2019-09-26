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
import com.hivemq.client.mqtt.reactor.mqtt5.Mqtt5ReactorClient;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MqttReactorClient implements Mqtt5ReactorClient {

    private final @NotNull Mqtt5RxClient delegate;

    public MqttReactorClient(final @NotNull Mqtt5RxClient delegate) {
        this.delegate = delegate;
    }

    public @NotNull Mono<Mqtt5ConnAck> connect(final @NotNull Mqtt5Connect connect) {
        return Mono.fromDirect(delegate.connect(connect).toFlowable());
    }

    public @NotNull Mono<Mqtt5SubAck> subscribe(final @NotNull Mqtt5Subscribe subscribe) {
        return Mono.fromDirect(delegate.subscribe(subscribe).toFlowable());
    }

    public @NotNull Flux<Mqtt5Publish> subscribeStream(final @NotNull Mqtt5Subscribe subscribe) {
        return Flux.from(delegate.subscribeStream(subscribe));
    }

    public @NotNull Flux<Mqtt5Publish> publishes(final @NotNull MqttGlobalPublishFilter filter) {
        return Flux.from(delegate.publishes(filter));
    }

    public @NotNull Mono<Mqtt5UnsubAck> unsubscribe(final @NotNull Mqtt5Unsubscribe unsubscribe) {
        return Mono.fromDirect(delegate.unsubscribe(unsubscribe).toFlowable());
    }

    public @NotNull Flux<Mqtt5PublishResult> publish(final @NotNull Publisher<Mqtt5Publish> publishFlowable) {
        return Flux.from(delegate.publish(Flowable.fromPublisher(publishFlowable)));
    }

    public @NotNull Mono<Void> reauth() {
        return Mono.fromDirect(delegate.reauth().toFlowable());
    }

    public @NotNull Mono<Void> disconnect(final @NotNull Mqtt5Disconnect disconnect) {
        return Mono.fromDirect(delegate.disconnect(disconnect).toFlowable());
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
