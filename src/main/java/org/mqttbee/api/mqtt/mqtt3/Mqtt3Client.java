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

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClient;
import org.mqttbee.api.mqtt.mqtt3.message.connect.Mqtt3Connect;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3Publish;
import org.mqttbee.api.mqtt.mqtt3.message.publish.Mqtt3PublishResult;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import org.mqttbee.api.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import org.mqttbee.rx.FlowableWithSingle;

/**
 * @author Silvio Giebl
 */
public interface Mqtt3Client extends MqttClient {

    @NotNull
    Single<Mqtt3ConnAck> connect(@NotNull Mqtt3Connect connect);

    @NotNull
    FlowableWithSingle<Mqtt3SubAck, Mqtt3Publish> subscribe(@NotNull Mqtt3Subscribe subscribe);

    @NotNull
    Flowable<Mqtt3Publish> remainingPublishes();

    @NotNull
    Flowable<Mqtt3Publish> allPublishes();

    @NotNull
    Completable unsubscribe(@NotNull Mqtt3Unsubscribe unsubscribe);

    @NotNull
    Flowable<Mqtt3PublishResult> publish(@NotNull Flowable<Mqtt3Publish> publishFlowable);

    @NotNull
    Completable disconnect();

    @NotNull
    @Override
    Mqtt3ClientData getClientData();

}
