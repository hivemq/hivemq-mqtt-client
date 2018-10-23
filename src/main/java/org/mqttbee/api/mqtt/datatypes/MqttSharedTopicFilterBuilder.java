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
public class MqttSharedTopicFilterBuilder<P>
        extends AbstractMqttTopicFilterBuilder<MqttSharedTopicFilterBuilder<P>, MqttSharedTopicFilter, P> {

    private @NotNull String shareName;

    public MqttSharedTopicFilterBuilder(
            final @NotNull String shareName,
            final @Nullable Function<? super MqttSharedTopicFilter, P> parentConsumer) {

        super(parentConsumer);
        this.shareName = shareName;
    }

    MqttSharedTopicFilterBuilder(
            final @NotNull String shareName, final @NotNull String base,
            final @Nullable Function<? super MqttSharedTopicFilter, P> parentConsumer) {

        super(base, parentConsumer);
        this.shareName = shareName;
    }

    @Override
    @NotNull MqttSharedTopicFilterBuilder<P> self() {
        return this;
    }

    @Override
    @NotNull MqttSharedTopicFilterBuilder<P> share(final @NotNull String shareName) {
        this.shareName = shareName;
        return this;
    }

    @Override
    public @NotNull MqttSharedTopicFilter build() {
        return MqttSharedTopicFilter.from(shareName, stringBuilder.toString());
    }

    public @NotNull P applySharedTopicFilter() {
        return apply();
    }
}
