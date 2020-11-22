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

package com.hivemq.client.mqtt.mqtt5.message.disconnect;

import com.hivemq.client.internal.mqtt.message.MqttReasonCodes;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Reason Code of an {@link Mqtt5Disconnect MQTT 5 Disconnect message}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt5DisconnectReasonCode implements Mqtt5ReasonCode {

    /**
     * Disconnect normally. The server must not publish the Will message.
     */
    NORMAL_DISCONNECTION(MqttReasonCodes.SUCCESS),
    /**
     * Disconnect normally. The server must also publish the Will message.
     */
    DISCONNECT_WITH_WILL_MESSAGE(MqttReasonCodes.DISCONNECT_WITH_WILL_MESSAGE),
    /**
     * The sender either does not want to reveal the reason for the disconnect or none of the other reason codes apply.
     */
    UNSPECIFIED_ERROR(MqttReasonCodes.UNSPECIFIED_ERROR),
    /**
     * A packet could not be parsed correctly according to the MQTT specification.
     */
    MALFORMED_PACKET(MqttReasonCodes.MALFORMED_PACKET),
    /**
     * A packet contained data that is not allowed by the MQTT protocol or is inconsistent with the state of the
     * receiver.
     */
    PROTOCOL_ERROR(MqttReasonCodes.PROTOCOL_ERROR),
    /**
     * A packet is valid but can not be processed by the implementation of the receiver.
     */
    IMPLEMENTATION_SPECIFIC_ERROR(MqttReasonCodes.IMPLEMENTATION_SPECIFIC_ERROR),
    /**
     * The client is not authorized to perform a request.
     */
    NOT_AUTHORIZED(MqttReasonCodes.NOT_AUTHORIZED),
    /**
     * The server is busy and can not continue processing requests from the client.
     */
    SERVER_BUSY(MqttReasonCodes.SERVER_BUSY),
    /**
     * The server is shutting down.
     */
    SERVER_SHUTTING_DOWN(MqttReasonCodes.SERVER_SHUTTING_DOWN),
    /**
     * The authentication method is not supported or does not match the authentication method currently in use.
     */
    BAD_AUTHENTICATION_METHOD(MqttReasonCodes.BAD_AUTHENTICATION_METHOD),
    /**
     * The connection is closed because no packet has been received for 1.5 times the keep alive time.
     */
    KEEP_ALIVE_TIMEOUT(MqttReasonCodes.KEEP_ALIVE_TIMEOUT),
    /**
     * Another client using the same client identifier has connected.
     */
    SESSION_TAKEN_OVER(MqttReasonCodes.SESSION_TAKEN_OVER),
    /**
     * A packet contained a topic filter that is formed correctly but is not accepted by the server.
     */
    TOPIC_FILTER_INVALID(MqttReasonCodes.TOPIC_FILTER_INVALID),
    /**
     * A packet contained a topic name that is formed correctly but is not accepted by the receiver.
     */
    TOPIC_NAME_INVALID(MqttReasonCodes.TOPIC_NAME_INVALID),
    /**
     * The receiver has received more publications for which it has not sent PUBACK or PUBCOMP than allowed by the
     * receive maximum it sent in the CONNECT or CONNACK packet.
     */
    RECEIVE_MAXIMUM_EXCEEDED(MqttReasonCodes.RECEIVE_MAXIMUM_EXCEEDED),
    /**
     * The receiver has received a PUBLISH packet containing a topic alias which is greater than the maximum topic alias
     * it sent in the CONNECT or CONNACK packet.
     */
    TOPIC_ALIAS_INVALID(MqttReasonCodes.TOPIC_ALIAS_INVALID),
    /**
     * The receiver has received a packet with a greater size than allowed by the maximum packet size it sent in the
     * CONNECT or CONNACK packet.
     */
    PACKET_TOO_LARGE(MqttReasonCodes.PACKET_TOO_LARGE),
    /**
     * The received data rate is too high.
     */
    MESSAGE_RATE_TOO_HIGH(MqttReasonCodes.MESSAGE_RATE_TOO_HIGH),
    /**
     * An implementation or administrative imposed limit has been exceeded.
     */
    QUOTA_EXCEEDED(MqttReasonCodes.QUOTA_EXCEEDED),
    /**
     * The connection is closed due to an administrative action.
     */
    ADMINISTRATIVE_ACTION(MqttReasonCodes.ADMINISTRATIVE_ACTION),
    /**
     * A payload does not match the specified payload format indicator.
     */
    PAYLOAD_FORMAT_INVALID(MqttReasonCodes.PAYLOAD_FORMAT_INVALID),
    /**
     * The server does not support retained messages.
     */
    RETAIN_NOT_SUPPORTED(MqttReasonCodes.RETAIN_NOT_SUPPORTED),
    /**
     * The client specified a QoS greater than the maximum QoS the server sent in the CONNACK packet.
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
     * The server does not support shared subscriptions.
     */
    SHARED_SUBSCRIPTIONS_NOT_SUPPORTED(MqttReasonCodes.SHARED_SUBSCRIPTIONS_NOT_SUPPORTED),
    /**
     * The connection is closed because the connection rate is too high.
     */
    CONNECTION_RATE_EXCEEDED(MqttReasonCodes.CONNECTION_RATE_EXCEEDED),
    /**
     * The maximum connection time authorized for this connection has been exceeded.
     */
    MAXIMUM_CONNECT_TIME(MqttReasonCodes.MAXIMUM_CONNECT_TIME),
    /**
     * The server does not support subscription identifiers.
     */
    SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED(MqttReasonCodes.SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED),
    /**
     * The server does not support wildcard subscriptions.
     */
    WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED(MqttReasonCodes.WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED);

    private final int code;

    Mqtt5DisconnectReasonCode(final int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    private static final int ERROR_CODE_MIN = UNSPECIFIED_ERROR.code;
    private static final int ERROR_CODE_MAX = WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED.code;
    private static final @NotNull Mqtt5DisconnectReasonCode @NotNull [] ERROR_CODE_LOOKUP =
            new Mqtt5DisconnectReasonCode[ERROR_CODE_MAX - ERROR_CODE_MIN + 1];

    static {
        for (final Mqtt5DisconnectReasonCode reasonCode : values()) {
            if (reasonCode != NORMAL_DISCONNECTION && reasonCode != DISCONNECT_WITH_WILL_MESSAGE) {
                ERROR_CODE_LOOKUP[reasonCode.code - ERROR_CODE_MIN] = reasonCode;
            }
        }
    }

    /**
     * Returns the Disconnect Reason Code belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the Disconnect Reason Code belonging to the given byte code or <code>null</code> if the byte code is not
     *         a valid Disconnect Reason Code.
     */
    public static @Nullable Mqtt5DisconnectReasonCode fromCode(final int code) {
        if (code == NORMAL_DISCONNECTION.code) {
            return NORMAL_DISCONNECTION;
        }
        if (code == DISCONNECT_WITH_WILL_MESSAGE.code) {
            return DISCONNECT_WITH_WILL_MESSAGE;
        }
        if (code < ERROR_CODE_MIN || code > ERROR_CODE_MAX) {
            return null;
        }
        return ERROR_CODE_LOOKUP[code - ERROR_CODE_MIN];
    }

    private static final @NotNull EnumSet<Mqtt5DisconnectReasonCode> BY_CLIENT =
            EnumSet.of(NORMAL_DISCONNECTION, DISCONNECT_WITH_WILL_MESSAGE, UNSPECIFIED_ERROR, MALFORMED_PACKET,
                    PROTOCOL_ERROR, IMPLEMENTATION_SPECIFIC_ERROR, BAD_AUTHENTICATION_METHOD, TOPIC_NAME_INVALID,
                    RECEIVE_MAXIMUM_EXCEEDED, TOPIC_ALIAS_INVALID, PACKET_TOO_LARGE, MESSAGE_RATE_TOO_HIGH,
                    QUOTA_EXCEEDED, ADMINISTRATIVE_ACTION, PAYLOAD_FORMAT_INVALID);
    private static final @NotNull EnumSet<Mqtt5DisconnectReasonCode> BY_USER = EnumSet.copyOf(BY_CLIENT);

    static {
        BY_USER.removeAll(
                EnumSet.of(MALFORMED_PACKET, PROTOCOL_ERROR, BAD_AUTHENTICATION_METHOD, RECEIVE_MAXIMUM_EXCEEDED,
                        TOPIC_ALIAS_INVALID, PACKET_TOO_LARGE));
    }

    @Override
    public boolean canBeSentByServer() {
        return this != DISCONNECT_WITH_WILL_MESSAGE;
    }

    @Override
    public boolean canBeSentByClient() {
        return BY_CLIENT.contains(this);
    }

    @Override
    public boolean canBeSetByUser() {
        return BY_USER.contains(this);
    }
}
