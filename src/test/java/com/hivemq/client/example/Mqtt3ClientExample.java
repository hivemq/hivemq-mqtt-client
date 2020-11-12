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

package com.hivemq.client.example;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttWebSocketConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3RxClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 * @author David Katz
 * @author Christian Hoff
 */
@SuppressWarnings("NullabilityAnnotations")
class Mqtt3ClientExample {

    private final TrustManagerFactory trustManagerFactory;
    private final KeyManagerFactory keyManagerFactory;
    private final String server;

    private final int port;
    private final boolean usesSsl;
    private final AtomicInteger receivedCount = new AtomicInteger();
    private final AtomicInteger publishedCount = new AtomicInteger();
    private final String serverPath;

    // create a client with a random UUID and connect to server
    Mqtt3ClientExample(
            @NotNull final String server,
            final int port,
            final boolean usesSsl,
            @Nullable final TrustManagerFactory trustManagerFactory,
            @Nullable final KeyManagerFactory keyManagerFactory,
            @Nullable final String serverPath) {
        this.server = server;
        this.port = port;
        this.usesSsl = usesSsl;
        this.trustManagerFactory = trustManagerFactory;
        this.keyManagerFactory = keyManagerFactory;
        this.serverPath = serverPath == null ? "mqtt" : serverPath;
    }

    Completable subscribeTo(
            final String topic, final MqttQos qos, final int countToPublish, final CountDownLatch subscribedLatch) {

        final Mqtt3RxClient client = getClient();

        // create a CONNECT message with keep alive of 10 seconds
        final Mqtt3Connect connectMessage = Mqtt3Connect.builder().keepAlive(10).build();
        // define what to do on connect, this does not connect yet
        final Single<Mqtt3ConnAck> connectScenario =
                client.connect(connectMessage).doOnSuccess(connAck -> System.out.println("connected subscriber"));

        // create a SUBSCRIBE message for the topic with QoS
        final Mqtt3Subscribe subscribeMessage = Mqtt3Subscribe.builder()
                .addSubscription(Mqtt3Subscription.builder().topicFilter(topic).maxQos(qos).build())
                .build();
        // define what to do with the publishes that match the subscription. This does not subscribe until rxJava's subscribe is called
        // NOTE: you can also subscribe without the stream, and then handle the incoming publishes on client.allPublishes()
        final Flowable<Mqtt3Publish> subscribeScenario =
                client.subscribePublishes(subscribeMessage).doOnSingle(subAck -> {
                    subscribedLatch.countDown();
                    System.out.println("subscribed to " + topic + ": return codes: " + subAck.getReturnCodes());
                }).doOnNext(publish -> {
                    if (publish.getPayload().isPresent()) {
                        final int receivedCount = this.receivedCount.incrementAndGet();
                        final String message = new String(publish.getPayloadAsBytes());
                        System.out.println(
                                "received message with payload '" + message + "' on topic '" + publish.getTopic() +
                                        "' received count: " + receivedCount);
                    } else {
                        System.out.println("received message without payload on topic '" + publish.getTopic() + "'");
            }
        });

        // define what to do when we disconnect, this does not disconnect yet
        final Completable disconnectScenario =
                client.disconnect().doOnComplete(() -> System.out.println("disconnected subscriber"));

        // now say we want to connect first and then subscribe, this does not connect and subscribe yet
        // only take the first countToPublish publications and then disconnect
        return connectScenario.ignoreElement()
                .andThen(subscribeScenario)
                .take(countToPublish)
                .ignoreElements()
                .andThen(disconnectScenario);
    }

    private boolean isNotUsingMqttPort(final int port) {
        return !(port == 1883 || port == 8883 || port == 8884);
    }

    Completable publish(final String topic, final MqttQos qos, final int countToPublish) {
        final Mqtt3RxClient client = getClient();

        // create a CONNECT message with keep alive of 10 seconds
        final Mqtt3Connect connectMessage = Mqtt3Connect.builder().keepAlive(10).build();
        // define what to do on connect, this does not connect yet
        final Single<Mqtt3ConnAck> connectScenario =
                client.connect(connectMessage).doOnSuccess(connAck -> System.out.println("connected publisher"));

        // create a stub publish and a counter
        final Mqtt3PublishBuilder.Complete publishMessageBuilder = Mqtt3Publish.builder().topic(topic).qos(qos);
        final AtomicInteger counter = new AtomicInteger();
        // fake a stream of random messages, actually not random, but an incrementing counter ;-)
        final Flowable<Mqtt3Publish> publishFlowable = Flowable.generate(emitter -> {
            if (counter.get() < countToPublish) {
                emitter.onNext(publishMessageBuilder.payload(("test " + counter.getAndIncrement()).getBytes()).build());
            } else {
                emitter.onComplete();
            }
        });

        // define what to publish and what to do when we published a message (e.g. PUBACK received), this does not publish yet
        final Flowable<Mqtt3PublishResult> publishScenario = client.publish(publishFlowable).doOnNext(publishResult -> {
            final int publishedCount = this.publishedCount.incrementAndGet();
            final Mqtt3Publish publish = publishResult.getPublish();
            System.out.println(
                    "published " + new String(publish.getPayloadAsBytes()) + " published count: " + publishedCount);
        });

        // define what to do when we disconnect, this does not disconnect yet
        final Completable disconnectScenario =
                client.disconnect().doOnComplete(() -> System.out.println("disconnected publisher"));

        // now we want to connect, then publish and take the corresponding number of pubAcks and disconnect
        // if we did not publish anything for 10 seconds also disconnect
        return connectScenario.ignoreElement().andThen(publishScenario).ignoreElements().andThen(disconnectScenario);
    }

    private Mqtt3RxClient getClient() {
        final MqttClientBuilder mqttClientBuilder =
                MqttClient.builder().identifier(UUID.randomUUID().toString()).serverHost(server).serverPort(port);

        if (usesSsl) {
            mqttClientBuilder.sslConfig(MqttClientSslConfig.builder()
                    .keyManagerFactory(keyManagerFactory)
                    .trustManagerFactory(trustManagerFactory)
                    .build());
        }

        if (isNotUsingMqttPort(port)) {
            mqttClientBuilder.webSocketConfig(MqttWebSocketConfig.builder().serverPath(serverPath).build());
        }

        return mqttClientBuilder.useMqttVersion3().buildRx();
    }

    int getReceivedCount() {
        return receivedCount.intValue();
    }

    int getPublishedCount() {
        return publishedCount.intValue();
    }
}