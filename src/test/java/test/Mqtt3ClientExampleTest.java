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

package test;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt.MqttClient;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3Client;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.util.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 */
class Mqtt3ClientExampleTest {

    @Test
    void subscriber() {
        // create a client with id "sub" and connect to "localhost" at port 1883
        final Mqtt3Client client = MqttClient.builder()
                .withIdentifier("sub")
                .forServerHost("broker.hivemq.com")
                .forServerPort(1883)
                .usingMqtt3()
                .reactive();

        // create a CONNECT message with keep alive of 10 seconds
        final Mqtt3Connect connectMessage = Mqtt3Connect.builder().withKeepAlive(10).build();
        // define what to do on connect, this does not connect yet
        final Single<Mqtt3ConnAck> connectScenario =
                client.connect(connectMessage).doOnSuccess(connAck -> System.out.println("connected subscriber"));

        // create a SUBSCRIBE message for the topics a/b and a/b/c with different QoS
        final Mqtt3Subscribe subscribeMessage = Mqtt3Subscribe.builder()
                .addSubscription(
                        Mqtt3Subscription.builder().withTopicFilter("a/b").withQoS(MqttQoS.EXACTLY_ONCE).build())
                .addSubscription(
                        Mqtt3Subscription.builder().withTopicFilter("a/b/c").withQoS(MqttQoS.AT_LEAST_ONCE).build())
                .build();
        // define what to do with the publishes, that match the subscription, this does not subscribe yet
        final Flowable<Mqtt3Publish> subscribeScenario = client.subscribe(subscribeMessage).doOnNext(publish -> {
            final Optional<ByteBuffer> payload = publish.getPayload();
            if (payload.isPresent()) {
                final String message = new String(ByteBufferUtil.getBytes(payload.get()));
                System.out.println(
                        "received message without payload '" + message + "' on topic '" + publish.getTopic() + "'");
            } else {
                System.out.println("received message without payload on topic '" + publish.getTopic() + "'");
            }
        });

        // now say we want to connect first and then subscribe, this does not connect and subscribe yet
        final Flowable<Mqtt3Publish> connectAndSubscribeScenario =
                connectScenario.toCompletable().andThen(subscribeScenario);

        // now we want to execute our "scenario"
        connectAndSubscribeScenario.blockingSubscribe();
    }

    @Test
    void publisherAB() {
        publisher("pub1", "a/b");
    }

    @Test
    void publisherABC() {
        publisher("pub2", "a/b/c");
    }

    private void publisher(final String clientIdentifier, final String topic) {
        // create a client with id "pub" and connect to "localhost" at port 1883
        final Mqtt3Client client = MqttClient.builder()
                .withIdentifier(clientIdentifier)
                .forServerHost("broker.hivemq.com")
                .forServerPort(1883)
                .usingMqtt3()
                .reactive();

        // create a CONNECT message with keep alive of 10 seconds
        final Mqtt3Connect connectMessage = Mqtt3Connect.builder().withKeepAlive(10).build();
        // define what to do on connect, this does not connect yet
        final Single<Mqtt3ConnAck> connectScenario =
                client.connect(connectMessage).doOnSuccess(connAck -> System.out.println("connected publisher"));

        // create a stub publish and a counter
        final Mqtt3PublishBuilder publishMessageBuilder =
                Mqtt3Publish.builder().withTopic(topic).withQos(MqttQoS.AT_LEAST_ONCE);
        final AtomicInteger counter = new AtomicInteger();
        // fake a stream of random messages, actually not random, but an incrementing counter ;-)
        final Flowable<Mqtt3Publish> publishFlowable = Flowable.generate(emitter -> {
            if (counter.get() < 100) {
                emitter.onNext(
                        publishMessageBuilder.withPayload(("test " + counter.getAndIncrement()).getBytes()).build());
            } else {
                emitter.onComplete();
            }
        });

        // define what to publish and what to do when we published a message (e.g. PUBACK received), this does not publish yet
        final Flowable<Mqtt3PublishResult> publishScenario =
                client.publish(publishFlowable).doOnNext(publishResult -> {
                    final Optional<ByteBuffer> payload = publishResult.getPublish().getPayload();
                    System.out.println("published " + new String(ByteBufferUtil.getBytes(payload.get())));
                });

        // define what to do when we disconnect, this does not disconnect yet
        final Completable disconnectScenario =
                client.disconnect().doOnComplete(() -> System.out.println("disconnected"));

        // now we want to connect, then publish and if we did not publish anything for 10 seconds disconnect
        connectScenario.toCompletable()
                .andThen(publishScenario)
                .timeout(10, TimeUnit.SECONDS)
                .onErrorResumeNext(disconnectScenario.toFlowable())
                .blockingSubscribe();
    }

}