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

package com.hivemq.mqtt.client2.internal.codec;

import com.hivemq.mqtt.client2.internal.MqttClientConfig;
import com.hivemq.mqtt.client2.internal.codec.decoder.MqttMessageDecoders;
import com.hivemq.mqtt.client2.internal.codec.decoder.mqtt3.Mqtt3ClientMessageDecoders;
import com.hivemq.mqtt.client2.internal.codec.decoder.mqtt5.Mqtt5ClientMessageDecoders;
import com.hivemq.mqtt.client2.internal.codec.encoder.MqttMessageEncoders;
import com.hivemq.mqtt.client2.internal.codec.encoder.mqtt3.Mqtt3ClientMessageEncoders;
import com.hivemq.mqtt.client2.internal.codec.encoder.mqtt5.Mqtt5ClientMessageEncoders;
import com.hivemq.mqtt.client2.internal.ioc.ConnectionScope;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
@Module
public abstract class MqttCodecModule {

    @Provides
    @ConnectionScope
    static @NotNull MqttMessageDecoders provideMessageDecoders(final @NotNull MqttClientConfig clientConfig) {
        switch (clientConfig.getMqttVersion()) {
            case MQTT_5_0:
                return Mqtt5ClientMessageDecoders.INSTANCE;
            case MQTT_3_1_1:
                return Mqtt3ClientMessageDecoders.INSTANCE;
            default:
                throw new IllegalStateException();
        }
    }

    @Provides
    @ConnectionScope
    static @NotNull MqttMessageEncoders provideMessageEncoders(final @NotNull MqttClientConfig clientConfig) {
        switch (clientConfig.getMqttVersion()) {
            case MQTT_5_0:
                return Mqtt5ClientMessageEncoders.INSTANCE;
            case MQTT_3_1_1:
                return Mqtt3ClientMessageEncoders.INSTANCE;
            default:
                throw new IllegalStateException();
        }
    }
}
