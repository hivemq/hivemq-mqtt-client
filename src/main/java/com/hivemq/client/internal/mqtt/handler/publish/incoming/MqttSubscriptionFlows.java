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
import com.hivemq.client.internal.util.collections.HandleList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public interface MqttSubscriptionFlows {

    void subscribe(@NotNull MqttTopicFilterImpl topicFilter, @Nullable MqttSubscribedPublishFlow flow);

    void remove(@NotNull MqttTopicFilterImpl topicFilter, @Nullable MqttSubscribedPublishFlow flow);

    void unsubscribe(
            @NotNull MqttTopicFilterImpl topicFilter,
            @Nullable Consumer<MqttSubscribedPublishFlow> unsubscribedCallback);

    void cancel(@NotNull MqttSubscribedPublishFlow flow);

    boolean findMatching(@NotNull MqttTopicImpl topic, @NotNull HandleList<MqttIncomingPublishFlow> matchingFlows);

    void clear(@NotNull Throwable cause);
}
