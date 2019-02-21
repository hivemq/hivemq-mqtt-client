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

package com.hivemq.client.internal.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public abstract class AsyncRuntimeException extends RuntimeException {

    public static @NotNull RuntimeException fillInStackTrace(final @NotNull RuntimeException e) {
        if (e instanceof AsyncRuntimeException) {
            e.fillInStackTrace();
        }
        return e;
    }

    private final boolean afterSuper;

    protected AsyncRuntimeException() {
        super();
        afterSuper = true;
    }

    protected AsyncRuntimeException(final @Nullable String message) {
        super(message, null);
        afterSuper = true;
    }

    protected AsyncRuntimeException(final @Nullable String message, final @Nullable Throwable cause) {
        super(message, cause);
        afterSuper = true;
    }

    protected AsyncRuntimeException(final @Nullable Throwable cause) {
        super(cause);
        afterSuper = true;
    }

    @Override
    public synchronized @NotNull Throwable fillInStackTrace() {
        if (afterSuper) {
            return super.fillInStackTrace();
        }
        return this;
    }
}
