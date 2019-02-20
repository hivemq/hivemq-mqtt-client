/*
 * Copyright 2018 The HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.message.connect;

import com.hivemq.client.internal.mqtt.message.MqttProperty;

/**
 * All possible MQTT CONNECT properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public interface MqttConnectProperty {

    int SESSION_EXPIRY_INTERVAL = MqttProperty.SESSION_EXPIRY_INTERVAL;
    int AUTHENTICATION_METHOD = MqttProperty.AUTHENTICATION_METHOD;
    int AUTHENTICATION_DATA = MqttProperty.AUTHENTICATION_DATA;
    int REQUEST_PROBLEM_INFORMATION = MqttProperty.REQUEST_PROBLEM_INFORMATION;
    int REQUEST_RESPONSE_INFORMATION = MqttProperty.REQUEST_RESPONSE_INFORMATION;
    int RECEIVE_MAXIMUM = MqttProperty.RECEIVE_MAXIMUM;
    int TOPIC_ALIAS_MAXIMUM = MqttProperty.TOPIC_ALIAS_MAXIMUM;
    int USER_PROPERTY = MqttProperty.USER_PROPERTY;
    int MAXIMUM_PACKET_SIZE = MqttProperty.MAXIMUM_PACKET_SIZE;

}
