/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt.message.publish.mqtt3;

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.publish.MqttPubRec;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PubRec;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PubRecReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
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

    @Override
    public boolean equals(final @Nullable Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Mqtt3MessageType.PUBREC.ordinal();
    }
}
