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

package com.hivemq.client.mqtt.mqtt5.message.connect;

import com.hivemq.client.internal.mqtt.message.MqttReasonCodes;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reason Code of an {@link Mqtt5ConnAck MQTT 5 ConnAck message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt5ConnAckReasonCode implements Mqtt5ReasonCode {

    /**
     * The connection is accepted.
     */
    SUCCESS(MqttReasonCodes.SUCCESS),
    /**
     * The server either does not want to reveal the reason for the failure or none of the other reason codes apply.
     */
    UNSPECIFIED_ERROR(MqttReasonCodes.UNSPECIFIED_ERROR),
    /**
     * The CONNECT packet could not be parsed correctly according to the MQTT specification.
     */
    MALFORMED_PACKET(MqttReasonCodes.MALFORMED_PACKET),
    /**
     * The CONNECT packet contained data that is not allowed by the MQTT protocol.
     */
    PROTOCOL_ERROR(MqttReasonCodes.PROTOCOL_ERROR),
    /**
     * The CONNECT packet is valid but is not accepted by the server.
     */
    IMPLEMENTATION_SPECIFIC_ERROR(MqttReasonCodes.IMPLEMENTATION_SPECIFIC_ERROR),
    /**
     * The server does not support the version of the MQTT protocol requested by the client.
     */
    UNSUPPORTED_PROTOCOL_VERSION(MqttReasonCodes.UNSUPPORTED_PROTOCOL_VERSION),
    /**
     * The client identifier is formed correctly but is not accepted by the server.
     */
    CLIENT_IDENTIFIER_NOT_VALID(MqttReasonCodes.CLIENT_IDENTIFIER_NOT_VALID),
    /**
     * The server does not accept the user name or password specified by the client.
     */
    BAD_USER_NAME_OR_PASSWORD(MqttReasonCodes.BAD_USER_NAME_OR_PASSWORD),
    /**
     * The client is not authorized to connect.
     */
    NOT_AUTHORIZED(MqttReasonCodes.NOT_AUTHORIZED),
    /**
     * The MQTT service is not available.
     */
    SERVER_UNAVAILABLE(MqttReasonCodes.SERVER_UNAVAILABLE),
    /**
     * The server is busy. Try again later.
     */
    SERVER_BUSY(MqttReasonCodes.SERVER_BUSY),
    /**
     * This client has been banned by administrative action. Contact the server administrator.
     */
    BANNED(MqttReasonCodes.BANNED),
    /**
     * The authentication method is not supported or does not match the authentication method currently in use.
     */
    BAD_AUTHENTICATION_METHOD(MqttReasonCodes.BAD_AUTHENTICATION_METHOD),
    /**
     * The Will topic name is formed correctly but is not accepted by the server.
     */
    TOPIC_NAME_INVALID(MqttReasonCodes.TOPIC_NAME_INVALID),
    /**
     * The CONNECT packet exceeded the maximum permissible size.
     */
    PACKET_TOO_LARGE(MqttReasonCodes.PACKET_TOO_LARGE),
    /**
     * An implementation or administrative imposed limit has been exceeded.
     */
    QUOTA_EXCEEDED(MqttReasonCodes.QUOTA_EXCEEDED),
    /**
     * The Will payload does not match the specified payload format indicator.
     */
    PAYLOAD_FORMAT_INVALID(MqttReasonCodes.PAYLOAD_FORMAT_INVALID),
    /**
     * The server does not support retained messages, but the Will retain flag was set.
     */
    RETAIN_NOT_SUPPORTED(MqttReasonCodes.RETAIN_NOT_SUPPORTED),
    /**
     * The server does not support the QoS of the Will.
     */
    QOS_NOT_SUPPORTED(MqttReasonCodes.QOS_NOT_SUPPORTED),
    /**
     * The client should temporarily use another server.
     */
    USE_ANOTHER_SERVER(MqttReasonCodes.USE_ANOTHER_SERVER),
    /**
     * The client should permanently use another server.
     */
    SERVER_MOVED(MqttReasonCodes.SERVER_MOVED),
    /**
     * The connection rate limit has been exceeded.
     */
    CONNECTION_RATE_EXCEEDED(MqttReasonCodes.CONNECTION_RATE_EXCEEDED);

    private final int code;

    Mqtt5ConnAckReasonCode(final int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    private static final int ERROR_CODE_MIN = UNSPECIFIED_ERROR.code;
    private static final int ERROR_CODE_MAX = CONNECTION_RATE_EXCEEDED.code;
    private static final @NotNull Mqtt5ConnAckReasonCode @NotNull [] ERROR_CODE_LOOKUP =
            new Mqtt5ConnAckReasonCode[ERROR_CODE_MAX - ERROR_CODE_MIN + 1];

    static {
        for (final Mqtt5ConnAckReasonCode reasonCode : values()) {
            if (reasonCode != SUCCESS) {
                ERROR_CODE_LOOKUP[reasonCode.code - ERROR_CODE_MIN] = reasonCode;
            }
        }
    }

    /**
     * Returns the CONNACK Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the CONNACK Reason Code belonging to the given byte code or <code>null</code> if the byte code is not a
     *         valid CONNACK Reason Code code.
     */
    public static @Nullable Mqtt5ConnAckReasonCode fromCode(final int code) {
        if (code == SUCCESS.code) {
            return SUCCESS;
        }
        if (code < ERROR_CODE_MIN || code > ERROR_CODE_MAX) {
            return null;
        }
        return ERROR_CODE_LOOKUP[code - ERROR_CODE_MIN];
    }
}
