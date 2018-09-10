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

package org.mqttbee.mqtt.message.publish.pubrec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRecBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRecReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
public class MqttPubRecBuilder implements Mqtt5PubRecBuilder {

    private final @NotNull MqttStatefulPublish publish;
    private @NotNull Mqtt5PubRecReasonCode reasonCode = MqttPubRec.DEFAULT_REASON_CODE;
    private @Nullable MqttUTF8StringImpl reasonString;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    public MqttPubRecBuilder(final @NotNull MqttStatefulPublish publish) {
        this.publish = publish;
    }

    @Override
    public @NotNull MqttPubRecBuilder reasonCode(final @NotNull Mqtt5PubRecReasonCode reasonCode) {
        this.reasonCode = Objects.requireNonNull(reasonCode, "Reason Code must not be null");
        return this;
    }

    @Override
    public @NotNull MqttPubRecBuilder reasonString(final @Nullable MqttUTF8String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return this;
    }

    @Override
    public @NotNull MqttPubRecBuilder userProperties(final @NotNull Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return this;
    }

    public @NotNull MqttStatefulPublish getPublish() {
        return publish;
    }

    public @NotNull MqttPubRec build() {
        return new MqttPubRec(publish.getPacketIdentifier(), reasonCode, reasonString, userProperties);
    }

}
