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

package com.hivemq.client2.internal.logging;

import com.hivemq.client2.internal.util.ClassUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public final class InternalLoggerFactory {

    private static final boolean SLF4J_AVAILABLE;

    static {
        SLF4J_AVAILABLE = ClassUtil.isAvailable("org.slf4j.Logger");
    }

    public static @NotNull InternalLogger getLogger(final @NotNull Class<?> clazz) {
        if (SLF4J_AVAILABLE) {
            return new InternalSlf4jLogger(clazz);
        }
        return InternalNoopLogger.INSTANCE;
    }

    private InternalLoggerFactory() {}
}
