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

package com.hivemq.client2.internal.mqtt.message;

import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.jetbrains.annotations.NotNull;

/**
 * MQTT Reason Codes according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public class MqttReasonCodes {

    public static final int SUCCESS = 0x00;
    public static final int GRANTED_QOS_1 = 0x01;
    public static final int GRANTED_QOS_2 = 0x02;
    public static final int DISCONNECT_WITH_WILL_MESSAGE = 0x04;
    public static final int NO_MATCHING_SUBSCRIBERS = 0x10;
    public static final int NO_SUBSCRIPTIONS_EXISTED = 0x11;
    public static final int CONTINUE_AUTHENTICATION = 0x18;
    public static final int REAUTHENTICATE = 0x19;
    public static final int UNSPECIFIED_ERROR = 0x80;
    public static final int MALFORMED_PACKET = 0x81;
    public static final int PROTOCOL_ERROR = 0x82;
    public static final int IMPLEMENTATION_SPECIFIC_ERROR = 0x83;
    public static final int UNSUPPORTED_PROTOCOL_VERSION = 0x84;
    public static final int CLIENT_IDENTIFIER_NOT_VALID = 0x85;
    public static final int BAD_USER_NAME_OR_PASSWORD = 0x86;
    public static final int NOT_AUTHORIZED = 0x87;
    public static final int SERVER_UNAVAILABLE = 0x88;
    public static final int SERVER_BUSY = 0x89;
    public static final int BANNED = 0x8A;
    public static final int SERVER_SHUTTING_DOWN = 0x8B;
    public static final int BAD_AUTHENTICATION_METHOD = 0x8C;
    public static final int KEEP_ALIVE_TIMEOUT = 0x8D;
    public static final int SESSION_TAKEN_OVER = 0x8E;
    public static final int TOPIC_FILTER_INVALID = 0x8F;
    public static final int TOPIC_NAME_INVALID = 0x90;
    public static final int PACKET_IDENTIFIER_IN_USE = 0x91;
    public static final int PACKET_IDENTIFIER_NOT_FOUND = 0x92;
    public static final int RECEIVE_MAXIMUM_EXCEEDED = 0x93;
    public static final int TOPIC_ALIAS_INVALID = 0x94;
    public static final int PACKET_TOO_LARGE = 0x95;
    public static final int MESSAGE_RATE_TOO_HIGH = 0x96;
    public static final int QUOTA_EXCEEDED = 0x97;
    public static final int ADMINISTRATIVE_ACTION = 0x98;
    public static final int PAYLOAD_FORMAT_INVALID = 0x99;
    public static final int RETAIN_NOT_SUPPORTED = 0x9A;
    public static final int QOS_NOT_SUPPORTED = 0x9B;
    public static final int USE_ANOTHER_SERVER = 0x9C;
    public static final int SERVER_MOVED = 0x9D;
    public static final int SHARED_SUBSCRIPTIONS_NOT_SUPPORTED = 0x9E;
    public static final int CONNECTION_RATE_EXCEEDED = 0x9F;
    public static final int MAXIMUM_CONNECT_TIME = 0xA0;
    public static final int SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED = 0xA1;
    public static final int WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED = 0xA2;

    public static boolean allErrors(final @NotNull ImmutableList<? extends Mqtt5ReasonCode> reasonCodes) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < reasonCodes.size(); i++) {
            if (!reasonCodes.get(i).isError()) {
                return false;
            }
        }
        return true;
    }

    private MqttReasonCodes() {}
}
