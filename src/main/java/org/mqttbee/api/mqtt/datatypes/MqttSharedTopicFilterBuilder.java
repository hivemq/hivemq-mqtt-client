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
import org.mqttbee.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class MqttSharedTopicFilterBuilder<P> extends MqttTopicFilterBuilder<P> {

    @NotNull
    public static <P> MqttSharedTopicFilterBuilder<P> create(
            @NotNull final String shareName, @NotNull final String base,
            @Nullable final Function<? super MqttSharedTopicFilter, P> parentConsumer) {

        return new MqttSharedTopicFilterBuilder<>(shareName, base, (parentConsumer == null) ? null :
                topicFilter -> parentConsumer.apply((MqttSharedTopicFilter) topicFilter));
    }

    private final String shareName;

    public MqttSharedTopicFilterBuilder(
            @NotNull final String shareName, @NotNull final String base,
            @Nullable final Function<? super MqttTopicFilter, P> parentConsumer) {

        super(base, parentConsumer);
        this.shareName = shareName;
    }

    @NotNull
    @Override
    public MqttSharedTopicFilterBuilder<P> addLevel(@NotNull final String subTopic) {
        super.addLevel(subTopic);
        return this;
    }

    @NotNull
    @Override
    public MqttSharedTopicFilterBuilder<P> singleLevelWildcard() {
        super.singleLevelWildcard();
        return this;
    }

    @NotNull
    @Override
    public MqttSharedTopicFilter multiLevelWildcardAndBuild() {
        multiLevelWildcard();
        return build();
    }

    @NotNull
    @Override
    public MqttSharedTopicFilter build() {
        return MqttSharedTopicFilter.from(shareName, stringBuilder.toString());
    }

}
