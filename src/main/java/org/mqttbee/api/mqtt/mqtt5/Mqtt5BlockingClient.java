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

package org.mqttbee.api.mqtt.mqtt5;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubscribeBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5BlockingClient extends Mqtt5Client {

    @NotNull Mqtt5ConnAck connect(@NotNull Mqtt5Connect connect);

    default @NotNull Mqtt5ConnectBuilder<Mqtt5ConnAck> connect() {
        return new Mqtt5ConnectBuilder<>(this::connect);
    }

    @NotNull Mqtt5SubAck subscribe(@NotNull Mqtt5Subscribe subscribe);

    default @NotNull Mqtt5SubscribeBuilder<Mqtt5SubAck> subscribe() {
        return new Mqtt5SubscribeBuilder<>(this::subscribe);
    }

    @NotNull Mqtt5Publish receive();

    @NotNull Mqtt5UnsubAck unsubscribe(@NotNull Mqtt5Unsubscribe unsubscribe);

    default @NotNull Mqtt5UnsubscribeBuilder<Mqtt5UnsubAck> unsubscribe() {
        return new Mqtt5UnsubscribeBuilder<>(this::unsubscribe);
    }

    @NotNull Mqtt5PublishResult publish(@NotNull Mqtt5Publish publish);

    default @NotNull Mqtt5PublishBuilder<Mqtt5PublishResult> publish() {
        return new Mqtt5PublishBuilder<>(this::publish);
    }

    boolean reauth();

    void disconnect(@NotNull Mqtt5Disconnect disconnect);

    default @NotNull Mqtt5DisconnectBuilder<Void> disconnect() {
        return new Mqtt5DisconnectBuilder<>(disconnect -> {
            disconnect(disconnect);
            return null;
        });
    }

}
