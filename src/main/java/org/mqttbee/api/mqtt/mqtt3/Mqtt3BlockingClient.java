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

package org.mqttbee.api.mqtt.mqtt3;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttGlobalPublishFilter;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3UnsubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.unsuback.Mqtt3UnsubAck;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
public interface Mqtt3BlockingClient extends Mqtt3Client {

    @NotNull Mqtt3ConnAck connect(@NotNull Mqtt3Connect connect);

    default @NotNull Mqtt3ConnectBuilder<Mqtt3ConnAck> connect() {
        return new Mqtt3ConnectBuilder<>(this::connect);
    }

    @NotNull Mqtt3SubAck subscribe(@NotNull Mqtt3Subscribe subscribe);

    default @NotNull Mqtt3SubscribeBuilder<Mqtt3SubAck> subscribe() {
        return new Mqtt3SubscribeBuilder<>(this::subscribe);
    }

    @NotNull Mqtt3Publishes publishes(@NotNull MqttGlobalPublishFilter filter);

    @NotNull Mqtt3UnsubAck unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

    default @NotNull Mqtt3UnsubscribeBuilder<Mqtt3UnsubAck> unsubscribe() {
        return new Mqtt3UnsubscribeBuilder<>(this::unsubscribe);
    }

    @NotNull Mqtt3PublishResult publish(@NotNull Mqtt3Publish publish);

    default @NotNull Mqtt3PublishBuilder<Mqtt3PublishResult> publish() {
        return new Mqtt3PublishBuilder<>(this::publish);
    }

    void disconnect();

    interface Mqtt3Publishes {

        @NotNull Mqtt3Publish receive() throws InterruptedException;

        @NotNull Optional<Mqtt3Publish> receive(final long timeout, final @NotNull TimeUnit timeUnit)
                throws InterruptedException;

        void cancel();
    }
}
