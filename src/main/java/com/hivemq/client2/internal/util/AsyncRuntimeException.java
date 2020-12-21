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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * @author Silvio Giebl
 */
public abstract class AsyncRuntimeException extends RuntimeException {

    public static @NotNull RuntimeException fillInStackTrace(final @NotNull RuntimeException e) {
        if (e instanceof AsyncRuntimeException) {
            final AsyncRuntimeException copy = ((AsyncRuntimeException) e).copy();
            final StackTraceElement[] stackTrace = copy.getStackTrace();
            // remove the copy and fillInStackTrace method calls from the trace
            int remove = 0;
            while (remove < stackTrace.length) {
                final StackTraceElement stackTraceElement = stackTrace[remove];
                remove++;
                if (stackTraceElement.getClassName().equals(AsyncRuntimeException.class.getCanonicalName()) &&
                        stackTraceElement.getMethodName().equals("fillInStackTrace")) {
                    break;
                }
            }
            copy.setStackTrace(Arrays.copyOfRange(stackTrace, remove, stackTrace.length));
            return copy;
        }
        return e;
    }

    protected AsyncRuntimeException(final @Nullable String message) {
        super(message, null);
    }

    protected AsyncRuntimeException(final @Nullable String message, final @Nullable Throwable cause) {
        super(message, cause);
    }

    protected AsyncRuntimeException(final @Nullable Throwable cause) {
        super(cause);
    }

    protected AsyncRuntimeException(final @NotNull AsyncRuntimeException e) {
        super(e.getMessage(), e.getCause());
        super.fillInStackTrace();
    }

    @Override
    public synchronized @NotNull Throwable fillInStackTrace() {
        return this;
    }

    protected abstract @NotNull AsyncRuntimeException copy();
}
