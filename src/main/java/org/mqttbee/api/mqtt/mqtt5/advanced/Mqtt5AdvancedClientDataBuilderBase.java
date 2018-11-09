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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQos2ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQos2ControlProvider;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5AdvancedClientDataBuilderBase<B extends Mqtt5AdvancedClientDataBuilderBase<B>> {

    @NotNull B incomingQos1ControlProvider(@Nullable Mqtt5IncomingQos1ControlProvider incomingQos1ControlProvider);

    @NotNull B outgoingQos1ControlProvider(@Nullable Mqtt5OutgoingQos1ControlProvider outgoingQos1ControlProvider);

    @NotNull B incomingQos2ControlProvider(@Nullable Mqtt5IncomingQos2ControlProvider incomingQos2ControlProvider);

    @NotNull B outgoingQos2ControlProvider(@Nullable Mqtt5OutgoingQos2ControlProvider outgoingQos2ControlProvider);
}
