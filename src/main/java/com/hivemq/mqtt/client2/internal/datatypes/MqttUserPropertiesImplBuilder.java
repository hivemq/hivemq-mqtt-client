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

package com.hivemq.mqtt.client2.internal.datatypes;

import com.hivemq.mqtt.client2.datatypes.MqttUtf8String;
import com.hivemq.mqtt.client2.internal.collections.ImmutableList;
import com.hivemq.mqtt.client2.internal.util.Checks;
import com.hivemq.mqtt.client2.internal.util.MqttChecks;
import com.hivemq.mqtt.client2.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import com.hivemq.mqtt.client2.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Silvio Giebl
 */
public abstract class MqttUserPropertiesImplBuilder<B extends MqttUserPropertiesImplBuilder<B>> {

    private final ImmutableList.@NotNull Builder<MqttUserPropertyImpl> listBuilder;

    MqttUserPropertiesImplBuilder() {
        listBuilder = ImmutableList.builder();
    }

    MqttUserPropertiesImplBuilder(final @NotNull MqttUserPropertiesImpl userProperties) {
        final ImmutableList<MqttUserPropertyImpl> list = userProperties.asList();
        listBuilder = ImmutableList.builder(list.size() + 1);
        listBuilder.addAll(list);
    }

    abstract @NotNull B self();

    public @NotNull B add(final @Nullable String name, final @Nullable String value) {
        listBuilder.add(MqttUserPropertyImpl.of(name, value));
        return self();
    }

    public @NotNull B add(final @Nullable MqttUtf8String name, final @Nullable MqttUtf8String value) {
        listBuilder.add(MqttChecks.userProperty(name, value));
        return self();
    }

    public @NotNull B add(final @Nullable Mqtt5UserProperty userProperty) {
        listBuilder.add(MqttChecks.userProperty(userProperty));
        return self();
    }

    public @NotNull B addAll(final @Nullable Mqtt5UserProperty @Nullable ... userProperties) {
        Checks.notNull(userProperties, "User Properties");
        listBuilder.ensureFree(userProperties.length);
        for (final Mqtt5UserProperty userProperty : userProperties) {
            add(userProperty);
        }
        return self();
    }

    public @NotNull B addAll(final @Nullable Collection<? extends @Nullable Mqtt5UserProperty> userProperties) {
        Checks.notNull(userProperties, "User Properties");
        listBuilder.ensureFree(userProperties.size());
        userProperties.forEach(this::add);
        return self();
    }

    public @NotNull B addAll(final @Nullable Stream<? extends @Nullable Mqtt5UserProperty> userProperties) {
        Checks.notNull(userProperties, "User Properties");
        userProperties.forEach(this::add);
        return self();
    }

    public @NotNull MqttUserPropertiesImpl build() {
        return MqttUserPropertiesImpl.of(listBuilder.build());
    }

    public static class Default extends MqttUserPropertiesImplBuilder<Default> implements Mqtt5UserPropertiesBuilder {

        public Default() {}

        Default(final @NotNull MqttUserPropertiesImpl userProperties) {
            super(userProperties);
        }

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttUserPropertiesImplBuilder<Nested<P>>
            implements Mqtt5UserPropertiesBuilder.Nested<P> {

        private final @NotNull Function<? super MqttUserPropertiesImpl, P> parentConsumer;

        public Nested(
                final @NotNull MqttUserPropertiesImpl userProperties,
                final @NotNull Function<? super MqttUserPropertiesImpl, P> parentConsumer) {
            super(userProperties);
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyUserProperties() {
            return parentConsumer.apply(build());
        }
    }
}
