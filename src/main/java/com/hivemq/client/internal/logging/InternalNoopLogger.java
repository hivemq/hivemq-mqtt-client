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

/**
 * @author Silvio Giebl
 */
class InternalNoopLogger implements InternalLogger {

    InternalNoopLogger(@SuppressWarnings("unused") final @NotNull Class<?> clazz) {}

    @Override
    public void error(final @NotNull String message) {}

    @Override
    public void error(final @NotNull String message, final @NotNull Throwable throwable) {}

    @Override
    public void warn(final @NotNull String message) {}

    @Override
    public void warn(final @NotNull String format, final @NotNull Object... args) {}
}
