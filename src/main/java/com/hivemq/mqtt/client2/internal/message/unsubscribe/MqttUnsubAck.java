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

package com.hivemq.mqtt.client2.internal.message.unsubscribe;

import com.hivemq.mqtt.client2.internal.collections.ImmutableList;
import com.hivemq.mqtt.client2.internal.datatypes.MqttUserPropertiesImpl;
import com.hivemq.mqtt.client2.internal.datatypes.MqttUtf8StringImpl;
import com.hivemq.mqtt.client2.internal.message.MqttMessageWithUserProperties;
import com.hivemq.mqtt.client2.internal.util.StringUtil;
import com.hivemq.mqtt.client2.mqtt5.message.unsubscribe.Mqtt5UnsubAck;
import com.hivemq.mqtt.client2.mqtt5.message.unsubscribe.Mqtt5UnsubAckReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class MqttUnsubAck extends MqttMessageWithUserProperties.WithReason.WithCodesAndId<Mqtt5UnsubAckReasonCode>
        implements Mqtt5UnsubAck {

    public MqttUnsubAck(
            final int packetIdentifier,
            final @NotNull ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes,
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
        return "MqttUnsubAck{" + toAttributeString() + "}";
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttUnsubAck)) {
            return false;
        }
        final MqttUnsubAck that = (MqttUnsubAck) o;
        return partialEquals(that);
    }

    @Override
    public int hashCode() {
        return partialHashCode();
    }
}
