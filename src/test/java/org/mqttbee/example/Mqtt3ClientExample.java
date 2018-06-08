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

package org.mqttbee.example;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.MqttClient;
import org.mqttbee.api.mqtt.MqttClientSslConfig;
import org.mqttbee.api.mqtt.MqttClientSslConfigBuilder;
import org.mqttbee.api.mqtt.MqttWebsocketConfig;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt3.Mqtt3Client;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import org.mqttbee.api.util.KeyStoreUtil;
import org.mqttbee.util.ByteBufferUtil;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 * @author David Katz
 * @author Christian Hoff
 * <p>
 * A simple test app. Can be run via gradle:
 * Publisher:
 * ./gradlew -PmainClass=org.mqttbee.example.Mqtt3ClientExample \
 * -Dserver=test.mosquitto.org \
 * -Dport=8883 \
 * -Dssl=true \
 * -Dcommand=publish \
 * -Dtopic=a/b \
 * -Dkeystore=src/test/resources/testkeys/mosquitto/mosquitto.org.client.jks \
 * -Dkeystorepass=testkeystore \
 * -Dprivatekeypass=testkeystore \
 * -Dtruststore=src/test/resources/testkeys/mosquitto/cacerts.jks \
 * -Dtruststorepass=testcas \
 * execute
 *
 *  Subscriber
 * ./gradlew -PmainClass=org.mqttbee.example.Mqtt3ClientExample \
 * -Dserver=test.mosquitto.org \
 * -Dport=8883 \
 * -Dssl=true \
 * -Dcommand=subscribe \
 * -Dtopic=a/b \
 * -Dkeystore=src/test/resources/testkeys/mosquitto/mosquitto.org.client.jks \
 * -Dkeystorepass=testkeystore \
 * -Dprivatekeypass=testkeystore \
 * -Dtruststore=src/test/resources/testkeys/mosquitto/cacerts.jks \
 * -Dtruststorepass=testcas \
 * execute
 */
class Mqtt3ClientExample {
    private static final String TOPIC = "topic";
    private static final String QOS = "qos";
    private static final String COMMAND = "command";
    private static final String SUBSCRIBE = "subscribe";
    private static final String PUBLISH = "publish";
    private static final String KEYSTORE_PATH = "keystore";
    private static final String KEYSTORE_PASS = "keystorepass";
    private static final String PRIVATE_KEY_PASS = "privatekeypass";
    private static final String JKS = "JKS";
    private static final String TRUSTSTORE_PATH = "truststore";
    private static final String TRUSTSTORE_PASS = "truststorepass";
    private static final String SERVER = "server";
    private static final String PORT = "port";
    private static final String USES_SSL = "ssl";
    private static final String COUNT = "count";
    private static final String SERVER_PATH = "serverpath";

    private final TrustManagerFactory trustManagerFactory;
    private final KeyManagerFactory keyManagerFactory;
    private final String server;
    
    private final int port;
    private final boolean usesSsl;
    private AtomicInteger receivedCount = new AtomicInteger();
    private AtomicInteger publishedCount = new AtomicInteger();
    private final String serverPath;


    // create a client with a random UUID and connect to server
    Mqtt3ClientExample(
            @NotNull String server, int port, boolean usesSsl, @Nullable TrustManagerFactory trustManagerFactory,
            @Nullable KeyManagerFactory keyManagerFactory, @Nullable String serverPath) {
        this.server = server;
        this.port = port;
        this.usesSsl = usesSsl;
        this.trustManagerFactory = trustManagerFactory;
        this.keyManagerFactory = keyManagerFactory;
        this.serverPath = serverPath == null ? "mqtt" : serverPath;
    }

