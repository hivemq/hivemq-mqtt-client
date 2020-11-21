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

package com.hivemq.client.mqtt.examples;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.lifecycle.Mqtt5DisconnectedContext;

import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Disable internet connection to see automatic reconnect in action.
 *
 * @author Silvio Giebl
 */
// @formatter:off
public class ReconnectStrategy {

    public static void main(final String[] args) throws InterruptedException {
//        defaultReconnect();
//        customizedReconnect();
        completelyCustom();
    }

    public static void defaultReconnect() {

        final Mqtt5BlockingClient client1 = Mqtt5Client.builder()
                .serverHost("broker.hivemq.com")
                .automaticReconnect() // exponential backoff, 1s initial, doubled up to 2min, random delays +-25%
                .buildBlocking();
    }

    public static void customizedReconnect() throws InterruptedException {

        final Mqtt5BlockingClient client2 = Mqtt5Client.builder()
                .serverHost("broker.hivemq.com")
                .automaticReconnectWith()
                    .initialDelay(3, TimeUnit.SECONDS)
                    .maxDelay(10, TimeUnit.SECONDS)
                    .applyAutomaticReconnect()
                .addConnectedListener(context -> System.out.println("connected " + LocalTime.now()))
                .addDisconnectedListener(context -> System.out.println("disconnected " + LocalTime.now()))
                .buildBlocking();

        client2.connectWith().keepAlive(2).send(); // short keep alive value so disabling internet connection triggers connection lost

        TimeUnit.MINUTES.sleep(3);
        client2.toAsync().disconnect();
    }

    private static void completelyCustom() {

        final Mqtt5BlockingClient client3 = Mqtt5Client.builder()
                .serverHost("broker.hivemq.com")
                // custom reconnect strategy is just a DisconnectedListener
                .addDisconnectedListener(context -> {
                    context.getReconnector()
                            .reconnect(true) // always reconnect (includes calling disconnect)
                            .delay(2L * context.getReconnector().getAttempts(), TimeUnit.SECONDS); // linear scaling delay
                })
                // multiple DisconnectedListener can form a reconnect strategy
                .addDisconnectedListener(context -> {
                    final Mqtt5DisconnectedContext context5 = (Mqtt5DisconnectedContext) context;
                    context5.getReconnector()
                            .reconnectWhen(getOAuthToken(), (token, throwable) -> { // first reconnect would be delayed 2s but OAuth server needs more time
                                if (token != null) {
                                    context5.getReconnector().connectWith()
                                            .simpleAuthWith().password(token).applySimpleAuth() // set OAuth token as password
                                            .applyConnect();
                                } else {
                                    context5.getReconnector().reconnect(false); // cancel reconnect if OAuth query failed
                                }
                            });
                })
                .addConnectedListener(context -> System.out.println("connected " + LocalTime.now()))
                .addDisconnectedListener(context -> System.out.println("disconnected " + LocalTime.now()))
                .buildBlocking();

        client3.connect();
        client3.disconnect();
    }

    private static CompletableFuture<byte[]> getOAuthToken() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                for(int i = 0; i < 5; i++) {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println("OAuth server is slow to respond ...");
                }
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            return new byte[] {1, 2, 3};
        });
    }
}
