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

package org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec;

import org.mqttbee.annotations.DoNotImplement;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PubRecBuilder {

    @NotNull
    Mqtt5PubRecBuilder userProperties(@NotNull Mqtt5UserProperties userProperties);

    @NotNull
    default Mqtt5UserPropertiesBuilder<Mqtt5PubRecBuilder> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    @NotNull
    Mqtt5PubRecReasonCode getReasonCode();

    @NotNull
    MqttUTF8String getReasonString();

}
