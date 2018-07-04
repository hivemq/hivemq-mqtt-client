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

package org.mqttbee.mqtt.message.connect.connack;

import org.mqttbee.mqtt.message.MqttProperty;

/**
 * All possible MQTT CONNACK properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttConnAckProperty {

    int SESSION_EXPIRY_INTERVAL = MqttProperty.SESSION_EXPIRY_INTERVAL;
    int ASSIGNED_CLIENT_IDENTIFIER = MqttProperty.ASSIGNED_CLIENT_IDENTIFIER;
    int SERVER_KEEP_ALIVE = MqttProperty.SERVER_KEEP_ALIVE;
    int AUTHENTICATION_METHOD = MqttProperty.AUTHENTICATION_METHOD;
    int AUTHENTICATION_DATA = MqttProperty.AUTHENTICATION_DATA;
    int RESPONSE_INFORMATION = MqttProperty.RESPONSE_INFORMATION;
    int SERVER_REFERENCE = MqttProperty.SERVER_REFERENCE;
    int REASON_STRING = MqttProperty.REASON_STRING;
    int RECEIVE_MAXIMUM = MqttProperty.RECEIVE_MAXIMUM;
    int TOPIC_ALIAS_MAXIMUM = MqttProperty.TOPIC_ALIAS_MAXIMUM;
    int MAXIMUM_QOS = MqttProperty.MAXIMUM_QOS;
    int RETAIN_AVAILABLE = MqttProperty.RETAIN_AVAILABLE;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;
    int MAXIMUM_PACKET_SIZE = MqttProperty.MAXIMUM_PACKET_SIZE;
    int WILDCARD_SUBSCRIPTION_AVAILABLE = MqttProperty.WILDCARD_SUBSCRIPTION_AVAILABLE;
    int SUBSCRIPTION_IDENTIFIER_AVAILABLE = MqttProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE;
    int SHARED_SUBSCRIPTION_AVAILABLE = MqttProperty.SHARED_SUBSCRIPTION_AVAILABLE;
}
