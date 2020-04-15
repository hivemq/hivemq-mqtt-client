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
import com.hivemq.client.internal.util.collections.HandleList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
@NotThreadSafe
class MqttMatchingPublishFlows extends HandleList<MqttIncomingPublishFlow> {

    boolean subscriptionFound;
    private int missingAcknowledgements;

    @Override
    public @NotNull Handle<MqttIncomingPublishFlow> add(final @NotNull MqttIncomingPublishFlow flow) {
        if (flow.manualAcknowledgement) {
            missingAcknowledgements++;
            flow.increaseMissingAcknowledgements();
        }
        return super.add(flow);
    }

    boolean areAcknowledged() {
        return missingAcknowledgements == 0;
    }

    void acknowledge(final @NotNull MqttIncomingPublishFlow flow) {
        flow.acknowledge(--missingAcknowledgements == 0);
    }
}
