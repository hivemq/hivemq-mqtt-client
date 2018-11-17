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

package org.mqttbee.api.mqtt.datatypes;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.util.MqttChecks;

import java.nio.ByteBuffer;

/**
 * UTF-8 encoded String according to the MQTT specification.
 * <p>
 * MUST requirements: An UTF-8 encoded String
 * <ul>
 * <li>must not be longer than 65535 bytes in UTF-8 encoding,</li>
 * <li>must not contain the null character (U+0000) and</li>
 * <li>must be well-formed UTF-8 as defined by the Unicode specification, so
 * <ul>
 * <li>must not contain encodings of UTF-16 surrogates (U+D800..U+DFFF) and</li>
 * <li>must not contain non-shortest form encodings.</li>
 * </ul>
 * </ul>
 * <p>
 * SHOULD requirements: An UTF-8 encoded String
 * <ul>
 * <li>should not contain control characters (U+0001..U+001F, U+007F..U+009F) and</li>
 * <li>should not contain non-characters.</li>
 * </ul>
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface MqttUtf8String extends Comparable<MqttUtf8String> {

    /**
     * Validates and creates an UTF-8 encoded string of the given UTF-16 encoded Java string.
     *
     * @param string the UTF-16 encoded Java string.
     * @return the created UTF-8 encoded string.
     * @throws IllegalArgumentException if the string is not a valid UTF-8 encoded string.
     */
    static @NotNull MqttUtf8String of(final @NotNull String string) {
        return MqttChecks.stringNotNull(string, "String");
    }

    /**
     * Checks whether this UTF-8 encoded string contains characters that it should not according to the MQTT
     * specification.
     * <p>
     * These characters are control characters (U+0001..U+001F, U+007F..U+009F) and non-characters.
     *
     * @return whether this UTF-8 encoded string contains characters that it should not.
     */
    boolean containsShouldNotCharacters();

    /**
     * Returns the UTF-8 encoded representation as a read-only byte buffer.
     *
     * @return the UTF-8 encoded read-only byte buffer.
     */
    @NotNull ByteBuffer toByteBuffer();
}
