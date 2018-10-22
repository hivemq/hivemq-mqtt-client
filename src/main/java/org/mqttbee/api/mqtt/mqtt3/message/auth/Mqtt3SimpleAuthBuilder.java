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

package org.mqttbee.api.mqtt.mqtt3.message.auth;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.message.auth.mqtt3.Mqtt3SimpleAuthView;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;

import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SimpleAuthBuilder<P> extends FluentBuilder<Mqtt3SimpleAuth, P> {

    private @Nullable MqttUTF8StringImpl username;
    private @Nullable ByteBuffer password;

    public Mqtt3SimpleAuthBuilder(final @Nullable Function<? super Mqtt3SimpleAuth, P> parentConsumer) {
        super(parentConsumer);
    }

    public @NotNull Mqtt3SimpleAuthBuilder<P> username(final @NotNull String username) {
        this.username = MqttBuilderUtil.string(username);
        return this;
    }

    public @NotNull Mqtt3SimpleAuthBuilder<P> username(final @NotNull MqttUTF8String username) {
        this.username = MqttBuilderUtil.string(username);
        return this;
    }

    public @NotNull Mqtt3SimpleAuthBuilder<P> password(final @Nullable byte[] password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return this;
    }

    public @NotNull Mqtt3SimpleAuthBuilder<P> password(final @Nullable ByteBuffer password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return this;
    }

    @Override
    public @NotNull Mqtt3SimpleAuth build() {
        Preconditions.checkState(username != null);
        return Mqtt3SimpleAuthView.of(username, password);
    }

    public @NotNull P applySimpleAuth() {
        return apply();
    }

}
