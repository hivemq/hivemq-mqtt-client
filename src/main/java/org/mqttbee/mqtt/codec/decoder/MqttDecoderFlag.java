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

/**
 * @author Silvio Giebl
 */
public enum MqttDecoderFlag {

    PROBLEM_INFORMATION_REQUESTED,
    RESPONSE_INFORMATION_REQUESTED,
    VALIDATE_PAYLOAD_FORMAT,
    DIRECT_BUFFER_PAYLOAD,
    DIRECT_BUFFER_AUTH,
    DIRECT_BUFFER_CORRELATION_DATA;

    private final int flag = 1 << ordinal();

    public boolean isSet(final int decoderFlags) {
        return (decoderFlags & flag) != 0;
    }

    public int set(final int decoderFlags) {
        return decoderFlags | flag;
    }

}
