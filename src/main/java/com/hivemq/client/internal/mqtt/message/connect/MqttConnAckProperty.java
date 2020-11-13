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

package com.hivemq.client.internal.mqtt.message.connect;

import com.hivemq.client.internal.mqtt.message.MqttProperty;

/**
 * All possible MQTT CONNACK properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public final class MqttConnAckProperty {

    public static final int SESSION_EXPIRY_INTERVAL = MqttProperty.SESSION_EXPIRY_INTERVAL;
    public static final int ASSIGNED_CLIENT_IDENTIFIER = MqttProperty.ASSIGNED_CLIENT_IDENTIFIER;
    public static final int SERVER_KEEP_ALIVE = MqttProperty.SERVER_KEEP_ALIVE;
    public static final int AUTHENTICATION_METHOD = MqttProperty.AUTHENTICATION_METHOD;
    public static final int AUTHENTICATION_DATA = MqttProperty.AUTHENTICATION_DATA;
    public static final int RESPONSE_INFORMATION = MqttProperty.RESPONSE_INFORMATION;
    public static final int SERVER_REFERENCE = MqttProperty.SERVER_REFERENCE;
    public static final int REASON_STRING = MqttProperty.REASON_STRING;
    public static final int RECEIVE_MAXIMUM = MqttProperty.RECEIVE_MAXIMUM;
    public static final int TOPIC_ALIAS_MAXIMUM = MqttProperty.TOPIC_ALIAS_MAXIMUM;
    public static final int MAXIMUM_QOS = MqttProperty.MAXIMUM_QOS;
    public static final int RETAIN_AVAILABLE = MqttProperty.RETAIN_AVAILABLE;
    public static final int USER_PROPERTY = MqttProperty.USER_PROPERTY;
    public static final int MAXIMUM_PACKET_SIZE = MqttProperty.MAXIMUM_PACKET_SIZE;
    public static final int WILDCARD_SUBSCRIPTION_AVAILABLE = MqttProperty.WILDCARD_SUBSCRIPTION_AVAILABLE;
    public static final int SUBSCRIPTION_IDENTIFIERS_AVAILABLE = MqttProperty.SUBSCRIPTION_IDENTIFIERS_AVAILABLE;
    public static final int SHARED_SUBSCRIPTION_AVAILABLE = MqttProperty.SHARED_SUBSCRIPTION_AVAILABLE;

    private MqttConnAckProperty() {}
}
