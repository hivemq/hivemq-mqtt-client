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

package com.hivemq.client.mqtt.examples;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Example how to handle async exceptions
 *
 * @author Adam Raźniewski
 */
public class ExceptionHandlingAsync {

    public static void main(final String[] args) throws InterruptedException {
        asyncExampleWithBlockingGet();
        completeAsyncExample();
    }

    public static void asyncExampleWithBlockingGet() throws InterruptedException {
        // Not existing mqtt broker on google.com - should return error
        final Mqtt5AsyncClient client = Mqtt5Client.builder()
                .serverHost("google.com")
                .serverPort(80)
                .buildAsync();

        CompletableFuture<Mqtt5ConnAck> completableFuture = client.connect();
        completableFuture.thenAccept(mqtt5ConnAck -> {
            System.out.println("This will be not called");
            client.disconnect();
        });
        completableFuture.exceptionally(throwable -> {
            System.out.println("This will be called async");
            return null;
        });


        try {
            Mqtt5ConnAck ack = completableFuture.get(120, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Interrupted exception");
        } catch (ExecutionException e) {
            System.out.println("ExecutionException exception - will be executed also");
        } catch (TimeoutException e) {
            System.out.println("TimeoutException exception");
        }

    }

    public static void completeAsyncExample() throws InterruptedException {
        // Not existing mqtt broker on google.com - should return error
        final Mqtt5AsyncClient client = Mqtt5Client.builder()
                .serverHost("google.com")
                .serverPort(80)
                .buildAsync();

        CompletableFuture<Mqtt5ConnAck> completableFuture = client.connect();
        completableFuture.thenAccept(mqtt5ConnAck -> {
            System.out.println("This will be not called");
            client.disconnect();
        });
        completableFuture.exceptionally(throwable -> {
            System.out.println("This will be called async");
            System.out.println("Really :O!");
            return null;
        });


        for (int i = 0; i < 3; i++) {
            TimeUnit.MILLISECONDS.sleep(1000);
            System.out.println("...");
        }

    }


}
