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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.message.auth.mqtt3.Mqtt3SimpleAuthView;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

import java.nio.ByteBuffer;

/**
 * @author Silvio Giebl
 */
public class Mqtt3SimpleAuthBuilder {

    private MqttUTF8StringImpl username;
    private ByteBuffer password;

    Mqtt3SimpleAuthBuilder() {
    }

    @NotNull
    public Mqtt3SimpleAuthBuilder withUsername(@NotNull final String username) {
        this.username = MqttBuilderUtil.string(username);
        return this;
    }

    @NotNull
    public Mqtt3SimpleAuthBuilder withUsername(@NotNull final MqttUTF8String username) {
        this.username = MqttBuilderUtil.string(username);
        return this;
    }

    @NotNull
    public Mqtt3SimpleAuthBuilder withPassword(@Nullable final byte[] password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return this;
    }

    @NotNull
    public Mqtt3SimpleAuthBuilder withPassword(@Nullable final ByteBuffer password) {
        this.password = MqttBuilderUtil.binaryDataOrNull(password);
        return this;
    }

    @NotNull
    public Mqtt3SimpleAuth build() {
        Preconditions.checkState(username != null);
        return Mqtt3SimpleAuthView.create(username, password);
    }

}
