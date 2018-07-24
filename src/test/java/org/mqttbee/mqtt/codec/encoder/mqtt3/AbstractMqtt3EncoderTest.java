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
package org.mqttbee.mqtt.codec.encoder.mqtt3;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttClientExecutorConfigImpl;
import org.mqttbee.mqtt.MqttVersion;
import org.mqttbee.mqtt.codec.encoder.AbstractMqttEncoderTest;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoders;
import org.mqttbee.mqtt.datatypes.MqttClientIdentifierImpl;
import org.mqttbee.mqtt.message.MqttMessage;

import java.nio.charset.Charset;
import java.util.Objects;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author Alex Stockinger
 */
public abstract class AbstractMqtt3EncoderTest extends AbstractMqttEncoderTest {

    protected static final Charset UTF8 = Charset.forName("UTF-8");

    protected AbstractMqtt3EncoderTest(
            @NotNull final MqttMessageEncoders messageEncoders,
            final boolean connected
    ) {
        super(messageEncoders, connected, createClientData());
    }

    private static MqttClientData createClientData() {
        return new MqttClientData(
                MqttVersion.MQTT_3_1_1,
                Objects.requireNonNull(MqttClientIdentifierImpl.from("test")),
                "localhost",
                1883,
                null,
                null,
                false,
                false,
                MqttClientExecutorConfigImpl.DEFAULT,
                null
        );
    }

    protected void encode(final byte[] expected, final MqttMessage object) {
        assertArrayEquals(expected, bytesOf(object));
    }

    protected byte[] bytesOf(final MqttWireMessage message) throws MqttException {
        return Bytes.concat(message.getHeader(), message.getPayload());
    }

    protected byte[] bytesOf(final MqttMessage object) {
        channel.writeOutbound(object);
        final ByteBuf byteBuf = channel.readOutbound();
        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();
        return actual;
    }
}
