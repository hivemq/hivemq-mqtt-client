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

package com.hivemq.client.internal.mqtt.message.publish;

import com.hivemq.client.internal.mqtt.message.MqttProperty;

/**
 * All possible MQTT PUBREL properties and its byte code according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
public final class MqttPubRelProperty {

    public static final int REASON_STRING = MqttProperty.REASON_STRING;
    public static final int USER_PROPERTY = MqttProperty.USER_PROPERTY;

    private MqttPubRelProperty() {}
}
