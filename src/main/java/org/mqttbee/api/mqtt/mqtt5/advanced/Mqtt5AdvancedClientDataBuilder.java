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

package org.mqttbee.api.mqtt.mqtt5.advanced;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQos2ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQos2ControlProvider;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;

/**
 * @author Silvio Giebl
 */
public class Mqtt5AdvancedClientDataBuilder {

    private Mqtt5IncomingQos1ControlProvider incomingQos1ControlProvider;
    private Mqtt5OutgoingQos1ControlProvider outgoingQos1ControlProvider;
    private Mqtt5IncomingQos2ControlProvider incomingQos2ControlProvider;
    private Mqtt5OutgoingQos2ControlProvider outgoingQos2ControlProvider;

    @NotNull
    public Mqtt5AdvancedClientDataBuilder incomingQos1ControlProvider(
            @NotNull final Mqtt5IncomingQos1ControlProvider incomingQos1ControlProvider) {

        this.incomingQos1ControlProvider = incomingQos1ControlProvider;
        return this;
    }

    @NotNull
    public Mqtt5AdvancedClientDataBuilder outgoingQos1ControlProvider(
            @NotNull final Mqtt5OutgoingQos1ControlProvider outgoingQos1ControlProvider) {

        this.outgoingQos1ControlProvider = outgoingQos1ControlProvider;
        return this;
    }

    @NotNull
    public Mqtt5AdvancedClientDataBuilder incomingQos2ControlProvider(
            @NotNull final Mqtt5IncomingQos2ControlProvider incomingQos2ControlProvider) {

        this.incomingQos2ControlProvider = incomingQos2ControlProvider;
        return this;
    }

    @NotNull
    public Mqtt5AdvancedClientDataBuilder outgoingQos2ControlProvider(
            @NotNull final Mqtt5OutgoingQos2ControlProvider outgoingQos2ControlProvider) {

        this.outgoingQos2ControlProvider = outgoingQos2ControlProvider;
        return this;
    }

    @NotNull
    public Mqtt5AdvancedClientData builder() {
        return new MqttAdvancedClientData(incomingQos1ControlProvider, outgoingQos1ControlProvider,
                incomingQos2ControlProvider, outgoingQos2ControlProvider);
    }

}
