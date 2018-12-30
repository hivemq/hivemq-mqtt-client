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

package org.mqttbee.util;

import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class ByteArray {

    protected final @NotNull byte[] array;
    protected int start;
    protected int end;

    public ByteArray(final @NotNull byte[] array, final int start, final int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    public int length() {
        return end - start;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteArray)) {
            return false;
        }
        final ByteArray byteArray = (ByteArray) o;
        return ByteArrayUtil.equals(array, start, end, byteArray.array, byteArray.start, byteArray.end);
    }

    @Override
    public int hashCode() {
        return ByteArrayUtil.hashCode(array, start, end);
    }
}