    Flowable<Mqtt3Publish> subscribeTo(final String topic, final MqttQoS qos, final int countToPublish) {
        final Mqtt3Client client = getClient();

        // create a CONNECT message with keep alive of 10 seconds
        final Mqtt3Connect connectMessage = Mqtt3Connect.builder().withKeepAlive(10).build();
        // define what to do on connect, this does not connect yet
        final Single<Mqtt3ConnAck> connectScenario =
                client.connect(connectMessage).doOnSuccess(connAck -> System.out.println("connected subscriber"));

        // create a SUBSCRIBE message for the topic with QoS
        final Mqtt3Subscribe subscribeMessage = Mqtt3Subscribe.builder()
                .addSubscription(
                        Mqtt3Subscription.builder().withTopicFilter(topic).withQoS(qos).build())
                .build();
        // define what to do with the publishes that match the subscription. This does not subscribe until rxJava's subscribe is called
        // NOTE: you can also subscribe without the stream, and then handle the incoming publishes on client.allPublishes()
        Flowable<Mqtt3Publish> subscribeScenario = client.subscribeWithStream(subscribeMessage)
                .doOnSingle((subAck, subscription) -> System.out.println("subscribed to " +  topic + ": return codes: " + subAck.getReturnCodes()))
                .doOnNext(publish -> {
            final Optional<ByteBuffer> payload = publish.getPayload();
            if (payload.isPresent()) {
                int receivedCount = this.receivedCount.incrementAndGet();
                final String message = new String(ByteBufferUtil.getBytes(payload.get()));
                System.out.println("received message with payload '" + message + "' on topic '" + publish.getTopic() +
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
        return connectScenario
                .toCompletable()
                .andThen(subscribeScenario)
                .take(countToPublish)
                .ignoreElements()
                .andThen(disconnectScenario.toFlowable());
    }

    private boolean isNotUsingMqttPort(int port) {
        return !(port == 1883 || port == 8883 || port == 8884);
    }

    Flowable<Mqtt3PublishResult> publish(final String topic, final MqttQoS qos, final int countToPublish) {
        final Mqtt3Client client = getClient();

        // create a CONNECT message with keep alive of 10 seconds
        final Mqtt3Connect connectMessage = Mqtt3Connect.builder().withKeepAlive(10).build();
        // define what to do on connect, this does not connect yet
        final Single<Mqtt3ConnAck> connectScenario =
                client.connect(connectMessage).doOnSuccess(connAck -> System.out.println("connected publisher"));

        // create a stub publish and a counter
        final Mqtt3PublishBuilder publishMessageBuilder =
                Mqtt3Publish.builder().withTopic(topic).withQos(qos);
        final AtomicInteger counter = new AtomicInteger();
        // fake a stream of random messages, actually not random, but an incrementing counter ;-)
        final Flowable<Mqtt3Publish> publishFlowable = Flowable.generate(emitter -> {
            if (counter.get() < countToPublish) {
                emitter.onNext(
                        publishMessageBuilder.withPayload(("test " + counter.getAndIncrement()).getBytes()).build());
            } else {
                emitter.onComplete();
            }
        });

        // define what to publish and what to do when we published a message (e.g. PUBACK received), this does not publish yet
        final Flowable<Mqtt3PublishResult> publishScenario = client.publish(publishFlowable).doOnNext(publishResult -> {
            int publishedCount = this.publishedCount.incrementAndGet();
            final Optional<ByteBuffer> payload = publishResult.getPublish().getPayload();
            payload.ifPresent(byteBuffer -> System.out.println(
                    "published " + new String(ByteBufferUtil.getBytes(byteBuffer)) + " published count: " +
                            publishedCount));
        });

        // define what to do when we disconnect, this does not disconnect yet
        final Completable disconnectScenario =
                client.disconnect().doOnComplete(() -> System.out.println("disconnected publisher"));

        // now we want to connect, then publish and take the corresponding number of pubAcks and disconnect
        // if we did not publish anything for 10 seconds also disconnect
        return connectScenario.toCompletable()
                .andThen(publishScenario)
                .take(countToPublish)
                .timeout(10, TimeUnit.SECONDS)
                .onErrorResumeNext(disconnectScenario.toFlowable())
                .ignoreElements()
                .andThen(disconnectScenario.toFlowable());
    }

    private Mqtt3Client getClient() {
        MqttClientSslConfig sslConfig = null;
        MqttWebsocketConfig websocketsConfig = null;
        if (usesSsl) {
            sslConfig = new MqttClientSslConfigBuilder()
                    .keyManagerFactory(keyManagerFactory)
                    .trustManagerFactory(trustManagerFactory)
                    .build();
        }

        if (isNotUsingMqttPort(port)) {
            websocketsConfig = MqttWebsocketConfig.builder().serverPath(serverPath).build();
        }


        return MqttClient.builder()
                .withIdentifier(UUID.randomUUID().toString())
                .withServerHost(server)
                .withServerPort(port)
                .usingSsl(sslConfig)
                .usingWebSockets(websocketsConfig)
                .usingMqtt3()
                .reactive();
    }

    private static String getProperty(final String key, final String defaultValue) {
        return System.getProperty(key) != null ? System.getProperty(key) : defaultValue;
    }

    public static void main(String[] args) throws IOException {
        String command = getProperty(COMMAND, SUBSCRIBE);
        int count = Integer.valueOf(getProperty(COUNT, "100"));
        String topic = getProperty(TOPIC, "a/b");
        MqttQoS qos = MqttQoS.fromCode(Integer.parseInt(getProperty(QOS, "1")));

        String server = getProperty(SERVER, "test.mosquitto.org");
        int port = Integer.valueOf(getProperty(PORT, "1883"));
        boolean usesSsl = Boolean.valueOf(getProperty(USES_SSL, "false"));
        String trustStorePath = getProperty(TRUSTSTORE_PATH, null);
        String trustStorePass = getProperty(TRUSTSTORE_PASS, "");
        String keyStorePath = getProperty(KEYSTORE_PATH, null);
        String keyStorePass = getProperty(KEYSTORE_PASS, "");
        String privateKeyPass = getProperty(PRIVATE_KEY_PASS, "");
        String serverPath = getProperty(SERVER_PATH, "mqtt");

        Mqtt3ClientExample instance = new Mqtt3ClientExample(
                server,
                port,
                usesSsl,
                KeyStoreUtil.trustManagerFromKeystore(new File(trustStorePath), trustStorePass),
                KeyStoreUtil.keyManagerFromKeystore(new File(keyStorePath), keyStorePass, privateKeyPass),
                serverPath);

        switch (command) {
            case SUBSCRIBE:
                instance.subscribeTo(topic, qos, count).subscribe();
                break;
            case PUBLISH:
                instance.publish(topic, qos, count).subscribe();
                break;
        }
    }

    int getReceivedCount() {
        return receivedCount.intValue();
    }

    int getPublishedCount() {
        return publishedCount.intValue();
    }
}