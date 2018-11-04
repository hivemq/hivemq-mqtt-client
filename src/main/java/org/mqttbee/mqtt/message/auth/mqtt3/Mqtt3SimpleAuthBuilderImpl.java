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
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuthBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuthBuilderBase;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
// @formatter:off
public abstract class Mqtt3SimpleAuthBuilderImpl<B extends Mqtt3SimpleAuthBuilderBase<B, C>, C extends B>
        implements Mqtt3SimpleAuthBuilderBase<B, C>,
                   Mqtt3SimpleAuthBuilderBase.Complete<B, C> {
// @formatter:on

    private @Nullable MqttUTF8StringImpl username;
    private @Nullable ByteBuffer password;

    abstract @NotNull C self();

    @Override
    public @NotNull C username(final @NotNull String username) {
        this.username = MqttBuilderUtil.string(username);
        return self();
    }

    @Override
    public @NotNull C username(final @NotNull MqttUTF8String username) {
        this.username = MqttBuilderUtil.string(username);
        return self();
    }

    @Override
    public @NotNull C password(final @Nullable byte[] password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return self();
    }

    @Override
    public @NotNull C password(final @Nullable ByteBuffer password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return self();
    }

    public @NotNull Mqtt3SimpleAuth build() {
        if (username == null) {
            throw new IllegalStateException("Username must be given.");
        }
        return Mqtt3SimpleAuthView.of(username, password);
    }

    // @formatter:off
    public static class Impl
            extends Mqtt3SimpleAuthBuilderImpl<Mqtt3SimpleAuthBuilder, Mqtt3SimpleAuthBuilder.Complete>
            implements Mqtt3SimpleAuthBuilder, Mqtt3SimpleAuthBuilder.Complete {
    // @formatter:on

        @Override
        @NotNull Mqtt3SimpleAuthBuilder.Complete self() {
            return this;
        }
    }

    // @formatter:off
    public static class NestedImpl<P>
            extends Mqtt3SimpleAuthBuilderImpl<
                        Mqtt3SimpleAuthBuilder.Nested<P>,
                        Mqtt3SimpleAuthBuilder.Nested.Complete<P>>
            implements Mqtt3SimpleAuthBuilder.Nested<P>,
                       Mqtt3SimpleAuthBuilder.Nested.Complete<P> {
    // @formatter:on

        private final @NotNull Function<? super Mqtt3SimpleAuth, P> parentConsumer;

        public NestedImpl(final @NotNull Function<? super Mqtt3SimpleAuth, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Mqtt3SimpleAuthBuilder.Nested.Complete<P> self() {
            return this;
        }

        @Override
        public @NotNull P applySimpleAuth() {
            return parentConsumer.apply(build());
        }
    }
}
