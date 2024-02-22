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
package com.hivemq.client.restrictions;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3RxClient;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import com.hivemq.testcontainer.junit5.HiveMQTestContainerExtension;
import io.reactivex.Flowable;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.utility.MountableFile;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Mqtt5ToMqtt3MessageForwardingIT {

    @RegisterExtension
    public final @NotNull HiveMQTestContainerExtension hivemq =
            new HiveMQTestContainerExtension().withHiveMQConfig(MountableFile.forClasspathResource("/config.xml"));

    @Test
    void mqtt5ToMqtt3_messageForwarding_rx() throws InterruptedException {

        final Mqtt3RxClient mqtt3Client = MqttClient
                .builder()
                .useMqttVersion3()
                .serverPort(hivemq.getMqttPort())
                .addConnectedListener(__ -> System.out.println("MQTTv3 client connected."))
                .addDisconnectedListener(context -> System.out.println("MQTTv3 client disconnected. (" + context.getCause().getMessage() + ")"))
                .buildRx();

        final Mqtt5RxClient mqtt5Client = MqttClient
                .builder()
                .useMqttVersion5()
                .serverPort(hivemq.getMqttPort())
                .addConnectedListener(__ -> System.out.println("MQTTv5 client connected."))
                .addDisconnectedListener(context -> System.out.println("MQTTv5 client disconnected. (" + context.getCause().getMessage() + ")"))
                .buildRx();

        final Mqtt3ConnAck mqtt3ConnAck = mqtt3Client.connect().timeout(5, TimeUnit.SECONDS).blockingGet();
        assertEquals(Mqtt3ConnAckReturnCode.SUCCESS, mqtt3ConnAck.getReturnCode());

        final Mqtt5ConnAck mqtt5ConnAck = mqtt5Client.connect().timeout(5, TimeUnit.SECONDS).blockingGet();
        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode());

        final int MESSAGE_COUNT = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(MESSAGE_COUNT);

        mqtt5Client
                .publishes(MqttGlobalPublishFilter.ALL)
                .subscribe(message -> {
                    final byte[] payload = message.getPayloadAsBytes();
                    final int i = payload[0];
                    System.out.println("MQTTv5 client received message #" + i + " from topic \"test_1\".");
                    System.out.println("MQTTv3 client sending message #" + i + " to topic \"test_2\"...");
                    try {
                        mqtt3Client
                                .publish(
                                        Flowable.just(
                                                Mqtt3Publish
                                                        .builder()
                                                        .topic("test_2")
                                                        .payload(payload)
                                                        .qos(MqttQos.EXACTLY_ONCE)
                                                        .build()
                                        )
                                )
                                .timeout(5, TimeUnit.SECONDS)
                                .subscribe();

                        countDownLatch.countDown();
                    } catch (final Exception ex) {
                        System.out.println("MQTTv3 client failed to send message #" + i + ".");
                        ex.printStackTrace();
                    }
                });

        final Mqtt5SubAck mqtt5SubAck = mqtt5Client
                .subscribeWith()
                .topicFilter("test_1")
                .qos(MqttQos.EXACTLY_ONCE)
                .applySubscribe()
                .timeout(5, TimeUnit.SECONDS)
                .blockingGet();

        assertTrue(mqtt5SubAck.getReasonCodes().contains(Mqtt5SubAckReasonCode.GRANTED_QOS_2));

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            final byte[] payload = new byte[42];
            payload[0] = (byte) i;

            System.out.println("MQTTv5 client sending message #" + i + " to topic \"test_1\"...");
            mqtt5Client
                    .publish(
                            Flowable.just(
                                    Mqtt5Publish
                                            .builder()
                                            .topic("test_1")
                                            .payload(payload)
                                            .qos(MqttQos.EXACTLY_ONCE)
                                            .build()
                            )
                    )
                    .timeout(5, TimeUnit.SECONDS)
                    .subscribe();

            TimeUnit.SECONDS.sleep(1);
        }

        assertTrue(countDownLatch.await(5 + 1, TimeUnit.SECONDS));
    }

    @Test
    void mqtt5ToMqtt3_messageForwarding_async() throws InterruptedException, ExecutionException, TimeoutException {

        final Mqtt3AsyncClient mqtt3Client = MqttClient
                .builder()
                .useMqttVersion3()
                .serverPort(hivemq.getMqttPort())
                .addConnectedListener(__ -> System.out.println("MQTTv3 client connected."))
                .addDisconnectedListener(context -> System.out.println("MQTTv3 client disconnected. (" + context.getCause().getMessage() + ")"))
                .buildAsync();

        final Mqtt5AsyncClient mqtt5Client = MqttClient
                .builder()
                .useMqttVersion5()
                .serverPort(hivemq.getMqttPort())
                .addConnectedListener(__ -> System.out.println("MQTTv5 client connected."))
                .addDisconnectedListener(context -> System.out.println("MQTTv5 client disconnected. (" + context.getCause().getMessage() + ")"))
                .buildAsync();

        final Mqtt3ConnAck mqtt3ConnAck = mqtt3Client.connect().get(5, TimeUnit.SECONDS);
        assertEquals(Mqtt3ConnAckReturnCode.SUCCESS, mqtt3ConnAck.getReturnCode());

        final Mqtt5ConnAck mqtt5ConnAck = mqtt5Client.connect().get(5, TimeUnit.SECONDS);
        assertEquals(Mqtt5ConnAckReasonCode.SUCCESS, mqtt5ConnAck.getReasonCode());

        final int MESSAGE_COUNT = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(MESSAGE_COUNT);

        final Mqtt5SubAck mqtt5SubAck = mqtt5Client
                .subscribeWith()
                .topicFilter("test_1")
                .qos(MqttQos.EXACTLY_ONCE)
                .callback(message -> {
                    final byte[] payload = message.getPayloadAsBytes();
                    final int i = payload[0];
                    System.out.println("MQTTv5 client received message #" + i + " from topic \"test_1\".");
                    System.out.println("MQTTv3 client sending message #" + i + " to topic \"test_2\"...");
                    try {
                        mqtt3Client
                                .publishWith()
                                .topic("test_2")
                                .payload(payload)
                                .qos(MqttQos.EXACTLY_ONCE)
                                .send()
                                .get(5, TimeUnit.SECONDS);

                        countDownLatch.countDown();
                    } catch (final Exception ex) {
                        System.out.println("MQTTv3 client failed to send message #" + i + ".");
                        ex.printStackTrace();
                    }
                })
                .send()
                .get(5, TimeUnit.SECONDS);

        assertTrue(mqtt5SubAck.getReasonCodes().contains(Mqtt5SubAckReasonCode.GRANTED_QOS_2));

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            final byte[] payload = new byte[42];
            payload[0] = (byte) i;

            System.out.println("MQTTv5 client sending message #" + i + " to topic \"test_1\"...");
            mqtt5Client
                    .publishWith()
                    .topic("test_1")
                    .payload(payload)
                    .qos(MqttQos.EXACTLY_ONCE)
                    .send()
                    .get(5, TimeUnit.SECONDS);

            TimeUnit.SECONDS.sleep(1);
        }

        assertTrue(countDownLatch.await(5 + 1, TimeUnit.SECONDS));
    }
}
