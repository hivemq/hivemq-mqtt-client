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

package com.hivemq.client.internal.mqtt.advanced.interceptor;

import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.Mqtt5ClientInterceptors;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5IncomingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5OutgoingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5IncomingQos2Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5OutgoingQos2Interceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttClientInterceptors implements Mqtt5ClientInterceptors {

    private final @Nullable Mqtt5IncomingQos1Interceptor incomingQos1Interceptor;
    private final @Nullable Mqtt5OutgoingQos1Interceptor outgoingQos1Interceptor;
    private final @Nullable Mqtt5IncomingQos2Interceptor incomingQos2Interceptor;
    private final @Nullable Mqtt5OutgoingQos2Interceptor outgoingQos2Interceptor;

    MqttClientInterceptors(
            final @Nullable Mqtt5IncomingQos1Interceptor incomingQos1Interceptor,
            final @Nullable Mqtt5OutgoingQos1Interceptor outgoingQos1Interceptor,
            final @Nullable Mqtt5IncomingQos2Interceptor incomingQos2Interceptor,
            final @Nullable Mqtt5OutgoingQos2Interceptor outgoingQos2Interceptor) {

        this.incomingQos1Interceptor = incomingQos1Interceptor;
        this.outgoingQos1Interceptor = outgoingQos1Interceptor;
        this.incomingQos2Interceptor = incomingQos2Interceptor;
        this.outgoingQos2Interceptor = outgoingQos2Interceptor;
    }

    @Override
    public @Nullable Mqtt5IncomingQos1Interceptor getIncomingQos1Interceptor() {
        return incomingQos1Interceptor;
    }

    @Override
    public @Nullable Mqtt5OutgoingQos1Interceptor getOutgoingQos1Interceptor() {
        return outgoingQos1Interceptor;
    }

    @Override
    public @Nullable Mqtt5IncomingQos2Interceptor getIncomingQos2Interceptor() {
        return incomingQos2Interceptor;
    }

    @Override
    public @Nullable Mqtt5OutgoingQos2Interceptor getOutgoingQos2Interceptor() {
        return outgoingQos2Interceptor;
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

        return Objects.equals(incomingQos1Interceptor, that.incomingQos1Interceptor) &&
                Objects.equals(outgoingQos1Interceptor, that.outgoingQos1Interceptor) &&
                Objects.equals(incomingQos2Interceptor, that.incomingQos2Interceptor) &&
                Objects.equals(outgoingQos2Interceptor, that.outgoingQos2Interceptor);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(incomingQos1Interceptor);
        result = 31 * result + Objects.hashCode(outgoingQos1Interceptor);
        result = 31 * result + Objects.hashCode(incomingQos2Interceptor);
        result = 31 * result + Objects.hashCode(outgoingQos2Interceptor);
        return result;
    }
}
