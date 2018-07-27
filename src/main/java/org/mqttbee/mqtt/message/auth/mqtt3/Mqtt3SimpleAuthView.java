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

package org.mqttbee.mqtt.message.auth.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;

import javax.annotation.concurrent.Immutable;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3SimpleAuthView implements Mqtt3SimpleAuth {

    @NotNull
    private static MqttSimpleAuth delegate(
            @NotNull final MqttUtf8StringImpl username, @Nullable final ByteBuffer password) {

        return new MqttSimpleAuth(username, password);
    }

    @NotNull
    public static Mqtt3SimpleAuthView of(
            @NotNull final MqttUtf8StringImpl username, @Nullable final ByteBuffer password) {

        return new Mqtt3SimpleAuthView(delegate(username, password));
    }

    @NotNull
    public static Mqtt3SimpleAuthView of(@NotNull final MqttSimpleAuth delegate) {
        return new Mqtt3SimpleAuthView(delegate);
    }

    private final MqttSimpleAuth delegate;

    private Mqtt3SimpleAuthView(@NotNull final MqttSimpleAuth delegate) {
        this.delegate = delegate;
    }

    @NotNull
    @Override
    public MqttUtf8String getUsername() {
        final MqttUtf8StringImpl username = delegate.getRawUsername();
        if (username == null) {
            throw new IllegalStateException();
        }
        return username;
    }

    @NotNull
    @Override
    public Optional<ByteBuffer> getPassword() {
        return delegate.getPassword();
    }

    @NotNull
    public MqttSimpleAuth getDelegate() {
        return delegate;
    }

}
