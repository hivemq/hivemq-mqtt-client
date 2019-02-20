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

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttTopicAliasAutoMapping;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttTopicAliasMapping;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientConnectionConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConnectionConfig;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import io.netty.channel.Channel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class MqttClientConnectionConfig
        implements Mqtt5ClientConnectionConfig, Mqtt5ClientConnectionConfig.RestrictionsForServer,
        Mqtt5ClientConnectionConfig.RestrictionsForClient, Mqtt3ClientConnectionConfig,
        Mqtt3ClientConnectionConfig.RestrictionsForClient {

    private final int keepAlive;
    private final long sessionExpiryInterval;
    private final boolean hasWillPublish;
    private final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism;
    private final int receiveMaximum;
    private final int maximumPacketSize;
    private final int topicAliasMaximum;
    private final boolean problemInformationRequested;
    private final boolean responseInformationRequested;
    private final int sendMaximum;
    private final int sendMaximumPacketSize;
    private final @Nullable MqttTopicAliasMapping sendTopicAliasMapping;
    private final @NotNull MqttQos maximumQos;
    private final boolean retainAvailable;
    private final boolean wildcardSubscriptionAvailable;
    private final boolean sharedSubscriptionAvailable;
    private final boolean subscriptionIdentifiersAvailable;
    private final @NotNull Channel channel;

    public MqttClientConnectionConfig(
            final int keepAlive, final long sessionExpiryInterval, final boolean hasWillPublish,
            final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism, final int receiveMaximum,
            final int maximumPacketSize, final int topicAliasMaximum, final boolean problemInformationRequested,
            final boolean responseInformationRequested, final int sendMaximum, final int sendMaximumPacketSize,
            final int sendTopicAliasMaximum, final @NotNull MqttQos maximumQos, final boolean retainAvailable,
            final boolean wildcardSubscriptionAvailable, final boolean sharedSubscriptionAvailable,
            final boolean subscriptionIdentifiersAvailable, final @NotNull Channel channel) {

        this.keepAlive = keepAlive;
        this.sessionExpiryInterval = sessionExpiryInterval;
        this.hasWillPublish = hasWillPublish;
        this.enhancedAuthMechanism = enhancedAuthMechanism;
        this.receiveMaximum = receiveMaximum;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMaximum = topicAliasMaximum;
        this.problemInformationRequested = problemInformationRequested;
        this.responseInformationRequested = responseInformationRequested;
        this.sendMaximum = sendMaximum;
        this.sendMaximumPacketSize = sendMaximumPacketSize;
        this.sendTopicAliasMapping =
                (sendTopicAliasMaximum == 0) ? null : new MqttTopicAliasAutoMapping(sendTopicAliasMaximum);
        this.maximumQos = maximumQos;
        this.retainAvailable = retainAvailable;
        this.wildcardSubscriptionAvailable = wildcardSubscriptionAvailable;
        this.sharedSubscriptionAvailable = sharedSubscriptionAvailable;
        this.subscriptionIdentifiersAvailable = subscriptionIdentifiersAvailable;
        this.channel = channel;
    }

    @Override
    public int getKeepAlive() {
        return keepAlive;
    }

    @Override
    public long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    @Override
    public boolean hasWillPublish() {
        return hasWillPublish;
    }

    @Override
    public @NotNull Optional<Mqtt5EnhancedAuthMechanism> getEnhancedAuthMechanism() {
        return Optional.ofNullable(enhancedAuthMechanism);
    }

    public @Nullable Mqtt5EnhancedAuthMechanism getRawEnhancedAuthMechanism() {
        return enhancedAuthMechanism;
    }

    @Override
    public @NotNull MqttClientConnectionConfig getRestrictionsForServer() {
        return this;
    }

    @Override
    public @NotNull MqttClientConnectionConfig getRestrictionsForClient() {
        return this;
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
    public boolean isProblemInformationRequested() {
        return problemInformationRequested;
    }

    @Override
    public boolean isResponseInformationRequested() {
        return responseInformationRequested;
    }

    @Override
    public int getSendMaximum() {
        return sendMaximum;
    }

    @Override
    public int getSendMaximumPacketSize() {
        return sendMaximumPacketSize;
    }

    @Override
    public int getSendTopicAliasMaximum() {
        return (sendTopicAliasMapping == null) ? 0 : sendTopicAliasMapping.getTopicAliasMaximum();
    }

    public @Nullable MqttTopicAliasMapping getSendTopicAliasMapping() {
        return sendTopicAliasMapping;
    }

    @Override
    public @NotNull MqttQos getMaximumQos() {
        return maximumQos;
    }

    @Override
    public boolean isRetainAvailable() {
        return retainAvailable;
    }

    @Override
    public boolean isWildcardSubscriptionAvailable() {
        return wildcardSubscriptionAvailable;
    }

    @Override
    public boolean isSharedSubscriptionAvailable() {
        return sharedSubscriptionAvailable;
    }

    @Override
    public boolean areSubscriptionIdentifiersAvailable() {
        return subscriptionIdentifiersAvailable;
    }

    public @NotNull Channel getChannel() {
        return channel;
    }
}
