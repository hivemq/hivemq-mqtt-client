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

package com.hivemq.client2.internal.mqtt.message.auth.mqtt3;

import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.mqtt.util.MqttChecks;
import com.hivemq.client2.internal.util.Checks;
import com.hivemq.client2.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client2.mqtt.mqtt3.message.auth.Mqtt3SimpleAuthBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class Mqtt3SimpleAuthViewBuilder<B extends Mqtt3SimpleAuthViewBuilder<B>> {

    private @Nullable MqttUtf8StringImpl username;
    private @Nullable ByteBuffer password;

    abstract @NotNull B self();

    public @NotNull B username(final @Nullable String username) {
        this.username = MqttUtf8StringImpl.of(username, "Username");
        return self();
    }

    public @NotNull B username(final @Nullable MqttUtf8String username) {
        this.username = MqttChecks.string(username, "Username");
        return self();
    }

    public @NotNull B password(final byte @Nullable [] password) {
        this.password = MqttChecks.binaryData(password, "Password");
        return self();
    }

    public @NotNull B password(final @Nullable ByteBuffer password) {
        this.password = MqttChecks.binaryData(password, "Password");
        return self();
    }

    public @NotNull Mqtt3SimpleAuthView build() {
        Checks.state(username != null, "Username must be given.");
        return Mqtt3SimpleAuthView.of(username, password);
    }

    public static class Default extends Mqtt3SimpleAuthViewBuilder<Default> implements Mqtt3SimpleAuthBuilder.Complete {

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends Mqtt3SimpleAuthViewBuilder<Nested<P>>
            implements Mqtt3SimpleAuthBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super Mqtt3SimpleAuthView, P> parentConsumer;

        public Nested(final @NotNull Function<? super Mqtt3SimpleAuthView, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applySimpleAuth() {
            return parentConsumer.apply(build());
        }
    }
}
