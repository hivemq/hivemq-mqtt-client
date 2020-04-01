/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.handler.publish.incoming;

import com.hivemq.client.internal.annotations.NotThreadSafe;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client.internal.mqtt.datatypes.MqttTopicImpl;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public interface MqttSubscriptionFlows {

    void subscribe(
            @NotNull MqttSubscription subscription, int subscriptionIdentifier,
            @Nullable MqttSubscribedPublishFlow flow);

    void suback(@NotNull MqttTopicFilterImpl topicFilter, int subscriptionIdentifier, boolean error);

    void unsubscribe(
            @NotNull MqttTopicFilterImpl topicFilter,
            @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback);

    void cancel(@NotNull MqttSubscribedPublishFlow flow);

    void findMatching(@NotNull MqttTopicImpl topic, @NotNull MqttMatchingPublishFlows matchingFlows);

    void clear(@NotNull Throwable cause);
}
