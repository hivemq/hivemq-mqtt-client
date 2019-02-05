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

package org.mqttbee.internal.mqtt.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.internal.mqtt.MqttClientConnectionConfig;
import org.mqttbee.internal.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.datatypes.MqttQos;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Silvio Giebl
 */
public abstract class AbstractMqttEncoderTest {

    private final @NotNull MqttMessageEncoders messageEncoders;
    private final boolean connected;

    @SuppressWarnings("NullabilityAnnotations")
    protected EmbeddedChannel channel;
    @SuppressWarnings("NullabilityAnnotations")
    protected MqttEncoder encoder;

    protected AbstractMqttEncoderTest(final @NotNull MqttMessageEncoders messageEncoders, final boolean connected) {
        this.messageEncoders = messageEncoders;
        this.connected = connected;
    }

    @BeforeEach
    void setUp() {
        createChannel();
    }

    @AfterEach
    void tearDown() {
        channel.close();
    }

    private void createChannel() {
        channel = new EmbeddedChannel(encoder = new MqttEncoder(messageEncoders));
        if (connected) {
            connected(MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT);
        }
    }

    protected void connected(final int maximumPacketSize) {
        encoder.onConnected(new MqttClientConnectionConfig(10, 0, false, null, 10,
                MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT, 0, true, false, 10, maximumPacketSize, 3,
                MqttQos.EXACTLY_ONCE, true, true, true, true, channel));
    }

    protected void encode(final @NotNull Object message, final @NotNull byte[] expected) {
        channel.writeOutbound(message);
        final ByteBuf actual = channel.readOutbound();

        try {
            assertEquals(expected.length, actual.readableBytes());
            for (int i = 0; i < expected.length; i++) {
                final int index = i;
                assertEquals(expected[i], actual.readByte(), () -> ("ByteBuf differed at index " + index));
            }
        } finally {
            actual.release();
        }
    }

    protected static @NotNull MqttPingReqEncoder createPingReqEncoder() {
        return new MqttPingReqEncoder();
    }
}
