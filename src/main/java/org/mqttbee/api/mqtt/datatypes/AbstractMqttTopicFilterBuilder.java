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
abstract class AbstractMqttTopicFilterBuilder<S extends AbstractMqttTopicFilterBuilder<S, B, P>, B extends MqttTopicFilter, P>
        extends FluentBuilder<B, P> {

    final @NotNull StringBuilder stringBuilder;

    AbstractMqttTopicFilterBuilder(final @Nullable Function<? super B, P> parentConsumer) {
        super(parentConsumer);
        stringBuilder = new StringBuilder();
    }

    AbstractMqttTopicFilterBuilder(final @NotNull String base, final @Nullable Function<? super B, P> parentConsumer) {
        super(parentConsumer);
        stringBuilder = new StringBuilder(base);
    }

    abstract @NotNull S self();

    public @NotNull S addLevel(final @NotNull String subTopic) {
        Preconditions.checkNotNull(subTopic, "Subtopic must not be null.");
        stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(subTopic);
        return self();
    }

    public @NotNull S singleLevelWildcard() {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(MqttTopicFilter.SINGLE_LEVEL_WILDCARD);
        return self();
    }

    private void multiLevelWildcard() {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(MqttTopicFilter.MULTI_LEVEL_WILDCARD);
    }

    public @NotNull B multiLevelWildcardAndBuild() {
        multiLevelWildcard();
        return build();
    }

    public @NotNull P multiLevelWildcardAndApply() {
        multiLevelWildcard();
        return apply();
    }

    abstract @NotNull MqttSharedTopicFilterBuilder<P> share(final @NotNull String shareName);
}
