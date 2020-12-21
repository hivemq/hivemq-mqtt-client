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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class AsyncRuntimeExceptionTest {

    @Test
    void constructor_message() {
        final TestAsyncRuntimeException exception = new TestAsyncRuntimeException("message");
        assertEquals("message", exception.getMessage());
        assertNull(exception.getCause());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void constructor_cause() {
        final TestAsyncRuntimeException exception = new TestAsyncRuntimeException(new RuntimeException("cause"));
        assertEquals("java.lang.RuntimeException: cause", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("cause", exception.getCause().getMessage());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void constructor_messageAndCause() {
        final TestAsyncRuntimeException exception =
                new TestAsyncRuntimeException("message", new RuntimeException("cause"));
        assertEquals("message", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("cause", exception.getCause().getMessage());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void noStackTrace() {
        final TestAsyncRuntimeException exception = new TestAsyncRuntimeException("message");
        assertEquals(0, exception.getStackTrace().length);
        final TestAsyncRuntimeException thrownException =
                assertThrows(TestAsyncRuntimeException.class, () -> { throw exception; });
        assertEquals(0, thrownException.getStackTrace().length);
    }

    @Test
    void fillInStackTrace_newStackTrace() {
        final TestAsyncRuntimeException exception = new TestAsyncRuntimeException("message");
        assertEquals(0, exception.getStackTrace().length);
        final RuntimeException filledException = AsyncRuntimeException.fillInStackTrace(exception);
        assertTrue(filledException instanceof TestAsyncRuntimeException);
        assertTrue(filledException.getStackTrace().length > 0);
        assertEquals("fillInStackTrace_newStackTrace", filledException.getStackTrace()[0].getMethodName());
    }

    @Test
    void fillInStackTrace_otherException() {
        final RuntimeException exception = new RuntimeException("message");
        final StackTraceElement[] stackTrace = exception.getStackTrace();
        assertAll(() -> {
            final RuntimeException filledException = AsyncRuntimeException.fillInStackTrace(exception);
            assertSame(exception, filledException);
            assertEquals(stackTrace.length, filledException.getStackTrace().length);
            assertEquals(stackTrace[0].getMethodName(), filledException.getStackTrace()[0].getMethodName());
        });
    }

    private static class TestAsyncRuntimeException extends AsyncRuntimeException {

        TestAsyncRuntimeException(final @Nullable String message) {
            super(message);
        }

        TestAsyncRuntimeException(final @Nullable String message, final @Nullable Throwable cause) {
            super(message, cause);
        }

        TestAsyncRuntimeException(final @Nullable Throwable cause) {
            super(cause);
        }

        TestAsyncRuntimeException(final @NotNull AsyncRuntimeException e) {
            super(e);
        }

        @Override
        protected @NotNull AsyncRuntimeException copy() {
            return new TestAsyncRuntimeException(this);
        }
    }
}