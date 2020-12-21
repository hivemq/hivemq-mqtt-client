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

package com.hivemq.client2.internal.mqtt.handler.disconnect;

import com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client2.internal.rx.CompletableFlow;
import com.hivemq.client2.mqtt.lifecycle.MqttDisconnectSource;
import com.hivemq.client2.mqtt.mqtt5.exceptions.Mqtt5DisconnectException;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Event that is fired when the channel will be closed containing the cause.
 * <p>
 * Only one such event is fired in all cases:
 * <ul>
 * <li>Server sent a DISCONNECT message</li>
 * <li>Client sends a DISCONNECT message</li>
 * <li>Server closed the channel without a DISCONNECT message</li>
 * <li>Client closes the channel without a DISCONNECT message</li>
 * </ul>
 *
 * @author Silvio Giebl
 */
public class MqttDisconnectEvent {

    private final @NotNull Throwable cause;
    private final @NotNull MqttDisconnectSource source;

    MqttDisconnectEvent(final @NotNull Throwable cause, final @NotNull MqttDisconnectSource source) {
        this.cause = cause;
        this.source = source;
    }

    /**
     * @return the cause for closing of the channel.
     */
    public @NotNull Throwable getCause() {
        return cause;
    }

    /**
     * @return whether the client initiated closing of the channel.
     */
    public @NotNull MqttDisconnectSource getSource() {
        return source;
    }

    /**
     * @return the DISCONNECT message which was sent or received, otherwise null.
     */
    @Nullable MqttDisconnect getDisconnect() {
        if (cause instanceof Mqtt5DisconnectException) {
            final Mqtt5Disconnect mqttMessage = ((Mqtt5DisconnectException) cause).getMqttMessage();
            if (mqttMessage instanceof MqttDisconnect) {
                return (MqttDisconnect) mqttMessage;
            }
        }
        return null;
    }

    static class ByUser extends MqttDisconnectEvent {

        private final @NotNull CompletableFlow flow;

        ByUser(final @NotNull MqttDisconnect disconnect, final @NotNull CompletableFlow flow) {
            super(new Mqtt5DisconnectException(disconnect, "Client sent DISCONNECT"), MqttDisconnectSource.USER);
            this.flow = flow;
        }

        public @NotNull CompletableFlow getFlow() {
            return flow;
        }
    }
}
