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

package org.mqttbee.mqtt.message.auth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5SimpleAuthBuilder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttSimpleAuthBuilder<B extends MqttSimpleAuthBuilder<B>> {

    private @Nullable MqttUTF8StringImpl username;
    private @Nullable ByteBuffer password;

    MqttSimpleAuthBuilder() {}

    abstract @NotNull B self();

    public @NotNull B username(final @Nullable String username) {
        this.username = MqttBuilderUtil.stringOrNull(username);
        return self();
    }

    public @NotNull B username(final @Nullable MqttUTF8String username) {
        this.username = MqttBuilderUtil.stringOrNull(username);
        return self();
    }

    public @NotNull B password(final @Nullable byte[] password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return self();
    }

    public @NotNull B password(final @Nullable ByteBuffer password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return self();
    }

    public @NotNull MqttSimpleAuth build() {
        if (username == null && password == null) {
            throw new IllegalStateException("Either user name or password must be given.");
        }
        return new MqttSimpleAuth(username, password);
    }

    public static class Default extends MqttSimpleAuthBuilder<Default> implements Mqtt5SimpleAuthBuilder.Complete {

        public Default() {}

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttSimpleAuthBuilder<Nested<P>>
            implements Mqtt5SimpleAuthBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super MqttSimpleAuth, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttSimpleAuth, P> parentConsumer) {
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
