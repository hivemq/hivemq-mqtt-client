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
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.mqtt.MqttVersion;
import org.mqttbee.internal.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.internal.mqtt.handler.disconnect.MqttDisconnectEvent;
import org.mqttbee.internal.mqtt.message.connect.MqttConnect;
import org.mqttbee.internal.mqtt.message.disconnect.MqttDisconnect;
import org.mqttbee.util.collections.IntMap;

/**
 * @author Silvio Giebl
 */
public abstract class AbstractMqttDecoderTest {

    private final @NotNull MqttMessageDecoders decoders;
    private final @NotNull MqttClientConfig clientData;
    protected @NotNull MqttConnect connect;

    protected final @NotNull ChannelHandler disconnectHandler = new ChannelInboundHandlerAdapter() {
        @Override
        public void userEventTriggered(
                final @NotNull ChannelHandlerContext ctx, final @NotNull Object evt) {

            if (evt instanceof MqttDisconnectEvent) {
                if (clientData.getMqttVersion() == MqttVersion.MQTT_3_1_1) {
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

    public AbstractMqttDecoderTest(
            final @NotNull MqttMessageDecoders decoders, final @NotNull MqttClientConfig clientData,
            final @NotNull MqttConnect connect) {

        this.decoders = decoders;
        this.clientData = clientData;
        this.connect = connect;
    }

    @BeforeEach
    protected void setUp() {
        createChannel();
    }

    @AfterEach
    protected void tearDown() {
        channel.close();
    }

    protected void createChannel() {
        channel = new EmbeddedChannel();
        channel.pipeline().addLast(decoder = new MqttDecoder(decoders, connect)).addLast(disconnectHandler);
    }

    protected void validatePayloadFormat() {
        final IntMap<MqttTopicImpl> topicAliasMapping = decoder.context.getTopicAliasMapping();
        decoder.context = new MqttDecoderContext(decoder.context.getMaximumPacketSize(),
                decoder.context.isProblemInformationRequested(), decoder.context.isResponseInformationRequested(), true,
                decoder.context.useDirectBufferPayload(), decoder.context.useDirectBufferAuth(),
                decoder.context.useDirectBufferCorrelationData(),
                (topicAliasMapping == null) ? 0 : topicAliasMapping.getMaxKey());
    }

    public static @NotNull MqttPingRespDecoder createPingRespDecoder() {
        return new MqttPingRespDecoder();
    }
}
