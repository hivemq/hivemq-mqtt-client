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

package com.hivemq.client2.internal.util;

import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public final class Utf8Util {

    private static final long NON_SHORTEST_FORM = 0b1000_0000L << 56;
    private static final long NO_FOLLOWING_BYTE = 0b1100_0000L << 56;
    private static final long UTF_16_SURROGATES = 0b1110_0000L << 56;
    private static final long INVALID_CODE_POINTS = 0b1111_0000L << 56;

    public static long isWellFormed(final byte @NotNull [] bytes) {
        int index = 0;
        final int end = bytes.length;
        while (true) {

            byte byte1;
            do {
                if (index >= end) {
                    return 0;
                }
            } while ((byte1 = bytes[index++]) >= 0);

            if (byte1 < (byte) 0xE0) { // 2 bytes
                if (index == end) {
                    return index + NO_FOLLOWING_BYTE;
                }
                if (byte1 < (byte) 0xC2) {
                    return index + NON_SHORTEST_FORM;
                }
                final byte byte2 = bytes[index++];
                if (byte2 > (byte) 0xBF) {
                    return index + NO_FOLLOWING_BYTE;
                }
            } else if (byte1 < (byte) 0xF0) { // 3 bytes
                if (index + 1 >= end) {
                    return index + NO_FOLLOWING_BYTE;
                }
                final byte byte2 = bytes[index++];
                if (byte2 > (byte) 0xBF) {
                    return index + NO_FOLLOWING_BYTE;
                }
                if ((byte1 == (byte) 0xE0) && (byte2 < (byte) 0xA0)) {
                    return index + NON_SHORTEST_FORM;
                }
                if ((byte1 == (byte) 0xED) && (byte2 >= (byte) 0xA0)) {
                    return index + UTF_16_SURROGATES;
                }
                final byte byte3 = bytes[index++];
                if (byte3 > (byte) 0xBF) {
                    return index + NO_FOLLOWING_BYTE;
                }
            } else { // 4 bytes
                if (index + 2 >= end) {
                    return index + NO_FOLLOWING_BYTE;
                }
                final byte byte2 = bytes[index++];
                if (byte2 > (byte) 0xBF) {
                    return index + NO_FOLLOWING_BYTE;
                }
                if ((byte1 == (byte) 0xF0) && (byte2 < (byte) 0x90)) {
                    return index + NON_SHORTEST_FORM;
                }
                if ((byte1 == (byte) 0xF4) && (byte2 > (byte) 0x8F) || (byte1 > (byte) 0xF4)) {
                    return index + INVALID_CODE_POINTS;
                }
                final byte byte3 = bytes[index++];
                if (byte3 > (byte) 0xBF) {
                    return index + NO_FOLLOWING_BYTE;
                }
                final byte byte4 = bytes[index++];
                if (byte4 > (byte) 0xBF) {
                    return index + NO_FOLLOWING_BYTE;
                }
            }
        }
    }

    public static int encodedLength(final @NotNull String string) {
        final int utf16Length = string.length();
        int utf8Length = utf16Length;
        for (int i = 0; i < utf16Length; i++) {
            final char c = string.charAt(i);
            if (c > 0x7F) {
                utf8Length++;
                if (c > 0x7FF) {
                    utf8Length++;
                    if (Character.isHighSurrogate(c)) {
                        i++;
                    }
                }
            }
        }
        return utf8Length;
    }

    private Utf8Util() {}
}
