/*
 * Copyright 2018 The HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.logging;

import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class InternalLoggerFactory {

    private static final boolean SLF4J_AVAILABLE;

    static {
        SLF4J_AVAILABLE = isAvailable("org.slf4j.Logger");
    }

    private static boolean isAvailable(final @NotNull String className) {
        try {
            return Class.forName(className) != null;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    public static @NotNull InternalLogger getLogger(final @NotNull Class<?> clazz) {
        if (SLF4J_AVAILABLE) {
            return new InternalSlf4jLogger(clazz);
        }
        return new InternalNoopLogger(clazz);
    }
}
