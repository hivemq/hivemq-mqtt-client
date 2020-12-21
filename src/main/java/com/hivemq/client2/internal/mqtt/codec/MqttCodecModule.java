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

package com.hivemq.client2.internal.mqtt.codec;

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.codec.decoder.MqttMessageDecoders;
import com.hivemq.client2.internal.mqtt.codec.decoder.mqtt3.Mqtt3ClientMessageDecoders;
import com.hivemq.client2.internal.mqtt.codec.decoder.mqtt5.Mqtt5ClientMessageDecoders;
import com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client2.internal.mqtt.codec.encoder.mqtt3.Mqtt3ClientMessageEncoders;
import com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5.Mqtt5ClientMessageEncoders;
import com.hivemq.client2.internal.mqtt.ioc.ConnectionScope;
import dagger.Lazy;
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
    static @NotNull MqttMessageDecoders provideMessageDecoders(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull Lazy<Mqtt5ClientMessageDecoders> mqtt5ClientMessageDecoders,
            final @NotNull Lazy<Mqtt3ClientMessageDecoders> mqtt3ClientMessageDecoders) {

        switch (clientConfig.getMqttVersion()) {
            case MQTT_5_0:
                return mqtt5ClientMessageDecoders.get();
            case MQTT_3_1_1:
                return mqtt3ClientMessageDecoders.get();
            default:
                throw new IllegalStateException();
        }
    }

    @Provides
    @ConnectionScope
    static @NotNull MqttMessageEncoders provideMessageEncoders(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull Lazy<Mqtt5ClientMessageEncoders> mqtt5ClientMessageEncoders,
            final @NotNull Lazy<Mqtt3ClientMessageEncoders> mqtt3ClientMessageEncoders) {

        switch (clientConfig.getMqttVersion()) {
            case MQTT_5_0:
                return mqtt5ClientMessageEncoders.get();
            case MQTT_3_1_1:
                return mqtt3ClientMessageEncoders.get();
            default:
                throw new IllegalStateException();
        }
    }
}
