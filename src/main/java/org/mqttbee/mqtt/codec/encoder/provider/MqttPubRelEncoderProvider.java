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

package org.mqttbee.mqtt.codec.encoder.provider;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider.ThreadLocalMqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;

import java.util.function.Supplier;

/**
 * @author Silvio Giebl
 */
public class MqttPubRelEncoderProvider extends ThreadLocalMqttMessageEncoderProvider<MqttPubRel> {

    private final MqttMessageEncoderProvider<MqttPubComp> pubCompProvider;

    public MqttPubRelEncoderProvider(
            @NotNull final Supplier<MqttMessageEncoderApplier<MqttPubRel>> supplier,
            @NotNull final MqttMessageEncoderProvider<MqttPubComp> pubCompProvider) {

        super(supplier);
        this.pubCompProvider = pubCompProvider;
    }

    @NotNull
    public MqttMessageEncoderProvider<MqttPubComp> getPubCompEncoderProvider() {
        return pubCompProvider;
    }

}
