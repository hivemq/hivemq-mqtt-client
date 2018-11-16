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

package org.mqttbee.mqtt.datatypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttTopicBuilder;
import org.mqttbee.util.Checks;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttTopicImplBuilder<B extends MqttTopicImplBuilder> {

    @Nullable StringBuilder stringBuilder;

    MqttTopicImplBuilder() {}

    MqttTopicImplBuilder(final @NotNull String baseTopic) {
        Checks.notEmpty(baseTopic, "Base topic");
        this.stringBuilder = new StringBuilder(baseTopic);
    }

    abstract @NotNull B self();

    public @NotNull B addLevel(final @Nullable String topicLevel) {
        Checks.notEmpty(topicLevel, "Topic level");
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(topicLevel);
        } else {
            stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(topicLevel);
        }
        return self();
    }

    public @NotNull MqttTopicImpl build() {
        Checks.state(stringBuilder != null, "At least one topic level must be added.");
        final String string = stringBuilder.toString();
        Checks.state(!string.isEmpty(), "Topic must be at least one character long.");
        return MqttTopicImpl.from(string);
    }

    public static class Default extends MqttTopicImplBuilder<Default> implements MqttTopicBuilder.Complete {

        public Default() {}

        public Default(final @NotNull String base) {
            super(base);
        }

        @Override
        @NotNull Default self() {
            return this;
        }

        public @NotNull MqttTopicFilterImplBuilder.Default filter() {
            if (stringBuilder == null) {
                return new MqttTopicFilterImplBuilder.Default();
            }
            return new MqttTopicFilterImplBuilder.Default(stringBuilder.toString());
        }

        public @NotNull MqttTopicFilterImplBuilder.SharedDefault share(final @Nullable String shareName) {
            if (stringBuilder == null) {
                return new MqttTopicFilterImplBuilder.SharedDefault(shareName);
            }
            return new MqttTopicFilterImplBuilder.SharedDefault(shareName, stringBuilder.toString());
        }
    }

    public static class Nested<P> extends MqttTopicImplBuilder<Nested<P>>
            implements MqttTopicBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super MqttTopicImpl, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttTopicImpl, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyTopic() {
            return parentConsumer.apply(build());
        }
    }
}
