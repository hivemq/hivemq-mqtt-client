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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Silvio Giebl
 */
class UnsignedDataTypesTest {

    @ParameterizedTest
    @ValueSource(longs = {0, 1, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE})
    void isUnsignedShort_true(final long l) {
        assertTrue(UnsignedDataTypes.isUnsignedShort(l));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE + 1, Long.MAX_VALUE, Long.MIN_VALUE})
    void isUnsignedShort_false(final long l) {
        assertFalse(UnsignedDataTypes.isUnsignedShort(l));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE})
    void isUnsignedInt_true(final long l) {
        assertTrue(UnsignedDataTypes.isUnsignedInt(l));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE + 1, Long.MAX_VALUE, Long.MIN_VALUE})
    void isUnsignedInt_false(final long l) {
        assertFalse(UnsignedDataTypes.isUnsignedInt(l));
    }
}