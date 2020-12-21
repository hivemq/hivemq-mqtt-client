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

package com.hivemq.client2.internal.mqtt.message.publish;

import com.hivemq.client2.internal.mqtt.message.MqttStatefulMessage;
import com.hivemq.client2.internal.util.UnsignedDataTypes;
import com.hivemq.client2.internal.util.collections.ImmutableIntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * @author Silvio Giebl
 */
@Unmodifiable
public class MqttStatefulPublish extends MqttStatefulMessage.WithId<MqttPublish> {

    public static final int NO_PACKET_IDENTIFIER_QOS_0 = -1;
    public static final int DEFAULT_NO_TOPIC_ALIAS = 0;
    public static final int TOPIC_ALIAS_FLAG = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
    public static final int TOPIC_ALIAS_FLAG_NEW = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE + 1;
    public static final @NotNull ImmutableIntList DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS = ImmutableIntList.of();

    private final boolean dup;
    private final int topicAlias;
    private final @NotNull ImmutableIntList subscriptionIdentifiers;

    MqttStatefulPublish(
            final @NotNull MqttPublish publish,
            final int packetIdentifier,
            final boolean dup,
            final int topicAlias,
            final @NotNull ImmutableIntList subscriptionIdentifiers) {

        super(publish, packetIdentifier);
        this.dup = dup;
        this.topicAlias = topicAlias;
        this.subscriptionIdentifiers = subscriptionIdentifiers;
    }

    public boolean isDup() {
        return dup;
    }

    public int getTopicAlias() {
        return topicAlias & TOPIC_ALIAS_FLAG;
    }

    public boolean isNewTopicAlias() {
        return (topicAlias & TOPIC_ALIAS_FLAG_NEW) != 0;
    }

    public @NotNull ImmutableIntList getSubscriptionIdentifiers() {
        return subscriptionIdentifiers;
    }

    @Override
    protected @NotNull String toAttributeString() {
        return super.toAttributeString() + ", dup=" + dup + ", topicAlias=" + topicAlias +
                ", subscriptionIdentifiers=" + subscriptionIdentifiers;
    }

    @Override
    public @NotNull String toString() {
        return "MqttStatefulPublish{" + toAttributeString() + '}';
    }
}
