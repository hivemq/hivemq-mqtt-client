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

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.util.FluentBuilder;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class MqttTopicBuilder<P> extends FluentBuilder<MqttTopic, P> {

    private final StringBuilder stringBuilder;

    public MqttTopicBuilder(@NotNull final String base, @Nullable final Function<? super MqttTopic, P> parentConsumer) {
        super(parentConsumer);
        this.stringBuilder = new StringBuilder(base);
    }

    @NotNull
    public MqttTopicBuilder<P> addLevel(@NotNull final String subTopic) {
        Preconditions.checkNotNull(subTopic);
        stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(subTopic);
        return this;
    }

    @NotNull
    public MqttTopicFilterBuilder<Void> filter() {
        return new MqttTopicFilterBuilder<>(stringBuilder.toString(), null);
    }

    @NotNull
    public MqttSharedTopicFilterBuilder<Void> share(@NotNull final String shareName) {
        return new MqttSharedTopicFilterBuilder<>(shareName, stringBuilder.toString(), null);
    }

    @NotNull
    @Override
    public MqttTopic build() {
        return MqttTopic.from(stringBuilder.toString());
    }

}
