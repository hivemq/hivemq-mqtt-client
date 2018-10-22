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

package org.mqttbee.api.mqtt.mqtt5.message.auth;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.message.auth.MqttSimpleAuth;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5SimpleAuthBuilder<P> extends FluentBuilder<Mqtt5SimpleAuth, P> {

    private @Nullable MqttUTF8StringImpl username;
    private @Nullable ByteBuffer password;

    public Mqtt5SimpleAuthBuilder(final @Nullable Function<? super Mqtt5SimpleAuth, P> parentConsumer) {
        super(parentConsumer);
    }

    public @NotNull Mqtt5SimpleAuthBuilder<P> username(final @Nullable String username) {
        this.username = MqttBuilderUtil.stringOrNull(username);
        return this;
    }

    public @NotNull Mqtt5SimpleAuthBuilder<P> username(final @Nullable MqttUTF8String username) {
        this.username = MqttBuilderUtil.stringOrNull(username);
        return this;
    }

    public @NotNull Mqtt5SimpleAuthBuilder<P> password(final @Nullable byte[] password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return this;
    }

    public @NotNull Mqtt5SimpleAuthBuilder<P> password(final @Nullable ByteBuffer password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return this;
    }

    @Override
    public @NotNull Mqtt5SimpleAuth build() {
        Preconditions.checkState(username != null || password != null);
        return new MqttSimpleAuth(username, password);
    }

    public @NotNull P applySimpleAuth() {
        return apply();
    }

}
