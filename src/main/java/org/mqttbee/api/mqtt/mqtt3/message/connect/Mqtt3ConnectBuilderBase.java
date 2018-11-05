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

package org.mqttbee.api.mqtt.mqtt3.message.connect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuth;
import org.mqttbee.api.mqtt.mqtt3.message.auth.Mqtt3SimpleAuthBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3WillPublishBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt3ConnectBuilderBase<B extends Mqtt3ConnectBuilderBase<B>> {

    @NotNull B keepAlive(final int keepAlive, final @NotNull TimeUnit timeUnit);

    @NotNull B cleanSession(final boolean isCleanSession);

    @NotNull B simpleAuth(final @Nullable Mqtt3SimpleAuth simpleAuth);

    @NotNull Mqtt3SimpleAuthBuilder.Nested<? extends B> simpleAuth();

    @NotNull B willPublish(final @Nullable Mqtt3Publish willPublish);

    @NotNull Mqtt3WillPublishBuilder.Nested<? extends B> willPublish();
}
