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
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.mqtt.netty.ChannelAttributes;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * MQTT 5 disconnecter which sends a DISCONNECT messages before closing a channel from the client side. Fires
 * {@link ChannelCloseEvent}s.
 *
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5Disconnecter implements MqttDisconnecter {

    @Inject
    Mqtt5Disconnecter() {
    }

    @Override
    public void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final String reasonString) {

        final MqttDisconnect disconnect = createDisconnect(channel, reasonCode, reasonString);
        MqttDisconnectUtil.fireChannelCloseEvent(channel, new Mqtt5MessageException(disconnect, reasonString), false);
        MqttDisconnectUtil.disconnectAndClose(channel, disconnect);
    }

    @Override
    public void disconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @NotNull final Throwable cause) {

        final MqttDisconnect disconnect = createDisconnect(channel, reasonCode, cause.getMessage());
        MqttDisconnectUtil.fireChannelCloseEvent(channel, new Mqtt5MessageException(disconnect, cause), false);
        MqttDisconnectUtil.disconnectAndClose(channel, disconnect);
    }

    private MqttDisconnect createDisconnect(
            @NotNull final Channel channel, final Mqtt5DisconnectReasonCode reasonCode,
            @Nullable final String reasonString) {

        MqttUTF8StringImpl mqttReasonString = null;
        if ((reasonString != null) && ChannelAttributes.sendReasonString(channel)) {
            mqttReasonString = MqttUTF8StringImpl.from(reasonString);
        }

        return new MqttDisconnect(reasonCode, MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT, null,
                mqttReasonString, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
    }

}
