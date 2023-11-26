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

import com.hivemq.mqtt.client2.internal.lifecycle.MqttConnectedContextImpl;
import com.hivemq.mqtt.client2.internal.message.connect.mqtt3.Mqtt3ConnAckView;
import com.hivemq.mqtt.client2.internal.message.connect.mqtt3.Mqtt3ConnectView;
import com.hivemq.mqtt.client2.internal.mqtt3.Mqtt3ClientConfigView;
import com.hivemq.mqtt.client2.mqtt3.lifecycle.Mqtt3ConnectedContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class Mqtt3ConnectedContextView implements Mqtt3ConnectedContext {

    private final @NotNull MqttConnectedContextImpl delegate;

    public Mqtt3ConnectedContextView(final @NotNull MqttConnectedContextImpl delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Mqtt3ClientConfigView getClientConfig() {
        return new Mqtt3ClientConfigView(delegate.getClientConfig());
    }

    @Override
    public @NotNull Mqtt3ConnectView getConnect() {
        return Mqtt3ConnectView.of(delegate.getConnect());
    }

    @Override
    public @NotNull Mqtt3ConnAckView getConnAck() {
        return Mqtt3ConnAckView.of(delegate.getConnAck());
    }
}
