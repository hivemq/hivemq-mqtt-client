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

package com.hivemq.client.mqtt.mqtt5.message.publish;

import com.hivemq.client.internal.mqtt.message.MqttCommonReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Reason Code of an {@link Mqtt5PubRec MQTT 5 PubRec message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt5PubRecReasonCode implements Mqtt5ReasonCode {

    /**
     * The message is accepted. Publication of the QoS 2 message proceeds.
     */
    SUCCESS(MqttCommonReasonCode.SUCCESS),
    /**
     * The message is accepted but there are no subscribers.
     */
    NO_MATCHING_SUBSCRIBERS(MqttCommonReasonCode.NO_MATCHING_SUBSCRIBERS),
    /**
     * The receiver either does not want to reveal the reason for the failure or none of the other reason codes apply.
     */
    UNSPECIFIED_ERROR(MqttCommonReasonCode.UNSPECIFIED_ERROR),
    /**
     * The PUBLISH packet is valid but is not accepted by the receiver.
     */
    IMPLEMENTATION_SPECIFIC_ERROR(MqttCommonReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    /**
     * The sender is not authorized to make the publication.
     */
    NOT_AUTHORIZED(MqttCommonReasonCode.NOT_AUTHORIZED),
    /**
     * The topic name is formed correctly but is not accepted by the receiver.
     */
    TOPIC_NAME_INVALID(MqttCommonReasonCode.TOPIC_NAME_INVALID),
    /**
     * The packet identifier is already in use. This might indicate a mismatch between the session state on the client
     * and server.
     */
    PACKET_IDENTIFIER_IN_USE(MqttCommonReasonCode.PACKET_IDENTIFIER_IN_USE),
    /**
     * An implementation or administrative imposed limit has been exceeded.
     */
    QUOTA_EXCEEDED(MqttCommonReasonCode.QUOTA_EXCEEDED),
    /**
     * The payload does not match the specified payload format indicator.
     */
    PAYLOAD_FORMAT_INVALID(MqttCommonReasonCode.PAYLOAD_FORMAT_INVALID);

    private static final @NotNull Mqtt5PubRecReasonCode @NotNull [] VALUES = values();

    private final int code;

    Mqtt5PubRecReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5PubRecReasonCode(final @NotNull MqttCommonReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    @Override
    public int getCode() {
        return code;
    }

    /**
     * Returns the PubRec Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the PubRec Reason Code belonging to the given byte code or <code>null</code> if the byte code is not a
     *         valid PubRec Reason Code.
     */
    public static @Nullable Mqtt5PubRecReasonCode fromCode(final int code) {
        for (final Mqtt5PubRecReasonCode reasonCode : VALUES) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }

    private static final @NotNull EnumSet<Mqtt5PubRecReasonCode> BY_USER =
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
