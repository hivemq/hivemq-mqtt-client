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

package com.hivemq.client.mqtt.mqtt5.exceptions;

import com.hivemq.client.internal.util.AsyncRuntimeException;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PubRec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * @author Silvio Giebl
 */
class Mqtt5PubRecExceptionTest {

    @Test
    void constructor() {
        final Mqtt5PubRec pubRec = mock(Mqtt5PubRec.class);
        final Mqtt5PubRecException exception = new Mqtt5PubRecException(pubRec, "message");
        assertEquals("message", exception.getMessage());
        assertNull(exception.getCause());
        assertEquals(pubRec, exception.getMqttMessage());
        assertEquals(0, exception.getStackTrace().length);
    }

    @Test
    void noStackTrace() {
        final Mqtt5PubRec pubRec = mock(Mqtt5PubRec.class);
        final Mqtt5PubRecException exception = new Mqtt5PubRecException(pubRec, "message");
        assertEquals(0, exception.getStackTrace().length);
        final Mqtt5PubRecException thrownException =
                assertThrows(Mqtt5PubRecException.class, () -> { throw exception; });
        assertEquals(0, thrownException.getStackTrace().length);
    }

    @Test
    void fillInStackTrace_newStackTrace() {
        final Mqtt5PubRec pubRec = mock(Mqtt5PubRec.class);
        final Mqtt5PubRecException exception = new Mqtt5PubRecException(pubRec, "message");
        assertEquals(0, exception.getStackTrace().length);
        final RuntimeException filledException = AsyncRuntimeException.fillInStackTrace(exception);
        assertTrue(filledException instanceof Mqtt5PubRecException);
        assertTrue(filledException.getStackTrace().length > 0);
        assertEquals("fillInStackTrace_newStackTrace", filledException.getStackTrace()[0].getMethodName());
    }

}