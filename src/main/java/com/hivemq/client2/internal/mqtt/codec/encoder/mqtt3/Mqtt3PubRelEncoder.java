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

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt3;

import com.hivemq.client2.internal.mqtt.codec.encoder.mqtt3.Mqtt3MessageEncoder.Mqtt3MessageWithIdEncoder;
import com.hivemq.client2.internal.mqtt.message.publish.MqttPubRel;
import com.hivemq.client2.mqtt.mqtt3.message.Mqtt3MessageType;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3PubRelEncoder extends Mqtt3MessageWithIdEncoder<MqttPubRel> {

    private static final int FIXED_HEADER = (Mqtt3MessageType.PUBREL.getCode() << 4) | 0b0010;

    @Inject
    Mqtt3PubRelEncoder() {}

    @Override
    int getFixedHeader() {
        return FIXED_HEADER;
    }
}
