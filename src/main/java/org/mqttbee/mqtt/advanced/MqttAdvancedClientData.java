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

import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.advanced.Mqtt5AdvancedClientData;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQos2ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQos2ControlProvider;

/**
 * @author Silvio Giebl
 */
public class MqttAdvancedClientData implements Mqtt5AdvancedClientData {

    private final Mqtt5IncomingQos1ControlProvider incomingQos1ControlProvider;
    private final Mqtt5OutgoingQos1ControlProvider outgoingQos1ControlProvider;
    private final Mqtt5IncomingQos2ControlProvider incomingQos2ControlProvider;
    private final Mqtt5OutgoingQos2ControlProvider outgoingQos2ControlProvider;

    public MqttAdvancedClientData(
            @Nullable final Mqtt5IncomingQos1ControlProvider incomingQos1ControlProvider,
            @Nullable final Mqtt5OutgoingQos1ControlProvider outgoingQos1ControlProvider,
            @Nullable final Mqtt5IncomingQos2ControlProvider incomingQos2ControlProvider,
            @Nullable final Mqtt5OutgoingQos2ControlProvider outgoingQos2ControlProvider) {

        this.incomingQos1ControlProvider = incomingQos1ControlProvider;
        this.outgoingQos1ControlProvider = outgoingQos1ControlProvider;
        this.incomingQos2ControlProvider = incomingQos2ControlProvider;
        this.outgoingQos2ControlProvider = outgoingQos2ControlProvider;
    }

    @Nullable
    public Mqtt5IncomingQos1ControlProvider getIncomingQos1ControlProvider() {
        return incomingQos1ControlProvider;
    }

    @Nullable
    public Mqtt5OutgoingQos1ControlProvider getOutgoingQos1ControlProvider() {
        return outgoingQos1ControlProvider;
    }

    @Nullable
    public Mqtt5IncomingQos2ControlProvider getIncomingQos2ControlProvider() {
        return incomingQos2ControlProvider;
    }

    @Nullable
    public Mqtt5OutgoingQos2ControlProvider getOutgoingQos2ControlProvider() {
        return outgoingQos2ControlProvider;
    }

}
