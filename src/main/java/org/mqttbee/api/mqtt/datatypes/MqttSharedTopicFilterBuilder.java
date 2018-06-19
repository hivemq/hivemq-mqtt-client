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

import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class MqttSharedTopicFilterBuilder<P> extends MqttTopicFilterBuilder<P> {

    private final Function<? super MqttSharedTopicFilter, P> parentConsumer;

    private final String shareName;

    public MqttSharedTopicFilterBuilder(
            @NotNull final String shareName, @NotNull final String base,
            @Nullable final Function<? super MqttSharedTopicFilter, P> parentConsumer) {

        super(base, null);
        this.parentConsumer = parentConsumer;
        this.shareName = shareName;
    }

    @NotNull
    @Override
    public MqttSharedTopicFilterBuilder<P> addLevel(@NotNull final String subTopic) {
        super.addLevel(subTopic);
        return this;
    }

    @NotNull
    @Override
    public MqttSharedTopicFilterBuilder<P> singleLevelWildcard() {
        super.singleLevelWildcard();
        return this;
    }

    @NotNull
    @Override
    public MqttSharedTopicFilter multiLevelWildcardAndBuild() {
        super.multiLevelWildcardAndBuild();
        return build();
    }

    @NotNull
    @Override
    public P done() {
        return done(build(), parentConsumer);
    }

    @NotNull
    public MqttSharedTopicFilter build() {
        return MqttSharedTopicFilter.from(shareName, stringBuilder.toString());
    }

}
