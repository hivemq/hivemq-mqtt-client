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

package com.hivemq.client2.internal.mqtt.message.publish;

import com.hivemq.client2.internal.mqtt.message.MqttProperty;

/**
 * All possible MQTT PUBLISH properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public final class MqttPublishProperty {

    public static final int PAYLOAD_FORMAT_INDICATOR = MqttProperty.PAYLOAD_FORMAT_INDICATOR;
    public static final int MESSAGE_EXPIRY_INTERVAL = MqttProperty.MESSAGE_EXPIRY_INTERVAL;
    public static final int CORRELATION_DATA = MqttProperty.CORRELATION_DATA;
    public static final int CONTENT_TYPE = MqttProperty.CONTENT_TYPE;
    public static final int RESPONSE_TOPIC = MqttProperty.RESPONSE_TOPIC;
    public static final int SUBSCRIPTION_IDENTIFIER = MqttProperty.SUBSCRIPTION_IDENTIFIER;
    public static final int TOPIC_ALIAS = MqttProperty.TOPIC_ALIAS;
    public static final int USER_PROPERTY = MqttProperty.USER_PROPERTY;

    private MqttPublishProperty() {}
}
