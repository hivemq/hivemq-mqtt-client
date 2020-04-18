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

package com.hivemq.client.internal.mqtt.message.subscribe.suback;

import com.hivemq.client.annotations.Immutable;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.message.MqttMessageWithUserProperties;
import com.hivemq.client.internal.util.StringUtil;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAckReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSubAck extends MqttMessageWithUserProperties.WithReason.WithCodesAndId<Mqtt5SubAckReasonCode>
        implements Mqtt5SubAck {

    public MqttSubAck(
            final int packetIdentifier,
            final @NotNull ImmutableList<Mqtt5SubAckReasonCode> reasonCodes,
            final @Nullable MqttUtf8StringImpl reasonString,
            final @NotNull MqttUserPropertiesImpl userProperties) {

        super(packetIdentifier, reasonCodes, reasonString, userProperties);
    }

    @Override
    protected @NotNull String toAttributeString() {
        return "reasonCodes=" + getReasonCodes() + StringUtil.prepend(", ", super.toAttributeString());
    }

    @Override
    public @NotNull String toString() {
        return "MqttSubAck{" + toAttributeString() + "}";
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttSubAck)) {
            return false;
        }
        final MqttSubAck that = (MqttSubAck) o;

        return partialEquals(that);
    }

    @Override
    public int hashCode() {
        return partialHashCode();
    }
}
