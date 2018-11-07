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
import org.mqttbee.api.mqtt.datatypes.MqttSharedTopicFilterBuilder;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilterBuilder;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.Checks;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttTopicFilterImplBuilder<B extends MqttTopicFilterImplBuilder<B>> {

    @Nullable StringBuilder stringBuilder;

    MqttTopicFilterImplBuilder() {}

    MqttTopicFilterImplBuilder(final @NotNull String baseTopicFilter) {
        Checks.notEmpty(baseTopicFilter, "Base topic filter");
        this.stringBuilder = new StringBuilder(baseTopicFilter);
    }

    abstract @NotNull B self();

    public @NotNull B addLevel(final @NotNull String topicLevel) {
        Checks.notEmpty(topicLevel, "Topic level");
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(topicLevel);
        } else {
            stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR).append(topicLevel);
        }
        return self();
    }

    public @NotNull B singleLevelWildcard() {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        } else {
            stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(MqttTopicFilter.SINGLE_LEVEL_WILDCARD);
        return self();
    }

    public @NotNull B multiLevelWildcard() {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(1);
        } else {
            stringBuilder.append(MqttTopic.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(MqttTopicFilter.MULTI_LEVEL_WILDCARD);
        return self();
    }

    public @NotNull MqttTopicFilterImpl build() {
        Checks.state(stringBuilder != null, "At least one topic level must be added.");
        final String string = stringBuilder.toString();
        Checks.state(!string.isEmpty(), "Topic must be at least one character long.");
        return MqttBuilderUtil.topicFilter(string);
    }

    public static abstract class Base<B extends Base<B>> extends MqttTopicFilterImplBuilder<B> {

        Base() {}

        Base(final @NotNull String baseTopicFilter) {
            super(baseTopicFilter);
        }

        public @NotNull MqttTopicFilterImpl build() {
            Checks.state(stringBuilder != null, "At least one topic level must be added.");
            final String string = stringBuilder.toString();
            Checks.state(!string.isEmpty(), "Topic must be at least one character long.");
            return MqttBuilderUtil.topicFilter(string);
        }
    }

    public static class Default extends Base<Default> implements MqttTopicFilterBuilder.Complete {

        public Default() {}

        public Default(final @NotNull String baseTopicFilter) {
            super(baseTopicFilter);
        }

        @Override
        @NotNull Default self() {
            return this;
        }

        @NotNull
        @Override
        public MqttTopicFilterImplBuilder.SharedDefault share(final @NotNull String shareName) {
            if (stringBuilder == null) {
                return new MqttTopicFilterImplBuilder.SharedDefault(shareName);
            }
            return new MqttTopicFilterImplBuilder.SharedDefault(shareName, stringBuilder.toString());
        }
    }

    public static class Nested<P> extends Base<Nested<P>> implements MqttTopicFilterBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super MqttTopicFilterImpl, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttTopicFilterImpl, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        public @NotNull MqttTopicFilterImplBuilder.SharedNested<P> share(final @NotNull String shareName) {
            if (stringBuilder == null) {
                return new MqttTopicFilterImplBuilder.SharedNested<>(shareName, parentConsumer);
            }
            return new MqttTopicFilterImplBuilder.SharedNested<>(shareName, stringBuilder.toString(), parentConsumer);
        }

        @Override
        public @NotNull P applyTopicFilter() {
            return parentConsumer.apply(build());
        }
    }

    public static abstract class SharedBase<B extends SharedBase<B>> extends MqttTopicFilterImplBuilder<B> {

        private final @NotNull String shareName;

        SharedBase(final @NotNull String shareName) {
            this.shareName = shareName;
        }

        SharedBase(final @NotNull String shareName, final @NotNull String baseTopicFilter) {
            super(baseTopicFilter);
            this.shareName = shareName;
        }

        public @NotNull MqttSharedTopicFilterImpl build() {
            Checks.state(stringBuilder != null, "At least one topic level must be added.");
            final String string = stringBuilder.toString();
            Checks.state(!string.isEmpty(), "Topic must be at least one character long.");
            return MqttBuilderUtil.sharedTopicFilter(shareName, string);
        }
    }

    public static class SharedDefault extends SharedBase<SharedDefault>
            implements MqttSharedTopicFilterBuilder.Complete {

        public SharedDefault(final @NotNull String shareName) {
            super(shareName);
        }

        public SharedDefault(final @NotNull String shareName, final @NotNull String baseTopicFilter) {
            super(shareName, baseTopicFilter);
        }

        @Override
        public @NotNull MqttSharedTopicFilterBuilder.Complete share(final @NotNull String shareName) {
            return this;
        }

        @Override
        @NotNull SharedDefault self() {
            return this;
        }
    }

    public static class SharedNested<P> extends SharedBase<SharedNested<P>>
            implements MqttSharedTopicFilterBuilder.Nested.Complete<P> {

        private final @NotNull Function<? super MqttSharedTopicFilterImpl, P> parentConsumer;

        SharedNested(
                final @NotNull String shareName,
                final @NotNull Function<? super MqttSharedTopicFilterImpl, P> parentConsumer) {

            super(shareName);
            this.parentConsumer = parentConsumer;
        }

        SharedNested(
                final @NotNull String shareName, final @NotNull String baseTopicFilter,
                final @NotNull Function<? super MqttSharedTopicFilterImpl, P> parentConsumer) {

            super(shareName, baseTopicFilter);
            this.parentConsumer = parentConsumer;
        }

        @Override
        public @NotNull MqttTopicFilterImplBuilder.SharedNested<P> share(final @NotNull String shareName) {
            return this;
        }

        @Override
        @NotNull SharedNested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyTopicFilter() {
            return parentConsumer.apply(build());
        }
    }
}
