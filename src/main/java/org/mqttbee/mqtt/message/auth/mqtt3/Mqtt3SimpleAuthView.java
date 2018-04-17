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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
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
    public static MqttSimpleAuth wrapped(
            @NotNull final MqttUTF8StringImpl username, @Nullable final ByteBuffer password) {

        return new MqttSimpleAuth(username, password);
    }

    @NotNull
    public static Mqtt3SimpleAuthView create(
            @NotNull final MqttUTF8StringImpl username, @Nullable final ByteBuffer password) {

        return new Mqtt3SimpleAuthView(wrapped(username, password));
    }

    private final MqttSimpleAuth wrapped;

    public Mqtt3SimpleAuthView(@NotNull final MqttSimpleAuth wrapped) {
        this.wrapped = wrapped;
    }

    @NotNull
    @Override
    public MqttUTF8String getUsername() {
        final MqttUTF8StringImpl username = wrapped.getRawUsername();
        if (username == null) {
            throw new IllegalStateException();
        }
        return username;
    }

    @NotNull
    @Override
    public Optional<ByteBuffer> getPassword() {
        return wrapped.getPassword();
    }

    @NotNull
    public MqttSimpleAuth getWrapped() {
        return wrapped;
    }

}
