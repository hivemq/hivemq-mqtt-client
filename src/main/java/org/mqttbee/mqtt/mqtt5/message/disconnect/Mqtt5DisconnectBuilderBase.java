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

package org.mqttbee.mqtt.mqtt5.message.disconnect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;

/**
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5DisconnectBuilderBase<B extends Mqtt5DisconnectBuilderBase<B>> {

    @NotNull B reasonCode(@NotNull Mqtt5DisconnectReasonCode reasonCode);

    @NotNull B sessionExpiryInterval(long sessionExpiryInterval);

    @NotNull B noSessionExpiry();

    @NotNull B serverReference(@Nullable String serverReference);

    @NotNull B serverReference(@Nullable MqttUtf8String serverReference);

    @NotNull B reasonString(@Nullable String reasonString);

    @NotNull B reasonString(@Nullable MqttUtf8String reasonString);

    @NotNull B userProperties(@NotNull Mqtt5UserProperties userProperties);

    @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends B> userProperties();
}
