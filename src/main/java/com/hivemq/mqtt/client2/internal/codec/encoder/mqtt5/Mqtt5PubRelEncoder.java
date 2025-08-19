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

package com.hivemq.mqtt.client2.internal.codec.encoder.mqtt5;

import com.hivemq.mqtt.client2.internal.message.publish.MqttPubRel;
import com.hivemq.mqtt.client2.mqtt5.message.Mqtt5MessageType;
import com.hivemq.mqtt.client2.mqtt5.message.publish.Mqtt5PubRelReasonCode;
import org.jetbrains.annotations.NotNull;

import static com.hivemq.mqtt.client2.internal.message.publish.MqttPubRel.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRelEncoder extends
        Mqtt5MessageWithUserPropertiesEncoder.WithReason.WithOmissibleCode.WithId<MqttPubRel, Mqtt5PubRelReasonCode> {

    public static final @NotNull Mqtt5PubRelEncoder INSTANCE = new Mqtt5PubRelEncoder();

    private static final int FIXED_HEADER = (Mqtt5MessageType.PUBREL.getCode() << 4) | 0b0010;

    private Mqtt5PubRelEncoder() {}

    @Override
    int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    @NotNull Mqtt5PubRelReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }
}
