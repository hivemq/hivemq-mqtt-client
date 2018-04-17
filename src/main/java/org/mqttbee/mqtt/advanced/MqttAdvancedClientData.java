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

package org.mqttbee.mqtt.advanced;

import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientData;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQoS1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQoS1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQoS2ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQoS2ControlProvider;

/**
 * @author Silvio Giebl
 */
public class MqttAdvancedClientData implements Mqtt5AdvancedClientData {

    private final Mqtt5IncomingQoS1ControlProvider incomingQoS1ControlProvider;
    private final Mqtt5OutgoingQoS1ControlProvider outgoingQoS1ControlProvider;
    private final Mqtt5IncomingQoS2ControlProvider incomingQoS2ControlProvider;
    private final Mqtt5OutgoingQoS2ControlProvider outgoingQoS2ControlProvider;

    public MqttAdvancedClientData(
            @Nullable final Mqtt5IncomingQoS1ControlProvider incomingQoS1ControlProvider,
            @Nullable final Mqtt5OutgoingQoS1ControlProvider outgoingQoS1ControlProvider,
            @Nullable final Mqtt5IncomingQoS2ControlProvider incomingQoS2ControlProvider,
            @Nullable final Mqtt5OutgoingQoS2ControlProvider outgoingQoS2ControlProvider) {

        this.incomingQoS1ControlProvider = incomingQoS1ControlProvider;
        this.outgoingQoS1ControlProvider = outgoingQoS1ControlProvider;
        this.incomingQoS2ControlProvider = incomingQoS2ControlProvider;
        this.outgoingQoS2ControlProvider = outgoingQoS2ControlProvider;
    }

    @Nullable
    public Mqtt5IncomingQoS1ControlProvider getIncomingQoS1ControlProvider() {
        return incomingQoS1ControlProvider;
    }

    @Nullable
    public Mqtt5OutgoingQoS1ControlProvider getOutgoingQoS1ControlProvider() {
        return outgoingQoS1ControlProvider;
    }

    @Nullable
    public Mqtt5IncomingQoS2ControlProvider getIncomingQoS2ControlProvider() {
        return incomingQoS2ControlProvider;
    }

    @Nullable
    public Mqtt5OutgoingQoS2ControlProvider getOutgoingQoS2ControlProvider() {
        return outgoingQoS2ControlProvider;
    }

}
