/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.api.mqtt.mqtt5.message.publish.puback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.internal.mqtt.message.MqttCommonReasonCode;

import java.util.EnumSet;

/**
 * MQTT Reason Codes that can be used in PUBACK packets according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public enum Mqtt5PubAckReasonCode implements Mqtt5ReasonCode {

    SUCCESS(MqttCommonReasonCode.SUCCESS),
    NO_MATCHING_SUBSCRIBERS(MqttCommonReasonCode.NO_MATCHING_SUBSCRIBERS),
    UNSPECIFIED_ERROR(MqttCommonReasonCode.UNSPECIFIED_ERROR),
    IMPLEMENTATION_SPECIFIC_ERROR(MqttCommonReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    NOT_AUTHORIZED(MqttCommonReasonCode.NOT_AUTHORIZED),
    TOPIC_NAME_INVALID(MqttCommonReasonCode.TOPIC_NAME_INVALID),
    PACKET_IDENTIFIER_IN_USE(MqttCommonReasonCode.PACKET_IDENTIFIER_IN_USE),
    QUOTA_EXCEEDED(MqttCommonReasonCode.QUOTA_EXCEEDED),
    PAYLOAD_FORMAT_INVALID(MqttCommonReasonCode.PAYLOAD_FORMAT_INVALID);

    private static final @NotNull Mqtt5PubAckReasonCode[] VALUES = values();

    private final int code;

    Mqtt5PubAckReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5PubAckReasonCode(final @NotNull MqttCommonReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    /**
     * @return the byte code of this PUBACK Reason Code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the PUBACK Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the PUBACK Reason Code belonging to the given byte code or null if the byte code is not a valid PUBACK
     *         Reason Code code.
     */
    public static @Nullable Mqtt5PubAckReasonCode fromCode(final int code) {
        for (final Mqtt5PubAckReasonCode reasonCode : VALUES) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }

    private static final @NotNull EnumSet<Mqtt5PubAckReasonCode> BY_USER =
            EnumSet.of(SUCCESS, UNSPECIFIED_ERROR, IMPLEMENTATION_SPECIFIC_ERROR, NOT_AUTHORIZED, TOPIC_NAME_INVALID,
                    QUOTA_EXCEEDED, PAYLOAD_FORMAT_INVALID);

    @Override
    public boolean canBeSentByClient() {
        return this != NO_MATCHING_SUBSCRIBERS;
    }

    @Override
    public boolean canBeSetByUser() {
        return BY_USER.contains(this);
    }
}
