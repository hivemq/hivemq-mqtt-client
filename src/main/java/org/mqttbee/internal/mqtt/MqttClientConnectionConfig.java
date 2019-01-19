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

package org.mqttbee.internal.mqtt;

import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.mqtt3.Mqtt3ClientConnectionConfig;
import org.mqttbee.mqtt.mqtt5.Mqtt5ClientConnectionConfig;
import org.mqttbee.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class MqttClientConnectionConfig implements Mqtt5ClientConnectionConfig, Mqtt3ClientConnectionConfig {

    private volatile int keepAlive;
    private volatile long sessionExpiryInterval;
    private final int receiveMaximum;
    private final int maximumPacketSize;
    private final int topicAliasMaximum;
    private final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism;
    private final boolean hasWillPublish;
    private final boolean problemInformationRequested;
    private final boolean responseInformationRequested;
    private final @NotNull Channel channel;

    public MqttClientConnectionConfig(
            final int keepAlive, final long sessionExpiryInterval, final int receiveMaximum,
            final int maximumPacketSize, final int topicAliasMaximum,
            final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism, final boolean hasWillPublish,
            final boolean problemInformationRequested, final boolean responseInformationRequested,
            final @NotNull Channel channel) {

        this.keepAlive = keepAlive;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.receiveMaximum = receiveMaximum;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMaximum = topicAliasMaximum;
        this.enhancedAuthMechanism = enhancedAuthMechanism;
        this.hasWillPublish = hasWillPublish;
        this.problemInformationRequested = problemInformationRequested;
        this.responseInformationRequested = responseInformationRequested;
        this.channel = channel;
    }

    @Override
    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(final int keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    public void setSessionExpiryInterval(final long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    @Override
    public int getReceiveMaximum() {
        return receiveMaximum;
    }

    @Override
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @Override
    public int getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    @Override
    public @NotNull Optional<Mqtt5EnhancedAuthMechanism> getEnhancedAuthMechanism() {
        return Optional.ofNullable(enhancedAuthMechanism);
    }

    public @Nullable Mqtt5EnhancedAuthMechanism getRawEnhancedAuthMechanism() {
        return enhancedAuthMechanism;
    }

    @Override
    public boolean hasWillPublish() {
        return hasWillPublish;
    }

    @Override
    public boolean isProblemInformationRequested() {
        return problemInformationRequested;
    }

    @Override
    public boolean isResponseInformationRequested() {
        return responseInformationRequested;
    }

    public @NotNull Channel getChannel() {
        return channel;
    }
}
