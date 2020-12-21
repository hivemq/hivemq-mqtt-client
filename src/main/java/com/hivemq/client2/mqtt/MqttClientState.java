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

import java.util.EnumSet;

/**
 * State of an {@link MqttClient}.
 * <pre>
 * +--------------+             +------------------------+
 * | DISCONNECTED &lt;-+         +-&gt; DISCONNECTED_RECONNECT |
 * +-------+------+  \       /  +-----------+------------+
 *         |          \     /               |
 *         |           \   /                |
 * +-------v------+     \ /     +-----------v------------+
 * |  CONNECTING  +------x------+  CONNECTING_RECONNECT  |
 * +-------+------+      |      +-----------+------------+
 *         |             |                  |
 *         |             |                  |
 *         |      +------+------+           |
 *         +------&gt;  CONNECTED  &lt;-----------+
 *                +-------------+
 * </pre>
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum MqttClientState {

    /**
     * The client is disconnected.
     */
    DISCONNECTED,
    /**
     * The client is connecting.
     * <p>
     * This means the client was {@link #DISCONNECTED}, a Connect message is sent, but the ConnAck message is not
     * received yet.
     */
    CONNECTING,
    /**
     * The client is connected.
     */
    CONNECTED,
    /**
     * The client is disconnected but will reconnect.
     */
    DISCONNECTED_RECONNECT,
    /**
     * The client is reconnecting.
     * <p>
     * This means the client was {@link #DISCONNECTED_RECONNECT}, a Connect message is sent, but the ConnAck message is
     * not received yet.
     */
    CONNECTING_RECONNECT;

    private static final @NotNull EnumSet<MqttClientState> CONNECTED_OR_RECONNECT =
            EnumSet.of(CONNECTED, DISCONNECTED_RECONNECT, CONNECTING_RECONNECT);

    /**
     * @return whether the client is connected.
     */
    public boolean isConnected() {
        return this == CONNECTED;
    }

    /**
     * @return whether the client is connected or will reconnect.
     */
    public boolean isConnectedOrReconnect() {
        return CONNECTED_OR_RECONNECT.contains(this);
    }
}
