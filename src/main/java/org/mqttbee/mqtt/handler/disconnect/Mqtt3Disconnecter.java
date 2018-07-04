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
import javax.inject.Inject;
import javax.inject.Singleton;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;

/**
 * MQTT 3 disconnecter which just closes a channel without sending a DISCONNECT message from the
 * client side. Fires {@link ChannelCloseEvent}s.
 *
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3Disconnecter implements MqttDisconnecter {

    @Inject
    Mqtt3Disconnecter() {}

    @Override
    public void disconnect(
            @NotNull final Channel channel,
            final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final String reasonString) {

        MqttDisconnectUtil.close(channel, reasonString);
    }

    @Override
    public void disconnect(
            @NotNull final Channel channel,
            final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final Throwable cause) {

        MqttDisconnectUtil.close(channel, cause);
    }
}
