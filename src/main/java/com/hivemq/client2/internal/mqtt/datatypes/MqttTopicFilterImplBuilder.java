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

package com.hivemq.client2.internal.mqtt.datatypes;

import com.hivemq.client2.internal.util.Checks;
import com.hivemq.client2.mqtt.datatypes.MqttSharedTopicFilterBuilder;
import com.hivemq.client2.mqtt.datatypes.MqttTopicFilterBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttTopicFilterImplBuilder<B extends MqttTopicFilterImplBuilder<B>> {

    @Nullable StringBuilder stringBuilder;

    MqttTopicFilterImplBuilder() {}

    MqttTopicFilterImplBuilder(final @NotNull String baseTopicFilter) {
        stringBuilder = new StringBuilder(baseTopicFilter);
    }

    MqttTopicFilterImplBuilder(final @NotNull MqttTopicFilterImpl topicFilter) {
        stringBuilder = new StringBuilder(topicFilter.getTopicFilterString());
    }

    abstract @NotNull B self();

    public @NotNull B addLevel(final @Nullable String topicLevel) {
        Checks.notEmpty(topicLevel, "Topic level");
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(topicLevel);
        } else {
            stringBuilder.append(MqttTopicImpl.TOPIC_LEVEL_SEPARATOR).append(topicLevel);
        }
        return self();
    }

    public @NotNull B singleLevelWildcard() {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder();
        } else {
            stringBuilder.append(MqttTopicImpl.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(MqttTopicFilterImpl.SINGLE_LEVEL_WILDCARD);
        return self();
    }

    public @NotNull B multiLevelWildcard() {
        if (stringBuilder == null) {
            stringBuilder = new StringBuilder(1);
        } else {
            stringBuilder.append(MqttTopicImpl.TOPIC_LEVEL_SEPARATOR);
        }
        stringBuilder.append(MqttTopicFilterImpl.MULTI_LEVEL_WILDCARD);
        return self();
    }

    public static abstract class Base<B extends Base<B>> extends MqttTopicFilterImplBuilder<B> {

        Base() {}

        Base(final @NotNull String baseTopicFilter) {
            super(baseTopicFilter);
        }

        Base(final @NotNull MqttTopicFilterImpl topicFilter) {
            super(topicFilter);
        }

        public @NotNull MqttTopicFilterImpl build() {
            Checks.state(stringBuilder != null, "At least one topic level must be added.");
            final String string = stringBuilder.toString();
            Checks.state(!string.isEmpty(), "Topic must be at least one character long.");
            return MqttTopicFilterImpl.of(string);
        }
    }

    public static class Default extends Base<Default> implements MqttTopicFilterBuilder.Complete {

        public Default() {}

        Default(final @NotNull String baseTopicFilter) {
            super(baseTopicFilter);
        }

        Default(final @NotNull MqttTopicFilterImpl topicFilter) {
            super(topicFilter);
        }

        @Override
        @NotNull Default self() {
            return this;
        }

        @Override
        public @NotNull SharedDefault share(final @Nullable String shareName) {
            if (stringBuilder == null) {
                return new SharedDefault(shareName);
            }
            return new SharedDefault(shareName, stringBuilder.toString());
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

        @Override
        public @NotNull SharedNested<P> share(final @Nullable String shareName) {
            if (stringBuilder == null) {
                return new SharedNested<>(shareName, parentConsumer);
            }
            return new SharedNested<>(shareName, stringBuilder.toString(), parentConsumer);
        }

        @Override
        public @NotNull P applyTopicFilter() {
            return parentConsumer.apply(build());
        }
    }

    public static abstract class SharedBase<B extends SharedBase<B>> extends MqttTopicFilterImplBuilder<B> {

        private @NotNull String shareName;

        SharedBase(final @Nullable String shareName) {
            this.shareName = Checks.notNull(shareName, "Share name");
        }

        SharedBase(final @Nullable String shareName, final @NotNull String baseTopicFilter) {
            super(baseTopicFilter);
            this.shareName = Checks.notNull(shareName, "Share name");
        }

        SharedBase(final @NotNull MqttSharedTopicFilterImpl sharedTopicFilter) {
            super(sharedTopicFilter);
            this.shareName = sharedTopicFilter.getShareName();
        }

        public @NotNull B share(final @Nullable String shareName) {
            this.shareName = Checks.notNull(shareName, "Share name");
            return self();
        }

        public @NotNull MqttSharedTopicFilterImpl build() {
            Checks.state(stringBuilder != null, "At least one topic level must be added.");
            final String string = stringBuilder.toString();
            Checks.state(!string.isEmpty(), "Topic must be at least one character long.");
            return MqttSharedTopicFilterImpl.of(shareName, string);
        }
    }

    public static class SharedDefault extends SharedBase<SharedDefault>
            implements MqttSharedTopicFilterBuilder.Complete {

        public SharedDefault(final @Nullable String shareName) {
            super(shareName);
        }

        SharedDefault(final @Nullable String shareName, final @NotNull String baseTopicFilter) {
            super(shareName, baseTopicFilter);
        }

        SharedDefault(final @NotNull MqttSharedTopicFilterImpl sharedTopicFilter) {
            super(sharedTopicFilter);
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
                final @Nullable String shareName,
                final @NotNull Function<? super MqttSharedTopicFilterImpl, P> parentConsumer) {

            super(shareName);
            this.parentConsumer = parentConsumer;
        }

        SharedNested(
                final @Nullable String shareName,
                final @NotNull String baseTopicFilter,
                final @NotNull Function<? super MqttSharedTopicFilterImpl, P> parentConsumer) {

            super(shareName, baseTopicFilter);
            this.parentConsumer = parentConsumer;
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
