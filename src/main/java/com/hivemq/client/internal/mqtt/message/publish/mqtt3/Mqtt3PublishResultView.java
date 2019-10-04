/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.message.publish.mqtt3;

import com.hivemq.client.internal.mqtt.exceptions.mqtt3.Mqtt3ExceptionFactory;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishResult;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import io.reactivex.functions.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private @NotNull String toAttributeString() {
        return "publish=" + getPublish() + ((!getError().isPresent()) ? "" : ", error=" + getError().get());
    }

    @Override
    public @NotNull String toString() {
        return "MqttPublishResult{" + toAttributeString() + '}';
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mqtt3PublishResultView)) {
            return false;
        }
        final Mqtt3PublishResultView that = (Mqtt3PublishResultView) o;

        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
