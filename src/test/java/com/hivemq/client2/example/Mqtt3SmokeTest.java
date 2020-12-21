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

package com.hivemq.client2.example;

import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.util.KeyStoreUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author David Katz
 * @author Christian Hoff
 */
class Mqtt3SmokeTest {

    private static final String KEYSTORE_PATH = "testkeys/mosquitto/mosquitto.org.client.jks";
    private static final String KEYSTORE_PASS = "testkeystore";
    private static final String PRIVATE_KEY_PASS = "testkeystore";
    private static final String TRUSTSTORE_PATH = "testkeys/mosquitto/cacerts.jks";
    private static final String TRUSTSTORE_PASS = "testcas";
    private final String server = "test.mosquitto.org";
    private final MqttQos qos = MqttQos.AT_LEAST_ONCE;
    private final String topic;
    private final Mqtt3ClientExample subscribeInstance;
    private Mqtt3ClientExample publishInstance;
    private final int count = 100;
    private CountDownLatch receivedLatch;

    Mqtt3SmokeTest() {
        topic = UUID.randomUUID().toString();
        subscribeInstance = new Mqtt3ClientExample(server, 1883, false, null, null, null);
    }

    @BeforeEach
    void subscribe() throws InterruptedException {
        final CountDownLatch subscribedLatch = new CountDownLatch(1);
        receivedLatch = new CountDownLatch(1);
        subscribeInstance.subscribeTo(topic, qos, count, subscribedLatch)
                .doOnComplete(() -> receivedLatch.countDown())
                .subscribe();
        assertTrue(subscribedLatch.await(10, TimeUnit.SECONDS));
    }

    @AfterEach
    void check() throws InterruptedException {
        assertTrue(receivedLatch.await(10, TimeUnit.SECONDS));
        assertEquals(count, publishInstance.getPublishedCount());
        assertEquals(count, subscribeInstance.getReceivedCount());
    }

    @Test
    @Disabled("test.mosquitto.org down")
    void mqttOverTcp() {
        publishInstance = new Mqtt3ClientExample(server, 1883, false, null, null, null);
        assertTrue(publishInstance.publish(topic, qos, count).blockingAwait(10, TimeUnit.SECONDS));
    }

    @Test
    @Disabled("test.mosquitto.org down")
    void mqttOverTls() throws IOException, URISyntaxException {
        final TrustManagerFactory trustManagerFactory = KeyStoreUtil.trustManagerFromKeystore(
                new File(getClass().getClassLoader().getResource(TRUSTSTORE_PATH).toURI()), TRUSTSTORE_PASS);

        publishInstance = new Mqtt3ClientExample(server, 8883, true, trustManagerFactory, null, null);
        assertTrue(publishInstance.publish(topic, qos, count).blockingAwait(10, TimeUnit.SECONDS));
    }

    @Test
    @Disabled("test.mosquitto.org down")
    void mqttOverTlsWithClientCert() throws IOException, URISyntaxException {
        final TrustManagerFactory trustManagerFactory = KeyStoreUtil.trustManagerFromKeystore(
                new File(getClass().getClassLoader().getResource(TRUSTSTORE_PATH).toURI()), TRUSTSTORE_PASS);
        final KeyManagerFactory keyManagerFactory = KeyStoreUtil.keyManagerFromKeystore(
                new File(getClass().getClassLoader().getResource(KEYSTORE_PATH).toURI()), KEYSTORE_PASS,
                PRIVATE_KEY_PASS);

        publishInstance = new Mqtt3ClientExample(server, 8884, true, trustManagerFactory, keyManagerFactory, null);
        assertTrue(publishInstance.publish(topic, qos, count).blockingAwait(10, TimeUnit.SECONDS));
    }

    @Test
    @Disabled("test.mosquitto.org down")
    void mqttOverWebSockets() {
        publishInstance = new Mqtt3ClientExample(server, 8080, false, null, null, "mqtt");
        assertTrue(publishInstance.publish(topic, qos, count).blockingAwait(10, TimeUnit.SECONDS));
    }

    @Test
    @Disabled("test.mosquitto.org down")
    void mqttOverWebSocketsEncrypted() {
        publishInstance = new Mqtt3ClientExample(server, 8081, true, null, null, "mqtt");
        assertTrue(publishInstance.publish(topic, qos, count).blockingAwait(10, TimeUnit.SECONDS));
    }

}
