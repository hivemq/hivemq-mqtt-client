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

package com.hivemq.mqtt.client2.internal.lifecycle.mqtt3;

import com.hivemq.mqtt.client2.internal.exceptions.mqtt3.Mqtt3ExceptionFactory;
import com.hivemq.mqtt.client2.internal.lifecycle.MqttDisconnectedContextImpl;
import com.hivemq.mqtt.client2.internal.mqtt3.Mqtt3ClientConfigView;
import com.hivemq.mqtt.client2.lifecycle.MqttDisconnectSource;
import com.hivemq.mqtt.client2.mqtt3.lifecycle.Mqtt3DisconnectedContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt3DisconnectedContextView implements Mqtt3DisconnectedContext {

    private final @NotNull MqttDisconnectedContextImpl delegate;

    public Mqtt3DisconnectedContextView(final @NotNull MqttDisconnectedContextImpl delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Mqtt3ClientConfigView getClientConfig() {
        return new Mqtt3ClientConfigView(delegate.getClientConfig());
    }

    @Override
    public @NotNull MqttDisconnectSource getSource() {
        return delegate.getSource();
    }

    @Override
    public @NotNull Throwable getCause() {
        return Mqtt3ExceptionFactory.map(delegate.getCause());
    }

    @Override
    public @NotNull Mqtt3ReconnectorView getReconnector() {
        return new Mqtt3ReconnectorView(delegate.getReconnector());
    }
}
