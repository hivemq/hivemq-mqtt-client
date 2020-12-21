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

package com.hivemq.client2.mqtt.lifecycle;

/**
 * Source which triggers disconnection.
 *
 * @author Silvio Giebl
 * @since 1.1
 */
public enum MqttDisconnectSource {

    /**
     * The user explicitly called disconnect.
     */
    USER,

    /**
     * The client itself (without interaction of the user) sent a Disconnect message or closed the connection without a
     * Disconnect message.
     * <p>
     * This can happen if
     * <ul>
     *   <li>an initialization error occurs (e.g. unknown host)
     *   <li>an operation times out (e.g. connect, ping)
     *   <li>a network error occurs
     *   <li>the server does not conform to the MQTT specification (e.g. malformed packet, protocol error)
     * </ul>
     */
    CLIENT,

    /**
     * The server sent a Disconnect message or closed the connection without a Disconnect message.
     */
    SERVER
}
