/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt;

import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttTopicAliasAutoMapping;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttTopicAliasMapping;
import com.hivemq.client.internal.util.UnsignedDataTypes;
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

    private static final int FLAG_HAS_SIMPLE_AUTH = 1;
    private static final int FLAG_HAS_WILL_PUBLISH = 1 << 1;
    private static final int FLAG_PROBLEM_INFORMATION_REQUESTED = 1 << 2;
    private static final int FLAG_RESPONSE_INFORMATION_REQUESTED = 1 << 3;
    private static final int FLAG_RETAIN_AVAILABLE = 1 << 4;
    private static final int FLAG_WILDCARD_SUBSCRIPTION_AVAILABLE = 1 << 5;
    private static final int FLAG_SHARED_SUBSCRIPTION_AVAILABLE = 1 << 6;
    private static final int FLAG_SUBSCRIPTION_IDENTIFIERS_AVAILABLE = 1 << 7;
    private static final int FLAG_CLEAN_START = 1 << 8;
    private static final int FLAG_CLEAN_STOP = 1 << 9;

    private final @NotNull MqttTransportConfigImpl transportConfig;
    private final short keepAlive;
    private volatile int sessionExpiryInterval;
    private final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism;
    private final short receiveMaximum;
    private final int maximumPacketSize;
    private final short topicAliasMaximum;
    private final short sendMaximum;
    private final int sendMaximumPacketSize;
    private final @Nullable MqttTopicAliasMapping sendTopicAliasMapping;
    private final @NotNull MqttQos maximumQos;
    private final @NotNull Channel channel;
    private final int flags;

    public MqttClientConnectionConfig(
            final @NotNull MqttTransportConfigImpl transportConfig,
            final int keepAlive,
            final boolean cleanStart,
            final boolean cleanStop,
            final long sessionExpiryInterval,
            final boolean hasSimpleAuth,
            final boolean hasWillPublish,
            final @Nullable Mqtt5EnhancedAuthMechanism enhancedAuthMechanism,
            final int receiveMaximum,
            final int maximumPacketSize,
            final int topicAliasMaximum,
            final boolean problemInformationRequested,
            final boolean responseInformationRequested,
            final int sendMaximum,
            final int sendMaximumPacketSize,
            final int sendTopicAliasMaximum,
            final @NotNull MqttQos maximumQos,
            final boolean retainAvailable,
            final boolean wildcardSubscriptionAvailable,
            final boolean sharedSubscriptionAvailable,
            final boolean subscriptionIdentifiersAvailable,
            final @NotNull Channel channel) {

        this.transportConfig = transportConfig;
        this.keepAlive = (short) keepAlive;
        this.sessionExpiryInterval = (int) sessionExpiryInterval;
        this.enhancedAuthMechanism = enhancedAuthMechanism;
        this.receiveMaximum = (short) receiveMaximum;
        this.maximumPacketSize = maximumPacketSize;
        this.topicAliasMaximum = (short) topicAliasMaximum;
        this.sendMaximum = (short) sendMaximum;
        this.sendMaximumPacketSize = sendMaximumPacketSize;
        this.sendTopicAliasMapping =
                (sendTopicAliasMaximum == 0) ? null : new MqttTopicAliasAutoMapping(sendTopicAliasMaximum);
        this.maximumQos = maximumQos;
        this.channel = channel;

        int flags = 0;
        if (hasSimpleAuth) {
            flags |= FLAG_HAS_SIMPLE_AUTH;
        }
        if (hasWillPublish) {
            flags |= FLAG_HAS_WILL_PUBLISH;
        }
        if (problemInformationRequested) {
            flags |= FLAG_PROBLEM_INFORMATION_REQUESTED;
        }
        if (responseInformationRequested) {
            flags |= FLAG_RESPONSE_INFORMATION_REQUESTED;
        }
        if (retainAvailable) {
            flags |= FLAG_RETAIN_AVAILABLE;
        }
        if (wildcardSubscriptionAvailable) {
            flags |= FLAG_WILDCARD_SUBSCRIPTION_AVAILABLE;
        }
        if (sharedSubscriptionAvailable) {
            flags |= FLAG_SHARED_SUBSCRIPTION_AVAILABLE;
        }
        if (subscriptionIdentifiersAvailable) {
            flags |= FLAG_SUBSCRIPTION_IDENTIFIERS_AVAILABLE;
        }
        if (cleanStart) {
            flags |= FLAG_CLEAN_START;
        }
        if (cleanStop) {
            flags |= FLAG_CLEAN_STOP;
        }
        this.flags = flags;
    }

    @Override
    public @NotNull MqttTransportConfigImpl getTransportConfig() {
        return transportConfig;
    }

    @Override
    public int getKeepAlive() {
        return keepAlive & UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    }

    public boolean isCleanStart() {
        return (flags & FLAG_CLEAN_START) != 0;
    }

    public boolean isCleanStop() {
        return (flags & FLAG_CLEAN_STOP) != 0;
    }

    @Override
    public long getSessionExpiryInterval() {
        return sessionExpiryInterval & UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE;
    }

    public void setSessionExpiryInterval(final long sessionExpiryInterval) {
        this.sessionExpiryInterval = (int) sessionExpiryInterval;
    }

    @Override
    public boolean hasSimpleAuth() {
        return (flags & FLAG_HAS_SIMPLE_AUTH) != 0;
    }

    @Override
    public boolean hasWillPublish() {
        return (flags & FLAG_HAS_WILL_PUBLISH) != 0;
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
        return receiveMaximum & UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    }

    @Override
    public int getMaximumPacketSize() {
        return maximumPacketSize;
    }

    @Override
    public int getTopicAliasMaximum() {
        return topicAliasMaximum & UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    }

    @Override
    public boolean isProblemInformationRequested() {
        return (flags & FLAG_PROBLEM_INFORMATION_REQUESTED) != 0;
    }

    @Override
    public boolean isResponseInformationRequested() {
        return (flags & FLAG_RESPONSE_INFORMATION_REQUESTED) != 0;
    }

    @Override
    public int getSendMaximum() {
        return sendMaximum & UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
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
        return (flags & FLAG_RETAIN_AVAILABLE) != 0;
    }

    @Override
    public boolean isWildcardSubscriptionAvailable() {
        return (flags & FLAG_WILDCARD_SUBSCRIPTION_AVAILABLE) != 0;
    }

    @Override
    public boolean isSharedSubscriptionAvailable() {
        return (flags & FLAG_SHARED_SUBSCRIPTION_AVAILABLE) != 0;
    }

    @Override
    public boolean areSubscriptionIdentifiersAvailable() {
        return (flags & FLAG_SUBSCRIPTION_IDENTIFIERS_AVAILABLE) != 0;
    }

    public @NotNull Channel getChannel() {
        return channel;
    }
}
