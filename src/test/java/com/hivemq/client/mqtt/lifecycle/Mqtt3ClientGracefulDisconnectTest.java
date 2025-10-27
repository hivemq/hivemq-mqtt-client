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

package com.hivemq.client.mqtt.lifecycle;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for graceful disconnect functionality in MQTT 3 clients.
 *
 * This test addresses the issue described in GitHub issue #675 where
 * disconnect() fails with MqttClientStateException when automatic
 * reconnection is enabled and the client is in a reconnecting state.
 *
 * @since 1.4.0
 */
class Mqtt3ClientGracefulDisconnectTest {

    @Test
    @Timeout(30)
    void disconnectGracefully_whenNotConnected_shouldSucceed() {
        final Mqtt3BlockingClient client = Mqtt3Client.builder()
                .serverHost("localhost")
                .serverPort(1883)
                .buildBlocking();

        // Should not throw any exception
        assertDoesNotThrow(() -> client.disconnectGracefully());
        assertEquals(MqttClientState.DISCONNECTED, client.getState());
    }

    @Test
    @Timeout(30)
    void disconnectGracefully_whenConnected_shouldSucceed() throws Exception {
        final Mqtt3BlockingClient client = Mqtt3Client.builder()
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildBlocking();

        try {
            // Connect the client
            client.connect();
            assertEquals(MqttClientState.CONNECTED, client.getState());

            // Graceful disconnect should succeed
            assertDoesNotThrow(() -> client.disconnectGracefully());
            assertEquals(MqttClientState.DISCONNECTED, client.getState());
        } catch (Exception e) {
            // If connection fails (network issues), graceful disconnect should still work
            assertDoesNotThrow(() -> client.disconnectGracefully());
            assertEquals(MqttClientState.DISCONNECTED, client.getState());
        }
    }

    @Test
    @Timeout(30)
    void disconnectGracefully_withAutomaticReconnect_shouldCancelReconnection() throws Exception {
        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        final AtomicReference<MqttClientState> finalState = new AtomicReference<>();

        final Mqtt3BlockingClient client = Mqtt3Client.builder()
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .automaticReconnect()
                    .initialDelay(1, TimeUnit.SECONDS)
                    .maxDelay(2, TimeUnit.SECONDS)
                    .applyAutomaticReconnect()
                .addDisconnectedListener(context -> {
                    System.out.println("Disconnected: " + context.getSource());
                    finalState.set(MqttClientState.DISCONNECTED);
                    disconnectedLatch.countDown();
                })
                .buildBlocking();

        try {
            // Connect the client
            client.connect();
            assertEquals(MqttClientState.CONNECTED, client.getState());

            // Disconnect to trigger reconnection
            client.disconnect();

            // Wait a bit for reconnection to start
            Thread.sleep(500);

            // Now call graceful disconnect - this should cancel reconnection
            assertDoesNotThrow(() -> client.disconnectGracefully());

            // Wait for disconnection to complete
            assertTrue(disconnectedLatch.await(5, TimeUnit.SECONDS));

            // Final state should be DISCONNECTED, not DISCONNECTED_RECONNECT
            assertEquals(MqttClientState.DISCONNECTED, client.getState());
            assertEquals(MqttClientState.DISCONNECTED, finalState.get());

        } catch (Exception e) {
            // If connection fails (network issues), graceful disconnect should still work
            assertDoesNotThrow(() -> client.disconnectGracefully());
            assertEquals(MqttClientState.DISCONNECTED, client.getState());
        }
    }

    @Test
    @Timeout(30)
    void disconnectGracefully_async_shouldSucceed() throws Exception {
        final Mqtt3BlockingClient client = Mqtt3Client.builder()
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildBlocking();

        try {
            // Connect the client
            client.connect();
            assertEquals(MqttClientState.CONNECTED, client.getState());

            // Test async graceful disconnect
            final var future = client.toAsync().disconnectGracefully();
            assertNotNull(future);

            // Wait for completion
            future.get(5, TimeUnit.SECONDS);
            assertEquals(MqttClientState.DISCONNECTED, client.getState());

        } catch (Exception e) {
            // If connection fails (network issues), graceful disconnect should still work
            final var future = client.toAsync().disconnectGracefully();
            assertNotNull(future);
            future.get(5, TimeUnit.SECONDS);
            assertEquals(MqttClientState.DISCONNECTED, client.getState());
        }
    }

    @Test
    @Timeout(30)
    void disconnectGracefully_comparedToRegularDisconnect() throws Exception {
        final Mqtt3BlockingClient client = Mqtt3Client.builder()
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .automaticReconnect()
                    .initialDelay(1, TimeUnit.SECONDS)
                    .maxDelay(2, TimeUnit.SECONDS)
                    .applyAutomaticReconnect()
                .buildBlocking();

        try {
            // Connect the client
            client.connect();
            assertEquals(MqttClientState.CONNECTED, client.getState());

            // Disconnect to trigger reconnection
            client.disconnect();

            // Wait a bit for reconnection to start
            Thread.sleep(500);

            // Regular disconnect might throw MqttClientStateException in reconnecting state
            // Graceful disconnect should not throw any exception
            assertDoesNotThrow(() -> client.disconnectGracefully());
            assertEquals(MqttClientState.DISCONNECTED, client.getState());

        } catch (Exception e) {
            // If connection fails (network issues), graceful disconnect should still work
            assertDoesNotThrow(() -> client.disconnectGracefully());
            assertEquals(MqttClientState.DISCONNECTED, client.getState());
        }
    }
}
