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
 */
package org.mqttbee.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.util.KeyStoreUtil;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author David Katz
 * @author Christian Hoff
 */
class Mqtt3SmokeTest {

    private static final String KEYSTORE_PATH = "src/test/resources/testkeys/mosquitto/mosquitto.org.client.jks";
    private static final String KEYSTORE_PASS = "testkeystore";
    private static final String PRIVATE_KEY_PASS = "testkeystore";
    private static final String TRUSTSTORE_PATH = "src/test/resources/testkeys/mosquitto/cacerts.jks";
    private static final String TRUSTSTORE_PASS = "testcas";
    private final String server = "test.mosquitto.org";
    private final MqttQoS qos = MqttQoS.AT_LEAST_ONCE;
    private String topic;
    private final Mqtt3ClientExample subscribeInstance;
    private Mqtt3ClientExample publishInstance;
    private final int count = 100;
    private boolean completed = false;

    public Mqtt3SmokeTest() {
        topic = UUID.randomUUID().toString();
        subscribeInstance = new Mqtt3ClientExample(server, 1883, false, null, null, null);
    }

    @BeforeEach
    void subscribe() {
        subscribeInstance.subscribeTo(topic, qos, count).doOnComplete(() -> {
            synchronized (subscribeInstance) {
                completed = true;
                subscribeInstance.notifyAll();
            }
        }).subscribe();
    }

    @AfterEach
    void check() throws InterruptedException {
        synchronized (subscribeInstance) {
            while (!completed) {
                subscribeInstance.wait(1000);
            }
        }
        assertEquals(count, publishInstance.getPublishedCount());
        assertEquals(count, subscribeInstance.getReceivedCount());
    }

    @Test
    void mqttOverTcp() {
        publishInstance = new Mqtt3ClientExample(server, 1883, false, null, null, null);
        publishInstance.publish(topic, qos, count).blockingSubscribe();
    }

    @Test
    void mqttOverTls() throws IOException {
        TrustManagerFactory trustManagerFactory =
                KeyStoreUtil.trustManagerFromKeystore(new File(TRUSTSTORE_PATH), TRUSTSTORE_PASS);

        publishInstance = new Mqtt3ClientExample(server, 8883, true, trustManagerFactory, null, null);
        publishInstance.publish(topic, qos, count).blockingSubscribe();
    }

    @Test
    void mqttOverTlsWithClientCert() throws IOException {
        TrustManagerFactory trustManagerFactory =
                KeyStoreUtil.trustManagerFromKeystore(new File(TRUSTSTORE_PATH), TRUSTSTORE_PASS);
        KeyManagerFactory keyManagerFactory =
                KeyStoreUtil.keyManagerFromKeystore(new File(KEYSTORE_PATH), KEYSTORE_PASS, PRIVATE_KEY_PASS);

        publishInstance = new Mqtt3ClientExample(server, 8884, true, trustManagerFactory, keyManagerFactory, null);
        publishInstance.publish(topic, qos, count).blockingSubscribe();
    }

    @Test
    void mqttOverWebSockets() {
        publishInstance = new Mqtt3ClientExample(server, 8080, false, null, null, "mqtt");
        publishInstance.publish(topic, qos, count).blockingSubscribe();
    }

    @Test
    void mqttOverWebSocketsEncrypted() {
        publishInstance = new Mqtt3ClientExample(server, 8081, true, null, null, "mqtt");
        publishInstance.publish(topic, qos, count).blockingSubscribe();
    }
}
