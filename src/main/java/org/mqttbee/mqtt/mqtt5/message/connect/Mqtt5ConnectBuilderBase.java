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

package org.mqttbee.mqtt.mqtt5.message.connect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.mqtt5.auth.Mqtt5EnhancedAuthProvider;
import org.mqttbee.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth;
import org.mqttbee.mqtt.mqtt5.message.auth.Mqtt5SimpleAuthBuilder;
import org.mqttbee.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt.mqtt5.message.publish.Mqtt5WillPublishBuilder;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5ConnectBuilderBase<B extends Mqtt5ConnectBuilderBase<B>> {

    @NotNull B keepAlive(int keepAlive);

    @NotNull B noKeepAlive();

    @NotNull B cleanStart(boolean isCleanStart);

    @NotNull B sessionExpiryInterval(long sessionExpiryInterval);

    @NotNull B noSessionExpiry();

    @NotNull B responseInformationRequested(boolean isResponseInformationRequested);

    @NotNull B problemInformationRequested(boolean isProblemInformationRequested);

    @NotNull B restrictions(@NotNull Mqtt5ConnectRestrictions restrictions);

    @NotNull Mqtt5ConnectRestrictionsBuilder.Nested<? extends B> restrictions();

    @NotNull B simpleAuth(@Nullable Mqtt5SimpleAuth simpleAuth);

    @NotNull Mqtt5SimpleAuthBuilder.Nested<? extends B> simpleAuth();

    @NotNull B enhancedAuth(@Nullable Mqtt5EnhancedAuthProvider enhancedAuthProvider);

    @NotNull B willPublish(@Nullable Mqtt5Publish willPublish);

    @NotNull B willPublish(@Nullable Mqtt5WillPublish willPublish);

    @NotNull Mqtt5WillPublishBuilder.Nested<? extends B> willPublish();

    @NotNull B userProperties(@NotNull Mqtt5UserProperties userProperties);

    @NotNull Mqtt5UserPropertiesBuilder.Nested<? extends B> userProperties();
}
