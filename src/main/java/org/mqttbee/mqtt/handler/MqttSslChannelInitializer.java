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
 */
package org.mqttbee.mqtt.handler;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.MqttClientSslData;

import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.handler.ssl.SslUtil;
import org.mqttbee.mqtt.message.connect.MqttConnect;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.util.Optional;

import static org.mqttbee.mqtt.handler.ssl.SslUtil.*;
import static org.mqttbee.mqtt.handler.ssl.SslUtil.createSslEngine;

/**
 * @author Christoph Schäbel
 */
public class MqttSslChannelInitializer extends MqttChannelInitializer {

    MqttSslChannelInitializer(
            @NotNull final MqttConnect connect, @NotNull final SingleEmitter<Mqtt5ConnAck> connAckEmitter,
            @NotNull final MqttClientData clientData) {
        super(connect, connAckEmitter, clientData);
    }

    @Override
    protected void initChannel(final Channel channel) {

        final Optional<MqttClientSslData> sslDataOptional = clientData.getSslData();

        if (!sslDataOptional.isPresent()) {
            throw new IllegalStateException("Channel initializer for SSL used, but no sslData present");
        }

        try {
            //create a new SslHandler with the configured settings
            final SslHandler sslHandler = createSslHandler(channel, sslDataOptional.get());

            //add the handler as first handler to the pipeline
            channel.pipeline().addLast(sslHandler);

        } catch (final SSLException e) {
            channel.pipeline().fireExceptionCaught(e);
        }

        //initialize normal channel afterwards
        super.initChannel(channel);
    }

    @NotNull
    private SslHandler createSslHandler(
            final Channel channel, final MqttClientSslData sslData) throws SSLException {

        final SSLEngine sslEngine = createSslEngine(channel, sslData);
        final SslHandler sslHandler = new SslHandler(sslEngine);

        sslHandler.setHandshakeTimeoutMillis(sslData.handshakeTimeout());
        return sslHandler;
    }

}
