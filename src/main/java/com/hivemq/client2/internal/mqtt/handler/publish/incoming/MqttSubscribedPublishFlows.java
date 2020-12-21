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

package com.hivemq.client2.internal.mqtt.handler.publish.incoming;

import com.hivemq.client2.internal.annotations.NotThreadSafe;
import com.hivemq.client2.internal.mqtt.datatypes.MqttTopicFilterImpl;
import com.hivemq.client2.internal.mqtt.message.subscribe.MqttSubscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
public interface MqttSubscribedPublishFlows {

    void subscribe(
            @NotNull MqttSubscription subscription,
            int subscriptionIdentifier,
            @Nullable MqttSubscribedPublishFlow flow);

    void suback(@NotNull MqttTopicFilterImpl topicFilter, int subscriptionIdentifier, boolean error);

    void unsubscribe(@NotNull MqttTopicFilterImpl topicFilter);

    void cancel(@NotNull MqttSubscribedPublishFlow flow);

    void findMatching(@NotNull MqttStatefulPublishWithFlows publishWithFlows);

    void clear(@NotNull Throwable cause);

    @NotNull Map<@NotNull Integer, @NotNull List<@NotNull MqttSubscription>> getSubscriptions();
}
