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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.mqttbee.mqtt.util.MqttChecks;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttUserPropertiesImplBuilder<B extends MqttUserPropertiesImplBuilder<B>> {

    private final @NotNull ImmutableList.Builder<MqttUserPropertyImpl> listBuilder;

    MqttUserPropertiesImplBuilder() {
        listBuilder = ImmutableList.builder();
    }

    MqttUserPropertiesImplBuilder(final @NotNull Mqtt5UserProperties userProperties) {
        final ImmutableList<MqttUserPropertyImpl> list = MqttChecks.userProperties(userProperties).asList();
        listBuilder = ImmutableList.builderWithExpectedSize(list.size() + 1);
        listBuilder.addAll(list);
    }

    abstract @NotNull B self();

    public @NotNull B add(final @Nullable String name, final @Nullable String value) {
        listBuilder.add(MqttChecks.userProperty(name, value));
        return self();
    }

    public @NotNull B add(final @Nullable MqttUTF8String name, final @Nullable MqttUTF8String value) {
        listBuilder.add(MqttChecks.userProperty(name, value));
        return self();
    }

    public @NotNull B add(final @Nullable Mqtt5UserProperty userProperty) {
        listBuilder.add(MqttChecks.userProperty(userProperty));
        return self();
    }

    public @NotNull MqttUserPropertiesImpl build() {
        return MqttUserPropertiesImpl.build(listBuilder);
    }

    public static class Default extends MqttUserPropertiesImplBuilder<Default> implements Mqtt5UserPropertiesBuilder {

        public Default() {}

        public Default(final @NotNull Mqtt5UserProperties userProperties) {
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

        public Nested(final @NotNull Function<? super MqttUserPropertiesImpl, P> parentConsumer) {
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
