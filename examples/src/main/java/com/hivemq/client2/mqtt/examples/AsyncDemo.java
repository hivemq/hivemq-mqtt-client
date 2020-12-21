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

package com.hivemq.client2.mqtt.examples;

import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client2.mqtt.mqtt5.Mqtt5Client;

import java.util.concurrent.TimeUnit;

/**
 * Small completely asynchronous example.
 *
 * @author Silvio Giebl
 */
public class AsyncDemo {

    public static void main(final String[] args) throws InterruptedException {

        final Mqtt5AsyncClient client = Mqtt5Client.builder().serverHost("broker.hivemq.com").buildAsync();

        client.connect()
                .thenAccept(connAck -> System.out.println("connected " + connAck))
                .thenCompose(v -> client.publishWith().topic("demo/topic/b").qos(MqttQos.EXACTLY_ONCE).send())
                .thenAccept(publishResult -> System.out.println("published " + publishResult))
                .thenCompose(v -> client.disconnect())
                .thenAccept(v -> System.out.println("disconnected"));

        System.out.println("see that everything above is async");
        for (int i = 0; i < 5; i++) {
            TimeUnit.MILLISECONDS.sleep(50);
            System.out.println("...");
        }
    }
}
