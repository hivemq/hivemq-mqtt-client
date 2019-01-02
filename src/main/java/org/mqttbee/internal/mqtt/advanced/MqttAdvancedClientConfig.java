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

package org.mqttbee.internal.mqtt.advanced;

import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.mqtt5.advanced.Mqtt5AdvancedClientConfig;
import org.mqttbee.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQos1Interceptor;
import org.mqttbee.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQos1Interceptor;
import org.mqttbee.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQos2Interceptor;
import org.mqttbee.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQos2Interceptor;

/**
 * @author Silvio Giebl
 */
public class MqttAdvancedClientConfig implements Mqtt5AdvancedClientConfig {

    private final @Nullable Mqtt5IncomingQos1Interceptor incomingQos1Interceptor;
    private final @Nullable Mqtt5OutgoingQos1Interceptor outgoingQos1Interceptor;
    private final @Nullable Mqtt5IncomingQos2Interceptor incomingQos2Interceptor;
    private final @Nullable Mqtt5OutgoingQos2Interceptor outgoingQos2Interceptor;

    MqttAdvancedClientConfig(
            final @Nullable Mqtt5IncomingQos1Interceptor incomingQos1Interceptor,
            final @Nullable Mqtt5OutgoingQos1Interceptor outgoingQos1Interceptor,
            final @Nullable Mqtt5IncomingQos2Interceptor incomingQos2Interceptor,
            final @Nullable Mqtt5OutgoingQos2Interceptor outgoingQos2Interceptor) {

        this.incomingQos1Interceptor = incomingQos1Interceptor;
        this.outgoingQos1Interceptor = outgoingQos1Interceptor;
        this.incomingQos2Interceptor = incomingQos2Interceptor;
        this.outgoingQos2Interceptor = outgoingQos2Interceptor;
    }

    public @Nullable Mqtt5IncomingQos1Interceptor getIncomingQos1Interceptor() {
        return incomingQos1Interceptor;
    }

    public @Nullable Mqtt5OutgoingQos1Interceptor getOutgoingQos1Interceptor() {
        return outgoingQos1Interceptor;
    }

    public @Nullable Mqtt5IncomingQos2Interceptor getIncomingQos2Interceptor() {
        return incomingQos2Interceptor;
    }

    public @Nullable Mqtt5OutgoingQos2Interceptor getOutgoingQos2Interceptor() {
        return outgoingQos2Interceptor;
    }
}
