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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class MqttTopicFilterBuilder<P>
        extends AbstractMqttTopicFilterBuilder<MqttTopicFilterBuilder<P>, MqttTopicFilter, P> {

    public MqttTopicFilterBuilder(final @Nullable Function<? super MqttTopicFilter, P> parentConsumer) {
        super(parentConsumer);
    }

    MqttTopicFilterBuilder(
            final @NotNull String base, final @Nullable Function<? super MqttTopicFilter, P> parentConsumer) {

        super(base, parentConsumer);
    }

    @Override
    @NotNull MqttTopicFilterBuilder<P> self() {
        return this;
    }

    @Override
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
