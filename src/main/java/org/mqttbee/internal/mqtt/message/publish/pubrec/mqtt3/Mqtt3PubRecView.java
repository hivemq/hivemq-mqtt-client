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

package org.mqttbee.internal.mqtt.message.publish.pubrec.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.internal.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.mqtt3.message.publish.pubrec.Mqtt3PubRec;
import org.mqttbee.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRecReasonCode;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3PubRecView implements Mqtt3PubRec {

    public static final @NotNull Mqtt3PubRecView INSTANCE = new Mqtt3PubRecView();

    public static @NotNull MqttPubRec delegate(final int packetIdentifier) {
        return new MqttPubRec(packetIdentifier, Mqtt5PubRecReasonCode.SUCCESS, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    private Mqtt3PubRecView() {}

    @Override
    public @NotNull String toString() {
        return "MqttPubRec{}";
    }
}
