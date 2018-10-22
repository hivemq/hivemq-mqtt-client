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
public class MqttTopicFilterBuilder<P> extends FluentBuilder<MqttTopicFilter, P> {

    final @NotNull StringBuilder stringBuilder;

    public MqttTopicFilterBuilder(
            final @NotNull String base, final @Nullable Function<? super MqttTopicFilter, P> parentConsumer) {

        super(parentConsumer);
        stringBuilder = new StringBuilder(base);
    }

    public @NotNull MqttTopicFilterBuilder<P> addLevel(final @NotNull String subTopic) {
        Preconditions.checkNotNull(subTopic, "Subtopic must not be null.");
        stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(subTopic);
        return this;
    }

    public @NotNull MqttTopicFilterBuilder<P> singleLevelWildcard() {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(MqttTopicFilter.SINGLE_LEVEL_WILDCARD);
        return this;
    }

    void multiLevelWildcard() {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(MqttTopicFilter.MULTI_LEVEL_WILDCARD);
    }

    public @NotNull MqttTopicFilter multiLevelWildcardAndBuild() {
        multiLevelWildcard();
        return build();
    }

    public @NotNull P multiLevelWildcardAndApply() {
        multiLevelWildcard();
        return apply();
    }

    public @NotNull MqttSharedTopicFilterBuilder<P> share(final @NotNull String shareName) {
        return new MqttSharedTopicFilterBuilder<>(shareName, stringBuilder.toString(), parentConsumer);
    }

    @Override
    public @NotNull MqttTopicFilter build() {
        return MqttTopicFilter.from(stringBuilder.toString());
    }

    public @NotNull P applyTopicFilter() {
        return apply();
    }

}
