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

package com.hivemq.client2.mqtt;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Silvio Giebl
 */
class MqttClientStateTest {

    @ParameterizedTest
    @EnumSource(value = MqttClientState.class, names = {"CONNECTED"})
    void isConnected_true(final @NotNull MqttClientState clientState) {
        assertTrue(clientState.isConnected());
    }

    @ParameterizedTest
    @EnumSource(value = MqttClientState.class,
            names = {"DISCONNECTED", "CONNECTING", "DISCONNECTED_RECONNECT", "CONNECTING_RECONNECT"})
    void isConnected_false(final @NotNull MqttClientState clientState) {
        assertFalse(clientState.isConnected());
    }

    @ParameterizedTest
    @EnumSource(value = MqttClientState.class, names = {"CONNECTED", "DISCONNECTED_RECONNECT", "CONNECTING_RECONNECT"})
    void isConnectedOrReconnect_true(final @NotNull MqttClientState clientState) {
        assertTrue(clientState.isConnectedOrReconnect());
    }

    @ParameterizedTest
    @EnumSource(value = MqttClientState.class, names = {"DISCONNECTED", "CONNECTING"})
    void isConnectedOrReconnect_false(final @NotNull MqttClientState clientState) {
        assertFalse(clientState.isConnectedOrReconnect());
    }
}