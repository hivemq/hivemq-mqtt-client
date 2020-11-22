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

package com.hivemq.client.mqtt.mqtt5.message.unsubscribe;

import com.hivemq.client.internal.mqtt.message.MqttReasonCodes;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reason Code of an {@link Mqtt5UnsubAck MQTT 5 UnsubAck message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt5UnsubAckReasonCode implements Mqtt5ReasonCode {

    /**
     * The subscription is deleted.
     */
    SUCCESS(MqttReasonCodes.SUCCESS),
    /**
     * No matching topic filter is being used by the client.
     */
    NO_SUBSCRIPTIONS_EXISTED(MqttReasonCodes.NO_SUBSCRIPTIONS_EXISTED),
    /**
     * The server either does not want to reveal the reason for the failure or none of the other reason codes apply.
     */
    UNSPECIFIED_ERROR(MqttReasonCodes.UNSPECIFIED_ERROR),
    /**
     * The UNSUBSCRIBE packet is valid but is not accepted by the server.
     */
    IMPLEMENTATION_SPECIFIC_ERROR(MqttReasonCodes.IMPLEMENTATION_SPECIFIC_ERROR),
    /**
     * The client is not authorized to unsubscribe.
     */
    NOT_AUTHORIZED(MqttReasonCodes.NOT_AUTHORIZED),
    /**
     * The topic filter is formed correctly but is not accepted by the server (for this client).
     */
    TOPIC_FILTER_INVALID(MqttReasonCodes.TOPIC_FILTER_INVALID),
    /**
     * The specified packet identifier is already in use.
     */
    PACKET_IDENTIFIER_IN_USE(MqttReasonCodes.PACKET_IDENTIFIER_IN_USE);

    private static final @NotNull Mqtt5UnsubAckReasonCode @NotNull [] VALUES = values();

    private final int code;

    Mqtt5UnsubAckReasonCode(final int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    /**
     * Returns the UnsubAck Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the UnsubAck Reason Code belonging to the given byte code or <code>null</code> if the byte code is not a
     *         valid UnsubAck Reason Code code.
     */
    public static @Nullable Mqtt5UnsubAckReasonCode fromCode(final int code) {
        for (final Mqtt5UnsubAckReasonCode reasonCode : VALUES) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }
}
