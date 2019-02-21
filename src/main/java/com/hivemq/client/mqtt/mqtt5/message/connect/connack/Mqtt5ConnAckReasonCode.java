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

package com.hivemq.client.mqtt.mqtt5.message.connect.connack;

import com.hivemq.client.internal.mqtt.message.MqttCommonReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reason Code of a {@link Mqtt5ConnAck MQTT 5 ConnAck message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt5ConnAckReasonCode implements Mqtt5ReasonCode {

    SUCCESS(MqttCommonReasonCode.SUCCESS),
    UNSPECIFIED_ERROR(MqttCommonReasonCode.UNSPECIFIED_ERROR),
    MALFORMED_PACKET(MqttCommonReasonCode.MALFORMED_PACKET),
    PROTOCOL_ERROR(MqttCommonReasonCode.PROTOCOL_ERROR),
    IMPLEMENTATION_SPECIFIC_ERROR(MqttCommonReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    UNSUPPORTED_PROTOCOL_VERSION(0x84),
    CLIENT_IDENTIFIER_NOT_VALID(0x85),
    BAD_USER_NAME_OR_PASSWORD(0x86),
    NOT_AUTHORIZED(MqttCommonReasonCode.NOT_AUTHORIZED),
    SERVER_UNAVAILABLE(0x88),
    SERVER_BUSY(MqttCommonReasonCode.SERVER_BUSY),
    BANNED(0x8A),
    BAD_AUTHENTICATION_METHOD(MqttCommonReasonCode.BAD_AUTHENTICATION_METHOD),
    TOPIC_NAME_INVALID(MqttCommonReasonCode.TOPIC_NAME_INVALID),
    PACKET_TOO_LARGE(MqttCommonReasonCode.PACKET_TOO_LARGE),
    QUOTA_EXCEEDED(MqttCommonReasonCode.QUOTA_EXCEEDED),
    PAYLOAD_FORMAT_INVALID(MqttCommonReasonCode.PAYLOAD_FORMAT_INVALID),
    RETAIN_NOT_SUPPORTED(MqttCommonReasonCode.RETAIN_NOT_SUPPORTED),
    QOS_NOT_SUPPORTED(MqttCommonReasonCode.QOS_NOT_SUPPORTED),
    USE_ANOTHER_SERVER(MqttCommonReasonCode.USE_ANOTHER_SERVER),
    SERVER_MOVED(MqttCommonReasonCode.SERVER_MOVED),
    CONNECTION_RATE_EXCEEDED(MqttCommonReasonCode.CONNECTION_RATE_EXCEEDED);

    private final int code;

    Mqtt5ConnAckReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5ConnAckReasonCode(final @NotNull MqttCommonReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    @Override
    public int getCode() {
        return code;
    }

    private static final int ERROR_CODE_MIN = UNSPECIFIED_ERROR.code;
    private static final int ERROR_CODE_MAX = CONNECTION_RATE_EXCEEDED.code;
    private static final @NotNull Mqtt5ConnAckReasonCode[] ERROR_CODE_LOOKUP =
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
     * @return the CONNACK Reason Code belonging to the given byte code or null if the byte code is not a valid CONNACK
     *         Reason Code code.
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
