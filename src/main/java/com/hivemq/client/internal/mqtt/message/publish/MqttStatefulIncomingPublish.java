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

package com.hivemq.client.internal.mqtt.message.publish;

import com.hivemq.client.internal.util.collections.ImmutableIntList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttStatefulIncomingPublish extends MqttStatefulPublish {

    private long id;
    private long connectionIndex;

    MqttStatefulIncomingPublish(
            final @NotNull MqttPublish publish,
            final int packetIdentifier,
            final boolean dup,
            final int topicAlias,
            final @NotNull ImmutableIntList subscriptionIdentifiers) {

        super(publish, packetIdentifier, dup, topicAlias, subscriptionIdentifiers);
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getConnectionIndex() {
        return connectionIndex;
    }

    public void setConnectionIndex(final long connectionIndex) {
        this.connectionIndex = connectionIndex;
    }
}
