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

package com.hivemq.mqtt.client2.internal.codec.encoder.mqtt3;

import com.hivemq.mqtt.client2.internal.codec.encoder.mqtt3.Mqtt3MessageEncoder.Mqtt3MessageWithIdEncoder;
import com.hivemq.mqtt.client2.internal.message.publish.MqttPubComp;
import com.hivemq.mqtt.client2.mqtt3.message.Mqtt3MessageType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
public class Mqtt3PubCompEncoder extends Mqtt3MessageWithIdEncoder<MqttPubComp> {

    public static final @NotNull Mqtt3PubCompEncoder INSTANCE = new Mqtt3PubCompEncoder();

    private static final int FIXED_HEADER = Mqtt3MessageType.PUBCOMP.getCode() << 4;

    private Mqtt3PubCompEncoder() {}

    @Override
    int getFixedHeader() {
        return FIXED_HEADER;
    }
}
