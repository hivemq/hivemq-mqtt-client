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

package com.hivemq.client2.internal.mqtt.message.publish;

import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.message.MqttMessageWithUserProperties;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PubAck;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PubAckReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttPubAck extends MqttMessageWithUserProperties.WithReason.WithCode.WithId<Mqtt5PubAckReasonCode>
        implements Mqtt5PubAck {

    public static final @NotNull Mqtt5PubAckReasonCode DEFAULT_REASON_CODE = Mqtt5PubAckReasonCode.SUCCESS;

    public MqttPubAck(
            final int packetIdentifier,
            final @NotNull Mqtt5PubAckReasonCode reasonCode,
            final @Nullable MqttUtf8StringImpl reasonString,
            final @NotNull MqttUserPropertiesImpl userProperties) {

        super(packetIdentifier, reasonCode, reasonString, userProperties);
    }

    @Override
    public @NotNull String toString() {
        return "MqttPubAck{" + toAttributeString() + "}";
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttPubAck)) {
            return false;
        }
        final MqttPubAck that = (MqttPubAck) o;

        return partialEquals(that);
    }

    @Override
    public int hashCode() {
        return partialHashCode();
    }
}
