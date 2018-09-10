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

package org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubRelBuilder {

    @NotNull Mqtt5PubRelBuilder reasonString(@Nullable MqttUTF8String reasonString);

    @NotNull Mqtt5PubRelBuilder userProperties(@NotNull Mqtt5UserProperties userProperties);

    default @NotNull Mqtt5UserPropertiesBuilder<Mqtt5PubRelBuilder> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @NotNull Mqtt5PubRelReasonCode getReasonCode();

}
