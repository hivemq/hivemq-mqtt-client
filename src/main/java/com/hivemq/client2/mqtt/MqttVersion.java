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

package com.hivemq.client2.mqtt;

/**
 * Available MQTT versions of {@link MqttClient MQTT clients}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum MqttVersion {

    /**
     * MQTT Version 3.1.1.
     * <p>
     * See <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/mqtt-v3.1.1.html">the latest version of the MQTT 3.1.1
     * specification</a>
     */
    MQTT_3_1_1,
    /**
     * MQTT version 5.0.
     * <p>
     * See <a href="http://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.html">the latest version of the MQTT 5.0
     * specification</a>
     */
    MQTT_5_0
}
