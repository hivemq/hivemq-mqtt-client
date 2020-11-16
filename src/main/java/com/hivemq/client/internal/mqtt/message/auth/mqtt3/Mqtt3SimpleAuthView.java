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

package com.hivemq.client.internal.mqtt.message.auth.mqtt3;

import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.message.auth.MqttSimpleAuth;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class Mqtt3SimpleAuthView implements Mqtt3SimpleAuth {

    private static @NotNull MqttSimpleAuth delegate(
            final @NotNull MqttUtf8StringImpl username, final @Nullable ByteBuffer password) {

        return new MqttSimpleAuth(username, password);
    }

    static @NotNull Mqtt3SimpleAuthView of(
            final @NotNull MqttUtf8StringImpl username, final @Nullable ByteBuffer password) {

        return new Mqtt3SimpleAuthView(delegate(username, password));
    }

    public static @NotNull Mqtt3SimpleAuthView of(final @NotNull MqttSimpleAuth delegate) {
        return new Mqtt3SimpleAuthView(delegate);
    }

    private final @NotNull MqttSimpleAuth delegate;

    private Mqtt3SimpleAuthView(final @NotNull MqttSimpleAuth delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull MqttUtf8String getUsername() {
        return Checks.stateNotNull(delegate.getRawUsername(), "Username");
    }

    @Override
    public @NotNull Optional<ByteBuffer> getPassword() {
        return delegate.getPassword();
    }

    public @NotNull MqttSimpleAuth getDelegate() {
        return delegate;
    }

    @Override
    public @NotNull String toString() {
        return delegate.toString();
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mqtt3SimpleAuthView)) {
            return false;
        }
        final Mqtt3SimpleAuthView that = (Mqtt3SimpleAuthView) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
