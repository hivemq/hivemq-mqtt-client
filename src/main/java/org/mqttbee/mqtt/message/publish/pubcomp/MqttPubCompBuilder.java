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

package org.mqttbee.mqtt.message.publish.pubcomp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImplBuilder;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * @author Silvio Giebl
 */
public class MqttPubCompBuilder implements Mqtt5PubCompBuilder {

    private final @NotNull MqttPubRel pubRel;
    private @NotNull Mqtt5PubCompReasonCode reasonCode = MqttPubComp.DEFAULT_REASON_CODE;
    private @Nullable MqttUTF8StringImpl reasonString;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttPubCompBuilder(final @NotNull MqttPubRel pubRel) {
        this.pubRel = pubRel;
    }

    public @NotNull MqttPubCompBuilder reasonCode(final @NotNull Mqtt5PubCompReasonCode reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    @Override
    public @NotNull MqttPubCompBuilder reasonString(final @Nullable MqttUTF8String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @Override
    public @NotNull MqttPubCompBuilder userProperties(final @NotNull Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    @Override
    public @NotNull MqttUserPropertiesImplBuilder.Nested<MqttPubCompBuilder> userProperties() {
        return new MqttUserPropertiesImplBuilder.Nested<>(this::userProperties);
    }

    @Override
    public @NotNull Mqtt5PubCompReasonCode getReasonCode() {
        return reasonCode;
    }

    public @NotNull MqttPubRel getPubRel() {
        return pubRel;
    }

    public @NotNull MqttPubComp build() {
        return new MqttPubComp(pubRel.getPacketIdentifier(), reasonCode, reasonString, userProperties);
    }
}
