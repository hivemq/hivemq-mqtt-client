/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.mqtt.codec.decoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.message.MqttMessage;

/**
 * Decoder for a MQTT message.
 *
 * @author Silvio Giebl
 */
public interface MqttMessageDecoder {

    /**
     * Decodes a MQTT message from the given byte buffer which was read from the given channel.
     *
     * @param flags                the flags of the fixed header.
     * @param in                   the byte buffer which contains the encoded message without the fixed header.
     * @param clientConnectionData the client data.
     * @return the decoded MQTT message or null if there are not enough byte in the byte buffer.
     * @throws MqttDecoderException if the byte buffer did not contain a valid encoded MQTT message.
     */
    @Nullable
    MqttMessage decode(int flags, @NotNull ByteBuf in, @NotNull MqttClientConnectionData clientConnectionData)
            throws MqttDecoderException;

}
