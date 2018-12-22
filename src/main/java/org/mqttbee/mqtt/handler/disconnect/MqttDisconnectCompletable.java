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

package org.mqttbee.mqtt.handler.disconnect;

import io.netty.channel.Channel;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.internal.disposables.EmptyDisposable;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.NotConnectedException;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.MqttClientConnectionConfig;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.rx.CompletableFlow;

/**
 * @author Silvio Giebl
 */
public class MqttDisconnectCompletable extends Completable {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttDisconnect disconnect;

    public MqttDisconnectCompletable(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttDisconnect disconnect) {

        this.clientConfig = clientConfig;
        this.disconnect = disconnect;
    }

    @Override
    protected void subscribeActual(final @NotNull CompletableObserver s) {
        final MqttClientConnectionConfig clientConnectionConfig = clientConfig.getRawClientConnectionConfig();
        if (clientConnectionConfig == null) {
            EmptyDisposable.error(new NotConnectedException(), s);
            return;
        }
        final Channel channel = clientConnectionConfig.getChannel();
        final MqttDisconnectHandler disconnectHandler =
                (MqttDisconnectHandler) channel.pipeline().get(MqttDisconnectHandler.NAME);
        if (disconnectHandler == null) {
            EmptyDisposable.error(new NotConnectedException(), s);
            return;
        }
        final CompletableFlow flow = new CompletableFlow(s);
        s.onSubscribe(flow);
        disconnectHandler.disconnect(disconnect, flow);
    }
}
