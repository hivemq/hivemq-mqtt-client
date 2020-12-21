/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client2.mqtt.mqtt5.message.publish;

import org.jetbrains.annotations.Nullable;

/**
 * Payload Format Indicator according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public enum Mqtt5PayloadFormatIndicator {

    /**
     * Payload consists of unspecified bytes.
     */
    UNSPECIFIED,
    /**
     * Payload consists of UTF-8 encoded character data as defined by the Unicode specification and restated in RFC
     * 3629.
     */
    UTF_8;

    /**
     * @return the byte code of this Payload Format Indicator.
     */
    public int getCode() {
        return ordinal();
    }

    /**
     * Returns the Payload Format Indicator belonging to the given byte code.
     *
     * @param code the byte code.
     * @return the Payload Format Indicator belonging to the byte code or null if the byte code is not a valid Payload
     *         Format Indicator.
     */
    public static @Nullable Mqtt5PayloadFormatIndicator fromCode(final int code) {
        if (code == UNSPECIFIED.getCode()) {
            return UNSPECIFIED;
        } else if (code == UTF_8.getCode()) {
            return UTF_8;
        }
        return null;
    }
}
