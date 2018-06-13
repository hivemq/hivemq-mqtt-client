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

package org.mqttbee.api.mqtt.datatypes;

import org.mqttbee.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class MqttSharedTopicFilterBuilder extends MqttTopicFilterBuilder {

    private final String shareName;

    MqttSharedTopicFilterBuilder(@NotNull final String shareName, @NotNull final String base) {
        super(base);
        this.shareName = shareName;
    }

    @NotNull
    @Override
    public MqttSharedTopicFilterBuilder subTopic(@NotNull final String subTopic) {
        return (MqttSharedTopicFilterBuilder) super.subTopic(subTopic);
    }

    @NotNull
    @Override
    public MqttSharedTopicFilterBuilder singleLevelWildcard() {
        return (MqttSharedTopicFilterBuilder) super.singleLevelWildcard();
    }

    @NotNull
    @Override
    public MqttSharedTopicFilter multiLevelWildcard() {
        return (MqttSharedTopicFilter) super.multiLevelWildcard();
    }

    @NotNull
    public MqttSharedTopicFilter build() {
        return MqttSharedTopicFilter.from(shareName, stringBuilder.toString());
    }

}
