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

package com.hivemq.client.internal.mqtt.codec.encoder.mqtt5;

import com.hivemq.client.internal.mqtt.message.publish.pubcomp.MqttPubComp;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.hivemq.client.internal.mqtt.message.publish.pubcomp.MqttPubComp.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubCompEncoder extends
        Mqtt5MessageWithUserPropertiesEncoder.WithReason.WithOmissibleCode.WithId<MqttPubComp, Mqtt5PubCompReasonCode> {

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBCOMP.getCode() << 4;

    @Inject
    Mqtt5PubCompEncoder() {}

    @Override
    int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    @NotNull Mqtt5PubCompReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }
}
