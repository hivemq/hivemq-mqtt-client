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

package org.mqttbee.mqtt.codec.decoder;

import io.netty.channel.embedded.EmbeddedChannel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.mqtt.MqttClientConfig;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectHandler;

/**
 * @author Silvio Giebl
 */
public abstract class AbstractMqttDecoderTest {

    protected final @NotNull MqttClientConfig clientData;
    private final @NotNull MqttMessageDecoders decoders;

    @SuppressWarnings("NullabilityAnnotations")
    protected EmbeddedChannel channel;
    @SuppressWarnings("NullabilityAnnotations")
    private MqttDecoder decoder;

    public AbstractMqttDecoderTest(
            final @NotNull MqttClientConfig clientData, final @NotNull MqttMessageDecoders decoders) {

        this.clientData = clientData;
        this.decoders = decoders;
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
        initChannel();
    }

    protected void initChannel() {
        channel.pipeline()
                .addLast(decoder = new MqttDecoder(clientData, decoders))
                .addLast(new MqttDisconnectHandler(clientData));
    }

    protected void validatePayloadFormat() {
        assert decoder.context != null;
        decoder.context = new MqttDecoderContext(decoder.context.getMaximumPacketSize(),
                decoder.context.isProblemInformationRequested(), decoder.context.isResponseInformationRequested(), true,
                decoder.context.useDirectBufferPayload(), decoder.context.useDirectBufferAuth(),
                decoder.context.useDirectBufferCorrelationData(), decoder.context.getTopicAliasMapping());
    }

    public static @NotNull MqttPingRespDecoder createPingRespDecoder() {
        return new MqttPingRespDecoder();
    }

}
