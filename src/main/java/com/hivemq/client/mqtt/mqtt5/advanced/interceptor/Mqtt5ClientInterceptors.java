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

package com.hivemq.client.mqtt.mqtt5.advanced.interceptor;

import com.hivemq.client.annotations.DoNotImplement;
import com.hivemq.client.internal.mqtt.advanced.interceptor.MqttClientInterceptorsBuilder;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5IncomingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5OutgoingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5IncomingQos2Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5OutgoingQos2Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Collection of interceptors of MQTT messages received or sent by an {@link com.hivemq.client.mqtt.mqtt5.Mqtt5Client
 * MQTT5Client}.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@DoNotImplement
public interface Mqtt5ClientInterceptors {

    /**
     * Creates a builder for a collection of interceptors.
     *
     * @return the created builder for a collection of interceptors.
     */
    static @NotNull Mqtt5ClientInterceptorsBuilder builder() {
        return new MqttClientInterceptorsBuilder.Default();
    }

    @Nullable Mqtt5IncomingQos1Interceptor getIncomingQos1Interceptor();

    @Nullable Mqtt5OutgoingQos1Interceptor getOutgoingQos1Interceptor();

    @Nullable Mqtt5IncomingQos2Interceptor getIncomingQos2Interceptor();

    @Nullable Mqtt5OutgoingQos2Interceptor getOutgoingQos2Interceptor();

    /**
     * Creates a builder for extending this collection of interceptors.
     *
     * @return the created builder.
     * @since 1.1
     */
    @NotNull Mqtt5ClientInterceptorsBuilder extend();
}
