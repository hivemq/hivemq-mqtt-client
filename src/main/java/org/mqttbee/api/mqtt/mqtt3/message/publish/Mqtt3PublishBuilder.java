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

package org.mqttbee.api.mqtt.mqtt3.message.publish;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.mqtt.message.publish.mqtt3.Mqtt3PublishView;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt3PublishBuilder<P> extends AbstractMqtt3PublishBuilder<Mqtt3PublishBuilder<P>, Mqtt3Publish, P> {

    public Mqtt3PublishBuilder(final @Nullable Function<? super Mqtt3Publish, P> parentConsumer) {
        super(parentConsumer);
    }

    Mqtt3PublishBuilder(final @NotNull Mqtt3Publish publish) {
        super(publish);
    }

    @Override
    @NotNull Mqtt3PublishBuilder<P> self() {
        return this;
    }

    @Override
    public @NotNull Mqtt3Publish build() {
        Preconditions.checkNotNull(topic, "Topic must not be null.");
        return Mqtt3PublishView.of(topic, payload, qos, retain);
    }

    public @NotNull P applyPublish() {
        return apply();
    }
}
