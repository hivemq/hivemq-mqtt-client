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

package com.hivemq.client2.internal.mqtt.advanced.interceptor;

import com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.Mqtt5ClientInterceptors;
import com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5InboundQos1Interceptor;
import com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5OutboundQos1Interceptor;
import com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5InboundQos2Interceptor;
import com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5OutboundQos2Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttClientInterceptors implements Mqtt5ClientInterceptors {

    private final @Nullable Mqtt5InboundQos1Interceptor inboundQos1Interceptor;
    private final @Nullable Mqtt5OutboundQos1Interceptor outboundQos1Interceptor;
    private final @Nullable Mqtt5InboundQos2Interceptor inboundQos2Interceptor;
    private final @Nullable Mqtt5OutboundQos2Interceptor outboundQos2Interceptor;

    MqttClientInterceptors(
            final @Nullable Mqtt5InboundQos1Interceptor inboundQos1Interceptor,
            final @Nullable Mqtt5OutboundQos1Interceptor outboundQos1Interceptor,
            final @Nullable Mqtt5InboundQos2Interceptor inboundQos2Interceptor,
            final @Nullable Mqtt5OutboundQos2Interceptor outboundQos2Interceptor) {

        this.inboundQos1Interceptor = inboundQos1Interceptor;
        this.outboundQos1Interceptor = outboundQos1Interceptor;
        this.inboundQos2Interceptor = inboundQos2Interceptor;
        this.outboundQos2Interceptor = outboundQos2Interceptor;
    }

    @Override
    public @Nullable Mqtt5InboundQos1Interceptor getInboundQos1Interceptor() {
        return inboundQos1Interceptor;
    }

    @Override
    public @Nullable Mqtt5OutboundQos1Interceptor getOutboundQos1Interceptor() {
        return outboundQos1Interceptor;
    }

    @Override
    public @Nullable Mqtt5InboundQos2Interceptor getInboundQos2Interceptor() {
        return inboundQos2Interceptor;
    }

    @Override
    public @Nullable Mqtt5OutboundQos2Interceptor getOutboundQos2Interceptor() {
        return outboundQos2Interceptor;
    }

    @Override
    public MqttClientInterceptorsBuilder.@NotNull Default extend() {
        return new MqttClientInterceptorsBuilder.Default(this);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MqttClientInterceptors)) {
            return false;
        }
        final MqttClientInterceptors that = (MqttClientInterceptors) o;

        return Objects.equals(inboundQos1Interceptor, that.inboundQos1Interceptor) &&
                Objects.equals(outboundQos1Interceptor, that.outboundQos1Interceptor) &&
                Objects.equals(inboundQos2Interceptor, that.inboundQos2Interceptor) &&
                Objects.equals(outboundQos2Interceptor, that.outboundQos2Interceptor);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(inboundQos1Interceptor);
        result = 31 * result + Objects.hashCode(outboundQos1Interceptor);
        result = 31 * result + Objects.hashCode(inboundQos2Interceptor);
        result = 31 * result + Objects.hashCode(outboundQos2Interceptor);
        return result;
    }
}
