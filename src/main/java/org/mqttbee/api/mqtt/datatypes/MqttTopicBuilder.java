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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.util.FluentBuilder;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class MqttTopicBuilder<P> extends FluentBuilder<MqttTopic, P> {

    private final @NotNull StringBuilder stringBuilder;

    public MqttTopicBuilder(final @NotNull String base, final @Nullable Function<? super MqttTopic, P> parentConsumer) {
        super(parentConsumer);
        this.stringBuilder = new StringBuilder(base);
    }

    public @NotNull MqttTopicBuilder<P> addLevel(final @NotNull String subTopic) {
        Preconditions.checkNotNull(subTopic, "Subtopic must not be null.");
        stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(subTopic);
        return this;
    }

    public @NotNull MqttTopicFilterBuilder<Void> filter() {
        return new MqttTopicFilterBuilder<>(stringBuilder.toString(), null);
    }

    public @NotNull MqttSharedTopicFilterBuilder<Void> share(final @NotNull String shareName) {
        return new MqttSharedTopicFilterBuilder<>(shareName, stringBuilder.toString(), null);
    }

    @Override
    public @NotNull MqttTopic build() {
        return MqttTopic.from(stringBuilder.toString());
    }

    public @NotNull P applyTopic() {
        return apply();
    }

}
