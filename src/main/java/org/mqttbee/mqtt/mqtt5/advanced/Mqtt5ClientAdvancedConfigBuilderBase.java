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

package org.mqttbee.mqtt.mqtt5.advanced;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.mqtt5.advanced.qos1.Mqtt5IncomingQos1Interceptor;
import org.mqttbee.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQos1Interceptor;
import org.mqttbee.mqtt.mqtt5.advanced.qos2.Mqtt5IncomingQos2Interceptor;
import org.mqttbee.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQos2Interceptor;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5ClientAdvancedConfigBuilderBase<B extends Mqtt5ClientAdvancedConfigBuilderBase<B>> {

    @NotNull B incomingQos1Interceptor(@Nullable Mqtt5IncomingQos1Interceptor incomingQos1Interceptor);

    @NotNull B outgoingQos1Interceptor(@Nullable Mqtt5OutgoingQos1Interceptor outgoingQos1Interceptor);

    @NotNull B incomingQos2Interceptor(@Nullable Mqtt5IncomingQos2Interceptor incomingQos2Interceptor);

    @NotNull B outgoingQos2Interceptor(@Nullable Mqtt5OutgoingQos2Interceptor outgoingQos2Interceptor);
}
