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

package org.mqttbee.internal.mqtt.codec.decoder;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.internal.mqtt.advanced.MqttClientAdvancedConfig;
import org.mqttbee.internal.mqtt.advanced.MqttClientAdvancedConfigBuilder;
import org.mqttbee.internal.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.internal.mqtt.handler.disconnect.MqttDisconnectEvent;
import org.mqttbee.internal.mqtt.message.connect.MqttConnect;
import org.mqttbee.internal.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.mqtt.mqtt5.message.Mqtt5Message;

/**
 * @author Silvio Giebl
 */
public abstract class AbstractMqttDecoderTest {

    private final @NotNull MqttMessageDecoders decoders;
    private final @NotNull MqttVersion mqttVersion;
    protected @NotNull MqttConnect connect;

    protected final @NotNull ChannelHandler disconnectHandler = new ChannelInboundHandlerAdapter() {
        @Override
        public void userEventTriggered(
                final @NotNull ChannelHandlerContext ctx, final @NotNull Object evt) {

            if (evt instanceof MqttDisconnectEvent) {
                if (mqttVersion == MqttVersion.MQTT_3_1_1) {
                    ctx.channel().close();
                } else {
                    final Throwable cause = ((MqttDisconnectEvent) evt).getCause();
                    if (cause instanceof Mqtt5MessageException) {
                        final Mqtt5Message message = ((Mqtt5MessageException) cause).getMqttMessage();
                        if (message instanceof MqttDisconnect) {
                            ctx.writeAndFlush(message);
                        } else {
                            ctx.channel().close();
                        }
                    } else {
                        ctx.channel().close();
                    }
                }
            } else {
                ctx.fireUserEventTriggered(evt);
            }
        }

        @Override
        public boolean isSharable() {
            return true;
        }
    };

    @SuppressWarnings("NullabilityAnnotations")
    protected EmbeddedChannel channel;
    @SuppressWarnings("NullabilityAnnotations")
    private MqttDecoder decoder;

    protected AbstractMqttDecoderTest(
            final @NotNull MqttMessageDecoders decoders, final @NotNull MqttVersion mqttVersion,
            final @NotNull MqttConnect connect) {

        this.decoders = decoders;
        this.mqttVersion = mqttVersion;
        this.connect = connect;
    }

    @BeforeEach
    void setUp() {
        createChannel();
    }

    @AfterEach
    void tearDown() {
        channel.close();
    }

    protected void createChannel() {
        createChannel(false);
    }

    private void createChannel(final boolean validatePayloadFormat) {
        final MqttClientAdvancedConfig advancedConfig =
                new MqttClientAdvancedConfigBuilder.Default().validatePayloadFormat(validatePayloadFormat).build();
        final MqttClientConfig clientConfig =
                new MqttClientConfig(MqttVersion.MQTT_5_0, MqttClientIdentifierImpl.of("test"), "localhost", 1883,
                        MqttClientExecutorConfigImpl.DEFAULT, null, null, advancedConfig);

        channel = new EmbeddedChannel();
        channel.pipeline()
                .addLast(decoder = new MqttDecoder(decoders, clientConfig, connect))
                .addLast(disconnectHandler);
    }

    protected void validatePayloadFormat() {
        createChannel(true);
    }

    public static @NotNull MqttPingRespDecoder createPingRespDecoder() {
        return new MqttPingRespDecoder();
    }
}
