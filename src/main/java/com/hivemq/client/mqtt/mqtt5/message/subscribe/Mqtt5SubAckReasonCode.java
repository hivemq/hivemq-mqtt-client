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

package com.hivemq.client.mqtt.mqtt5.message.subscribe;

import com.hivemq.client.internal.mqtt.message.MqttCommonReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reason Code of an {@link Mqtt5SubAck MQTT 5 SubAck message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt5SubAckReasonCode implements Mqtt5ReasonCode {

    /**
     * The subscription is accepted and the maximum QoS sent will be QoS 0 (this might be a lower QoS than was
     * requested).
     */
    GRANTED_QOS_0(0x00),
    /**
     * The subscription is accepted and the maximum QoS sent will be QoS 1 (this might be a lower QoS than was
     * requested).
     */
    GRANTED_QOS_1(0x01),
    /**
     * The subscription is accepted and the maximum QoS sent will be QoS 2.
     */
    GRANTED_QOS_2(0x02),
    /**
     * The server either does not want to reveal the reason for the failure or none of the other reason codes apply.
     */
    UNSPECIFIED_ERROR(MqttCommonReasonCode.UNSPECIFIED_ERROR),
    /**
     * The SUBSCRIBE packet is valid but is not accepted by the server.
     */
    IMPLEMENTATION_SPECIFIC_ERROR(MqttCommonReasonCode.IMPLEMENTATION_SPECIFIC_ERROR),
    /**
     * The client is not authorized to make the subscription.
     */
    NOT_AUTHORIZED(MqttCommonReasonCode.NOT_AUTHORIZED),
    /**
     * The topic filter is formed correctly but is not accepted by the server (for this client).
     */
    TOPIC_FILTER_INVALID(MqttCommonReasonCode.TOPIC_FILTER_INVALID),
    /**
     * The specified packet identifier is already in use.
     */
    PACKET_IDENTIFIER_IN_USE(MqttCommonReasonCode.PACKET_IDENTIFIER_IN_USE),
    /**
     * An implementation or administrative imposed limit has been exceeded.
     */
    QUOTA_EXCEEDED(MqttCommonReasonCode.QUOTA_EXCEEDED),
    /**
     * The server does not support shared subscriptions (for this client).
     */
    SHARED_SUBSCRIPTIONS_NOT_SUPPORTED(MqttCommonReasonCode.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED),
    /**
     * The server does not support subscription identifiers (for this client).
     */
    SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED(MqttCommonReasonCode.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED),
    /**
     * The server does not support wildcard subscriptions (for this client).
     */
    WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED(MqttCommonReasonCode.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED);

    private static final @NotNull Mqtt5SubAckReasonCode @NotNull [] VALUES = values();

    private final int code;

    Mqtt5SubAckReasonCode(final int code) {
        this.code = code;
    }

    Mqtt5SubAckReasonCode(final @NotNull MqttCommonReasonCode reasonCode) {
        this(reasonCode.getCode());
    }

    @Override
    public int getCode() {
        return code;
    }

    /**
     * Returns the SubAck Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the SubAck Reason Code belonging to the given byte code or <code>null</code> if the byte code is not a
     *         valid SubAck Reason Code.
     */
    public static @Nullable Mqtt5SubAckReasonCode fromCode(final int code) {
        for (final Mqtt5SubAckReasonCode reasonCode : VALUES) {
            if (reasonCode.code == code) {
                return reasonCode;
            }
        }
        return null;
    }
}
