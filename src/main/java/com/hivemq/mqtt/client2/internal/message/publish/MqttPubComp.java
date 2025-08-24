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

package com.hivemq.mqtt.client2.internal.message.publish;

import com.hivemq.mqtt.client2.internal.datatypes.MqttUserPropertiesImpl;
import com.hivemq.mqtt.client2.internal.datatypes.MqttUtf8StringImpl;
import com.hivemq.mqtt.client2.internal.message.MqttMessageWithUserProperties;
import com.hivemq.mqtt.client2.internal.util.StringUtil;
import com.hivemq.mqtt.client2.mqtt5.message.publish.Mqtt5PubComp;
import com.hivemq.mqtt.client2.mqtt5.message.publish.Mqtt5PubCompReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttPubComp extends MqttMessageWithUserProperties.WithReason.WithCode.WithId<Mqtt5PubCompReasonCode>
        implements Mqtt5PubComp {

    public static final @NotNull Mqtt5PubCompReasonCode DEFAULT_REASON_CODE = Mqtt5PubCompReasonCode.SUCCESS;

    public MqttPubComp(
            final int packetIdentifier,
            final @NotNull Mqtt5PubCompReasonCode reasonCode,
            final @Nullable MqttUtf8StringImpl reasonString,
            final @NotNull MqttUserPropertiesImpl userProperties) {
        super(packetIdentifier, reasonCode, reasonString, userProperties);
    }

    @Override
    public @NotNull String toString() {
        return "MqttPubComp{reasonCode=" + getReasonCode() + StringUtil.prepend(", ", super.toAttributeString()) + "}";
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttPubComp)) {
            return false;
        }
        final MqttPubComp that = (MqttPubComp) o;
        return partialEquals(that);
    }

    @Override
    public int hashCode() {
        return partialHashCode();
    }
}
