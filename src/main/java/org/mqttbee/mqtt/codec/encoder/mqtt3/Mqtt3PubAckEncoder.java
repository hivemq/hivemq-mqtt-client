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

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt.codec.encoder.mqtt3.Mqtt3MessageEncoder.Mqtt3MessageFixedSizeEncoder;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3PubAckEncoder extends Mqtt3MessageFixedSizeEncoder<MqttPubAck> {

    private static final int FIXED_HEADER = Mqtt3MessageType.PUBACK.getCode() << 4;
    private static final int REMAINING_LENGTH = 2;
    private static final int ENCODED_LENGTH = 2 + REMAINING_LENGTH;

    @Inject
    Mqtt3PubAckEncoder() {
    }

    @Override
    int encodedLength() {
        return ENCODED_LENGTH;
    }

    @Override
    public void encode(@NotNull final MqttPubAck message, @NotNull final ByteBuf out) {
        encodeFixedHeader(out);
        encodeVariableHeader(message, out);
    }

    private void encodeFixedHeader(@NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        out.writeByte(REMAINING_LENGTH);
    }

    private void encodeVariableHeader(@NotNull final MqttPubAck message, @NotNull final ByteBuf out) {
        out.writeShort(message.getPacketIdentifier());
    }

}
