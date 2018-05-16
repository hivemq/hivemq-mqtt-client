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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.codec.encoder.MqttEncoder;
import org.mqttbee.mqtt.handler.auth.MqttAuthHandler;
import org.mqttbee.mqtt.handler.auth.MqttDisconnectOnAuthHandler;
import org.mqttbee.mqtt.handler.connect.MqttConnectHandler;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectHandler;
import org.mqttbee.mqtt.ioc.ChannelComponent;
import org.mqttbee.mqtt.message.connect.MqttConnect;

/**
 * Default channel initializer.
 *
 * @author Silvio Giebl
 */
public class MqttChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final MqttConnect connect;
    private final SingleEmitter<Mqtt5ConnAck> connAckEmitter;
    private final MqttClientData clientData;

    MqttChannelInitializer(
            @NotNull final MqttConnect connect, @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            @NotNull final MqttClientData clientData) {

        this.connect = connect;
        this.connAckEmitter = connAckEmitter;
        this.clientData = clientData;
    }

    @Override
    protected void initChannel(final SocketChannel channel) {
        final ChannelComponent channelComponent = ChannelComponent.create(channel, clientData);

        final ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(MqttEncoder.NAME, channelComponent.encoder());

        if (connect.getRawEnhancedAuthProvider() == null) {
            pipeline.addLast(MqttDisconnectOnAuthHandler.NAME, channelComponent.disconnectOnAuthHandler());
        } else {
            pipeline.addLast(MqttAuthHandler.NAME, channelComponent.authHandler());
        }

        pipeline.addLast(MqttConnectHandler.NAME, new MqttConnectHandler(connect, connAckEmitter, clientData));
        pipeline.addLast(MqttDisconnectHandler.NAME, channelComponent.disconnectHandler());
    }

}
