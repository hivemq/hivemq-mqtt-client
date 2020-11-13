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

package com.hivemq.client.internal.mqtt.message.publish;

import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.util.MqttChecks;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PubRelBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PubRelReasonCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class MqttPubRelBuilder implements Mqtt5PubRelBuilder {

    private final int packetIdentifier;
    private @NotNull Mqtt5PubRelReasonCode reasonCode = MqttPubRel.DEFAULT_REASON_CODE;
    private @Nullable MqttUtf8StringImpl reasonString;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttPubRelBuilder(final @NotNull MqttPubRec pubRec) {
        packetIdentifier = pubRec.getPacketIdentifier();
    }

    public @NotNull MqttPubRelBuilder reasonCode(final @NotNull Mqtt5PubRelReasonCode reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    @Override
    public @NotNull MqttPubRelBuilder reasonString(final @Nullable String reasonString) {
        this.reasonString = MqttChecks.reasonString(reasonString);
        return this;
    }

    @Override
    public @NotNull MqttPubRelBuilder reasonString(final @Nullable MqttUtf8String reasonString) {
        this.reasonString = MqttChecks.reasonString(reasonString);
        return this;
    }

    @Override
    public @NotNull MqttPubRelBuilder userProperties(final @Nullable Mqtt5UserProperties userProperties) {
        this.userProperties = MqttChecks.userProperties(userProperties);
        return this;
    }

    @Override
    public MqttUserPropertiesImplBuilder.@NotNull Nested<MqttPubRelBuilder> userProperties() {
        return new MqttUserPropertiesImplBuilder.Nested<>(userProperties, this::userProperties);
    }

    @Override
    public @NotNull Mqtt5PubRelReasonCode getReasonCode() {
        return reasonCode;
    }

    public @NotNull MqttPubRel build() {
        return new MqttPubRel(packetIdentifier, reasonCode, reasonString, userProperties);
    }
}
