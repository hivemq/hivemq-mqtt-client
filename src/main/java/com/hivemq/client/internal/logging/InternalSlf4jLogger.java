/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Silvio Giebl
 */
class InternalSlf4jLogger implements InternalLogger {

    private final @NotNull Logger delegate;

    InternalSlf4jLogger(final @NotNull Class<?> clazz) {
        delegate = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void error(final @NotNull String message) {
        delegate.error(message);
    }

    @Override
    public void error(final @NotNull String message, final @NotNull Throwable throwable) {
        delegate.error(message, throwable);
    }

    @Override
    public void warn(final @NotNull String message) {
        delegate.warn(message);
    }

    @Override
    public void warn(final @NotNull String format, final @NotNull Object... args) {
        delegate.warn(format, args);
    }
}
