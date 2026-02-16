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

package com.hivemq.client.lifecycle;

import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.github.sgtsilvio.gradle.oci.junit.jupiter.OciImages;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;

@Disabled("Need a way to delete the client subscription from the session to properly test the re-subscribe feature")
@Testcontainers
public class Mqtt5SubscribeOnReconnectIT {

    @Container
    private final @NotNull HiveMQContainer hivemq = new HiveMQContainer(OciImages.getImageName("hivemq/hivemq-ce")) //
            .withHiveMQConfig(MountableFile.forClasspathResource("/config.xml"));

    private final @NotNull AtomicBoolean reconnect = new AtomicBoolean(true);

    @AfterEach
    void tearDown() {
        reconnect.set(false);
    }

    @Test
    @Timeout(60)
    void mqtt5_resubscribe_on_reconnect() throws Exception {
        final Mqtt5BlockingClient publisher =
                Mqtt5Client.builder().identifier("PublishClient").serverPort(hivemq.getMqttPort()).buildBlocking();
        publisher.connect();

        final ConcurrentLinkedQueue<Mqtt5Publish> publishes = new ConcurrentLinkedQueue<>();
        final CountDownLatch latch = new CountDownLatch(1);
        final Mqtt5BlockingClient subscriber = Mqtt5Client.builder()
                .identifier("SubscribeClient")
                .serverPort(hivemq.getMqttPort())
                .addConnectedListener(context -> System.out.println("Subscriber connected"))
                .addDisconnectedListener(context -> {
                    System.out.println("Subscriber disconnected");
                    context.getReconnector().resubscribeIfSessionPresent(true);
                    if (reconnect.get()) {
                        System.out.println("Reconnect subscriber");
                        context.getReconnector().reconnect(true);
                        latch.countDown();
                    }
                })
                .buildBlocking();

        subscriber.toAsync().publishes(MqttGlobalPublishFilter.ALL, publishes::add);
        subscriber.connectWith().cleanStart(false).sessionExpiryInterval(3600).send();
        subscriber.subscribeWith().topicFilter("#").qos(MqttQos.AT_LEAST_ONCE).send();

        for (int i = 0; i < 10; i++) {
            publisher.toAsync().publishWith().topic("test").qos(MqttQos.AT_LEAST_ONCE).send();
        }
        await().until(() -> {
            System.out.println(publishes.size());
            return publishes.size() == 10;
        });

        subscriber.disconnect();
        latch.await();
        for (int i = 0; i < 10; i++) {
            publisher.toAsync().publishWith().topic("test").qos(MqttQos.AT_LEAST_ONCE).send();
        }
        await().until(() -> publishes.size() == 20);
    }
}
