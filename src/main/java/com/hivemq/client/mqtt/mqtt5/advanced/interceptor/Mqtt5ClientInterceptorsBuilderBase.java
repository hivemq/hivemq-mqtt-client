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

import com.hivemq.client.annotations.CheckReturnValue;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5IncomingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5OutgoingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5IncomingQos2Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5OutgoingQos2Interceptor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builder base for a {@link Mqtt5ClientInterceptors}.
 *
 * @param <B> the type of the builder.
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5ClientInterceptorsBuilderBase<B extends Mqtt5ClientInterceptorsBuilderBase<B>> {

    @CheckReturnValue
    @NotNull B incomingQos1Interceptor(@Nullable Mqtt5IncomingQos1Interceptor incomingQos1Interceptor);

    @CheckReturnValue
    @NotNull B outgoingQos1Interceptor(@Nullable Mqtt5OutgoingQos1Interceptor outgoingQos1Interceptor);

    @CheckReturnValue
    @NotNull B incomingQos2Interceptor(@Nullable Mqtt5IncomingQos2Interceptor incomingQos2Interceptor);

    @CheckReturnValue
    @NotNull B outgoingQos2Interceptor(@Nullable Mqtt5OutgoingQos2Interceptor outgoingQos2Interceptor);
}
