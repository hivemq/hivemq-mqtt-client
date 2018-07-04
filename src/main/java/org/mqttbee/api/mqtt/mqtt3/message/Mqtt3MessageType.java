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

package org.mqttbee.api.mqtt.mqtt3.message;

import org.mqttbee.annotations.Nullable;

/**
 * MQTT message type according to the MQTT 3 specification. See also <a
 * href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html">the official MQTT 3.1.1
 * specification</a>.
 *
 * @author Silvio Giebl
 */
public enum Mqtt3MessageType {
    RESERVED_ZERO,
    CONNECT,
    CONNACK,
    PUBLISH,
    PUBACK,
    PUBREC,
    PUBREL,
    PUBCOMP,
    SUBSCRIBE,
    SUBACK,
    UNSUBSCRIBE,
    UNSUBACK,
    PINGREQ,
    PINGRESP,
    DISCONNECT;

    /** @return the byte code of this MQTT message type. */
    public int getCode() {
        return ordinal();
    }

    /**
     * Returns the MQTT message type belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the MQTT message type belonging to the given byte code or null if the byte code is
     *     not a valid MQTT 3 message type code.
     */
    @Nullable
    public static Mqtt3MessageType fromCode(final int code) {
        final Mqtt3MessageType[] values = values();
        if (code < 0 || code >= values.length) {
            return null;
        }
        return values[code];
    }
}
