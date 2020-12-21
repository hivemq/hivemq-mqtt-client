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
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.util.MqttChecks;
import com.hivemq.client2.internal.util.Checks;
import com.hivemq.client2.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client2.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PubAckBuilder;
import com.hivemq.client2.mqtt.mqtt5.message.publish.Mqtt5PubAckReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class MqttPubAckBuilder implements Mqtt5PubAckBuilder {

    private final @NotNull MqttStatefulPublish publish;
    private @NotNull Mqtt5PubAckReasonCode reasonCode = MqttPubAck.DEFAULT_REASON_CODE;
    private @Nullable MqttUtf8StringImpl reasonString;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttPubAckBuilder(final @NotNull MqttStatefulPublish publish) {
        this.publish = publish;
    }

    @Override
    public @NotNull MqttPubAckBuilder reasonCode(final @Nullable Mqtt5PubAckReasonCode reasonCode) {
        this.reasonCode = Checks.notNull(reasonCode, "Reason code");
        return this;
    }

    @Override
    public @NotNull MqttPubAckBuilder reasonString(final @Nullable String reasonString) {
        this.reasonString = MqttChecks.reasonString(reasonString);
        return this;
    }

    @Override
    public @NotNull MqttPubAckBuilder reasonString(final @Nullable MqttUtf8String reasonString) {
        this.reasonString = MqttChecks.reasonString(reasonString);
        return this;
    }

    @Override
    public @NotNull MqttPubAckBuilder userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        this.userProperties = MqttChecks.userProperties(userProperties);
        return this;
    }

    @Override
    public MqttUserPropertiesImplBuilder.@NotNull Nested<MqttPubAckBuilder> userPropertiesWith() {
        return new MqttUserPropertiesImplBuilder.Nested<>(userProperties, this::userProperties);
    }

    public @NotNull MqttStatefulPublish getPublish() {
        return publish;
    }

    public @NotNull MqttPubAck build() {
        return new MqttPubAck(publish.getPacketIdentifier(), reasonCode, reasonString, userProperties);
    }
}
