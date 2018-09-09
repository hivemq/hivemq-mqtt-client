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

package org.mqttbee.api.mqtt.mqtt5.message.publish;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAck;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubComp;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRec;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRel;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5PublishResult {

    @SuppressWarnings("unchecked")
    static <T extends Mqtt5PublishResult> void when(
            final @NotNull Mqtt5PublishResult publishResult, final @NotNull Class<T> type,
            final @NotNull Consumer<T> consumer) {

        Objects.requireNonNull(publishResult, "Publish result must not be null");
        Objects.requireNonNull(type, "Type must not be null");
        Objects.requireNonNull(consumer, "Consumer must not be null");

        if (type.isInstance(publishResult)) {
            consumer.accept((T) publishResult);
        }
    }

    @NotNull Mqtt5Publish getPublish();

    @NotNull Optional<Throwable> getError();

    interface Mqtt5Qos1Result extends Mqtt5PublishResult {

        static void when(
                final @NotNull Mqtt5PublishResult publishResult, final @NotNull Consumer<Mqtt5Qos1Result> consumer) {
            Mqtt5PublishResult.when(publishResult, Mqtt5Qos1Result.class, consumer);
        }

        @NotNull Mqtt5PubAck getPubAck();
    }

    interface Mqtt5Qos2Result extends Mqtt5PublishResult {

        static void when(
                final @NotNull Mqtt5PublishResult publishResult, final @NotNull Consumer<Mqtt5Qos2Result> consumer) {
            Mqtt5PublishResult.when(publishResult, Mqtt5Qos2Result.class, consumer);
        }

        @NotNull Mqtt5PubRec getPubRec();
    }

    interface Mqtt5Qos2CompleteResult extends Mqtt5Qos2Result {

        static void when(
                final @NotNull Mqtt5PublishResult publishResult,
                final @NotNull Consumer<Mqtt5Qos2CompleteResult> consumer) {
            Mqtt5PublishResult.when(publishResult, Mqtt5Qos2CompleteResult.class, consumer);
        }

        @NotNull Mqtt5PubRel getPubRel();

        @NotNull Mqtt5PubComp getPubComp();
    }

}
