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

package org.mqttbee.internal.mqtt.message.publish;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.Immutable;
import org.mqttbee.internal.mqtt.message.MqttStatefulMessage;
import org.mqttbee.util.collections.ImmutableIntList;

/**
 * @author Silvio Giebl
 */
@Immutable
public class MqttStatefulPublish extends MqttStatefulMessage.WithId<MqttPublish> {

    public static final int NO_PACKET_IDENTIFIER_QOS_0 = -1;
    public static final int DEFAULT_NO_TOPIC_ALIAS = -1;
    public static final @NotNull ImmutableIntList DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS = ImmutableIntList.of();

    private final boolean isDup;
    private final int topicAlias;
    private final boolean isNewTopicAlias;
    private final @NotNull ImmutableIntList subscriptionIdentifiers;

    MqttStatefulPublish(
            final @NotNull MqttPublish publish, final int packetIdentifier, final boolean isDup, final int topicAlias,
            final boolean isNewTopicAlias, final @NotNull ImmutableIntList subscriptionIdentifiers) {

        super(publish, packetIdentifier);
        this.isDup = isDup;
        this.topicAlias = topicAlias;
        this.isNewTopicAlias = isNewTopicAlias;
        this.subscriptionIdentifiers = subscriptionIdentifiers;
    }

    public boolean isDup() {
        return isDup;
    }

    public int getTopicAlias() {
        return topicAlias;
    }

    public boolean isNewTopicAlias() {
        return isNewTopicAlias;
    }

    public @NotNull ImmutableIntList getSubscriptionIdentifiers() {
        return subscriptionIdentifiers;
    }
}
