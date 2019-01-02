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

package org.mqttbee.internal.mqtt.message.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.datatypes.MqttUtf8StringImpl;
import org.mqttbee.internal.util.ByteBufferUtil;
import org.mqttbee.mqtt.datatypes.MqttUtf8String;
import org.mqttbee.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttSimpleAuth implements Mqtt5SimpleAuth {

    private final @Nullable MqttUtf8StringImpl username;
    private final @Nullable ByteBuffer password;

    public MqttSimpleAuth(final @Nullable MqttUtf8StringImpl username, final @Nullable ByteBuffer password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public @NotNull Optional<MqttUtf8String> getUsername() {
        return Optional.ofNullable(username);
    }

    public @Nullable MqttUtf8StringImpl getRawUsername() {
        return username;
    }

    @Override
    public @NotNull Optional<ByteBuffer> getPassword() {
        return ByteBufferUtil.optionalReadOnly(password);
    }

    public @Nullable ByteBuffer getRawPassword() {
        return password;
    }
}
