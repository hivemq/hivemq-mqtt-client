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

package com.hivemq.client.internal.mqtt.message.unsubscribe.mqtt3;

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubAck;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.mqtt3.message.Mqtt3MessageType;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAck;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAckReasonCode;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class Mqtt3UnsubAckView implements Mqtt3UnsubAck {

    public static final @NotNull ImmutableList<Mqtt5UnsubAckReasonCode> REASON_CODES_ALL_SUCCESS = ImmutableList.of();
    public static final @NotNull Mqtt3UnsubAckView INSTANCE = new Mqtt3UnsubAckView();
    public static final @NotNull Function<Mqtt5UnsubAck, Mqtt3UnsubAck> MAPPER = (unsubAck) -> INSTANCE;

    public static @NotNull MqttUnsubAck delegate(final int packetIdentifier) {
        return new MqttUnsubAck(
                packetIdentifier, REASON_CODES_ALL_SUCCESS, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    private Mqtt3UnsubAckView() {}

    @Override
    public @NotNull String toString() {
        return "MqttUnsubAck{}";
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Mqtt3MessageType.UNSUBACK.ordinal();
    }
}
