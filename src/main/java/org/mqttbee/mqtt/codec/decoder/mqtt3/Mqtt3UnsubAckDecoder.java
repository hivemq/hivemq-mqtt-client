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

package org.mqttbee.mqtt.codec.decoder.mqtt3;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.checkRemainingLength;

import io.netty.buffer.ByteBuf;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.mqtt3.Mqtt3UnsubAckView;

/**
 * @author Daniel Krüger
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3UnsubAckDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int REMAINING_LENGTH = 2; // 2 for the packetId

    @Inject
    Mqtt3UnsubAckDecoder() {}

    @Nullable
    @Override
    public MqttUnsubAck decode(
            final int flags,
            @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionData clientConnectionData)
            throws MqttDecoderException {

        checkFixedHeaderFlags(FLAGS, flags);
        checkRemainingLength(REMAINING_LENGTH, in.readableBytes());

        final int packetIdentifier = in.readUnsignedShort();

        return Mqtt3UnsubAckView.delegate(packetIdentifier);
    }
}
