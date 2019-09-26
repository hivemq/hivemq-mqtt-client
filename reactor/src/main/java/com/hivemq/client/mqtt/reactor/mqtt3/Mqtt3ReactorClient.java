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

package com.hivemq.client.mqtt.reactor.mqtt3;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectView;
import com.hivemq.client.internal.mqtt.message.connect.mqtt3.Mqtt3ConnectViewBuilder;
import com.hivemq.client.internal.mqtt.message.subscribe.mqtt3.Mqtt3SubscribeViewBuilder;
import com.hivemq.client.internal.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubscribeViewBuilder;
import com.hivemq.client.internal.mqtt.reactor.mqtt3.Mqtt3ReactorClientView;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.unsuback.Mqtt3UnsubAck;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@DoNotImplement
public interface Mqtt3ReactorClient extends Mqtt3Client {

    static @NotNull Mqtt3ReactorClient from(final @NotNull Mqtt3Client client) {
        if (client instanceof Mqtt3ReactorClient) {
            return (Mqtt3ReactorClient) client;
        }
        return new Mqtt3ReactorClientView(client.toRx());
    }

    default @NotNull Mono<Mqtt3ConnAck> connect() {
        return connect(Mqtt3ConnectView.DEFAULT);
    }

    @NotNull Mono<Mqtt3ConnAck> connect(@NotNull Mqtt3Connect connect);

    default @NotNull Mqtt3ConnectBuilder.Nested<Mono<Mqtt3ConnAck>> connectWith() {
        return new Mqtt3ConnectViewBuilder.Nested<>(this::connect);
    }

    @NotNull Mono<Mqtt3SubAck> subscribe(@NotNull Mqtt3Subscribe subscribe);

    default @NotNull Mqtt3SubscribeBuilder.Nested.Start<Mono<Mqtt3SubAck>> subscribeWith() {
        return new Mqtt3SubscribeViewBuilder.Nested<>(this::subscribe);
    }

    @NotNull Flux<Mqtt3Publish> subscribeStream(@NotNull Mqtt3Subscribe subscribe);

    default @NotNull Mqtt3SubscribeBuilder.Nested.Start<Flux<Mqtt3Publish>> subscribeStreamWith() {
        return new Mqtt3SubscribeViewBuilder.Nested<>(this::subscribeStream);
    }

    @NotNull Flux<Mqtt3Publish> publishes(@NotNull MqttGlobalPublishFilter filter);

    @NotNull Mono<Mqtt3UnsubAck> unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

    default @NotNull Mqtt3UnsubscribeBuilder.Nested.Start<Mono<Mqtt3UnsubAck>> unsubscribeWith() {
        return new Mqtt3UnsubscribeViewBuilder.Nested<>(this::unsubscribe);
    }

    @NotNull Flux<Mqtt3PublishResult> publish(@NotNull Publisher<Mqtt3Publish> publishFlowable);

    @NotNull Mono<Void> disconnect();
}
