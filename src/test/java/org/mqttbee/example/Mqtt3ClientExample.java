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
import org.mqttbee.annotations.Nullable;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 * @author David Katz
 * <p>
 * A simple test app. Can be run via gradle:
 * Publisher:
 * ./gradlew -PmainClass=org.mqttbee.example.Mqtt3ClientExample \
 * -Dcommand=publish \
 * -Dtopic=a/b \
 * -Dkeystore=src/test/resources/testkeys/mosquitto/mosquitto.org.client.jks \
 * -Dkeystorepass=testkeystore \
 * -Dtruststore=src/test/resources/testkeys/mosquitto/cacerts.jks \
 * -Dtruststorepass=testcas \
 * execute
 *
 *  Subscriber
 * ./gradlew -PmainClass=org.mqttbee.example.Mqtt3ClientExample \
 * -Dcommand=subscribe \
 * -Dtopic=a/b \
 * -Dkeystore=src/test/resources/testkeys/mosquitto/mosquitto.org.client.jks \
 * -Dkeystorepass=testkeystore \
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
    private static final String JKS = "JKS";
    private static final String TRUSTSTORE_PATH = "truststore";
    private static final String TRUSTSTORE_PASS = "truststorepass";
    //private boolean usesSsl = false;
    // create a client with a random UUID and connect to server

    // test server from hivemq
    //final String server = "broker.mqttdashboard.com";
    //final int port = 1883;
    //final int port = 8000;

    // test server from hivemq
    //final String server = "broker.hivemq.com";
    //final String server = "52.57.95.16";
    //final int port = 8000;

    // local mosquitto on 8000
    //final String server = "localhost";
    //final int port = 8000;

    // test server from mosquitto
    private final String server = "test.mosquitto.org";

    // mosquitto websockets unencrypted
    //private final int port = 8080;


    // mosquitto websockets encrypted
    private final int port = 8884;
    private boolean usesSsl = true;
    private final KeyStore trustStore;
    private final KeyStore keyStore;
    private final String keyStorePassword;

    private Mqtt3ClientExample(KeyStore trustStore, KeyStore keyStore, String keyStorePassword) {
        this.trustStore = trustStore;
        this.keyStore = keyStore;
        this.keyStorePassword = keyStorePassword;
    }

    private void subscribe(String topic, MqttQoS qos) {


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

    private boolean isNotUsingMqttPort(int port) {
        return !(port == 1883 || port == 8883 || port == 8884);
    }

    private void publisher(final String topic, final MqttQoS qos) {
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
                    if (payload.isPresent()) {
                        System.out.println("published " + new String(ByteBufferUtil.getBytes(payload.get())));
                    }
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

    private Mqtt3Client getClient() {
        return MqttClient.builder()
                .withIdentifier(UUID.randomUUID().toString())
                .forServerHost(server)
                .withServerPort(port)
                .withServerPath("mqtt")
                .usingSSL(usesSsl)
                .keyStore(keyStore)
                .keyStorePassword(keyStorePassword)
                .trustStore(trustStore)
                .usingWebSockets(isNotUsingMqttPort(port))
                .usingMqtt3()
                .reactive();
    }

    private static String getProperty(final String key, final String defaultValue) {
        return System.getProperty(key) != null ? System.getProperty(key) : defaultValue;
    }

    private static KeyStore loadKeyStore(@Nullable String keyStorePath, @Nullable String keyStorePass) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        if (keyStorePath == null) {
            return null;
        }

        KeyStore keyStore = KeyStore.getInstance(JKS);
        InputStream readStream = new FileInputStream(keyStorePath);
        char[] keystorePassChars = keyStorePass != null ? keyStorePass.toCharArray() : new char[0];
        keyStore.load(readStream, keystorePassChars);
        return keyStore;
    }

    public static void main(String[] args) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        String command = getProperty(COMMAND, SUBSCRIBE);
        String topic = getProperty(TOPIC, "a/b");
        MqttQoS qos = MqttQoS.fromCode(Integer.parseInt(getProperty(QOS, "1")));

        String trustStorePath = getProperty(TRUSTSTORE_PATH, null);
        String trustStorePass = getProperty(TRUSTSTORE_PASS, "");
        String keyStorePath = getProperty(KEYSTORE_PATH, null);
        String keyStorePass = getProperty(KEYSTORE_PASS, "");

        Mqtt3ClientExample instance = new Mqtt3ClientExample(
                loadKeyStore(trustStorePath, trustStorePass),
                loadKeyStore(keyStorePath, keyStorePass),
                keyStorePass);

        switch (command) {
            case SUBSCRIBE:
                instance.subscribe(topic, qos);
                break;
            case PUBLISH:
                instance.publisher(topic, qos);
                break;
        }
    }
}