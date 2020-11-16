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
import com.hivemq.client.internal.mqtt.message.publish.MqttPubComp;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PubComp;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PubCompReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class Mqtt3PubCompView implements Mqtt3PubComp {

    public static final @NotNull Mqtt3PubCompView INSTANCE = new Mqtt3PubCompView();

    public static @NotNull MqttPubComp delegate(final int packetIdentifier) {
        return new MqttPubComp(packetIdentifier, Mqtt5PubCompReasonCode.SUCCESS, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    private Mqtt3PubCompView() {}

    @Override
    public @NotNull String toString() {
        return "MqttPubComp{}";
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Mqtt3MessageType.PUBCOMP.ordinal();
    }
}
