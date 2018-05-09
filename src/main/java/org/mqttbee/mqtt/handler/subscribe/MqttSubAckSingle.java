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

package org.mqttbee.mqtt.handler.subscribe;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.internal.disposables.EmptyDisposable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.ioc.ChannelComponent;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribe;

/**
 * @author Silvio Giebl
 */
public class MqttSubAckSingle extends Single<Mqtt5SubAck> {

    private final MqttSubscribe subscribe;
    private final MqttClientData clientData;

    public MqttSubAckSingle(@NotNull final MqttSubscribe subscribe, @NotNull final MqttClientData clientData) {
        this.subscribe = subscribe;
        this.clientData = clientData;
    }

    @Override
    protected void subscribeActual(final SingleObserver<? super Mqtt5SubAck> observer) {
        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        if (clientConnectionData == null) {
            EmptyDisposable.error(new NotConnectedException(), observer);
        } else {
            final ChannelComponent channelComponent = ChannelComponent.get(clientConnectionData.getChannel());
            final MqttSubscriptionHandler subscriptionHandler = channelComponent.subscriptionHandler();

            final MqttSubAckFlow flow = new MqttSubAckFlow(observer);
            observer.onSubscribe(flow);
            subscriptionHandler.subscribe(new MqttSubscribeWithFlow(subscribe, flow));
        }
    }

}
