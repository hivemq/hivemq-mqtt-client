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

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClient;
import org.mqttbee.api.mqtt.mqtt5.message.connect.Mqtt5Connect;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck;
import org.mqttbee.rx.FlowableWithSingle;

/**
 * @author Silvio Giebl
 */
public interface Mqtt5Client extends MqttClient {

    @NotNull
    Single<Mqtt5ConnAck> connect(@NotNull Mqtt5Connect connect);

    @NotNull
    Single<Mqtt5SubAck> subscribe(@NotNull Mqtt5Subscribe subscribe);

    @NotNull
    FlowableWithSingle<Mqtt5SubAck, Mqtt5Publish> subscribeWithStream(@NotNull Mqtt5Subscribe subscribe);

    @NotNull
    Flowable<Mqtt5Publish> remainingPublishes();

    @NotNull
    Flowable<Mqtt5Publish> allPublishes();

    @NotNull
    Single<Mqtt5UnsubAck> unsubscribe(@NotNull Mqtt5Unsubscribe unsubscribe);

    @NotNull
    Flowable<Mqtt5PublishResult> publish(@NotNull Flowable<Mqtt5Publish> publishFlowable);

    @NotNull
    Completable reauth();

    @NotNull
    Completable disconnect(@NotNull Mqtt5Disconnect disconnect);

    @NotNull
    @Override
    Mqtt5ClientData getClientData();

}
