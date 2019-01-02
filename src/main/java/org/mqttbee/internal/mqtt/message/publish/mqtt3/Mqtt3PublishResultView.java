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

package org.mqttbee.internal.mqtt.message.publish.mqtt3;

import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.internal.mqtt.message.publish.MqttPublishResult;
import org.mqttbee.internal.mqtt.mqtt3.exceptions.Mqtt3ExceptionFactory;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
public class Mqtt3PublishResultView implements Mqtt3PublishResult {

    public static final @NotNull Function<Mqtt5PublishResult, Mqtt3PublishResult> MAPPER = Mqtt3PublishResultView::of;

    public static @NotNull Mqtt3PublishResultView of(final @NotNull Mqtt5PublishResult publishResult) {
        return new Mqtt3PublishResultView((MqttPublishResult) publishResult);
    }

    private final @NotNull MqttPublishResult delegate;

    private Mqtt3PublishResultView(final @NotNull MqttPublishResult delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull Mqtt3Publish getPublish() {
        return Mqtt3PublishView.of(delegate.getPublish());
    }

    @Override
    public @NotNull Optional<Throwable> getError() {
        return delegate.getError().map(Mqtt3ExceptionFactory.MAPPER_JAVA);
    }
}
