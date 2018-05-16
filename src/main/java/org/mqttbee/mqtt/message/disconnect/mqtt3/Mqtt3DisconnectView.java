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

package org.mqttbee.mqtt.message.disconnect.mqtt3;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.disconnect.Mqtt3Disconnect;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;

import javax.annotation.concurrent.Immutable;

/**
 * @author Silvio Giebl
 */
@Immutable
public class Mqtt3DisconnectView implements Mqtt3Disconnect {

    private static MqttDisconnect WRAPPED;
    private static Mqtt3DisconnectView INSTANCE;

    @NotNull
    public static MqttDisconnect wrapped() {
        if (WRAPPED != null) {
            return WRAPPED;
        }
        return WRAPPED = new MqttDisconnect(Mqtt5DisconnectReasonCode.NORMAL_DISCONNECTION,
                MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null, null,
                MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

    @NotNull
    public static Mqtt3DisconnectView create() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        return INSTANCE = new Mqtt3DisconnectView();
    }

    private Mqtt3DisconnectView() {
    }

    @NotNull
    public MqttDisconnect getWrapped() {
        return WRAPPED;
    }

}
