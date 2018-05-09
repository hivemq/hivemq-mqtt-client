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

package org.mqttbee.mqtt.handler;

import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.message.connect.MqttConnect;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provider for the channel initializer.
 *
 * @author Silvio Giebl
 */
@Singleton
public class MqttChannelInitializerProvider {

    @Inject
    MqttChannelInitializerProvider() {
    }

    /**
     * Returns the appropriate channel initializer for the given data.
     *
     * @param connect        the CONNECT message.
     * @param connAckEmitter the emitter for the CONNACK message.
     * @param clientData     the data of the client.
     * @return the appropriate channel initializer.
     */
    public MqttChannelInitializer get(
            @NotNull final MqttConnect connect, @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            @NotNull final MqttClientData clientData) {

        return new MqttChannelInitializer(connect, connAckEmitter, clientData);
    }
}
