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

package com.hivemq.client2.internal.mqtt.handler.util;

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Silvio Giebl
 */
public abstract class FlowWithEventLoop {

    private static final int STATE_INIT = 0;
    private static final int STATE_NOT_DONE = 1;
    private static final int STATE_DONE = 2;
    private static final int STATE_CANCELLED = 3;

    private final @NotNull MqttClientConfig clientConfig;
    protected final @NotNull EventLoop eventLoop;
    private final @NotNull AtomicInteger doneState = new AtomicInteger(STATE_INIT);

    public FlowWithEventLoop(final @NotNull MqttClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        eventLoop = clientConfig.acquireEventLoop();
    }

    public boolean init() {
        if (doneState.compareAndSet(STATE_INIT, STATE_NOT_DONE)) {
            return true;
        }
        clientConfig.releaseEventLoop();
        return false;
    }

    protected boolean setDone() {
        if (doneState.compareAndSet(STATE_NOT_DONE, STATE_DONE)) {
            clientConfig.releaseEventLoop();
            return true;
        }
        return false;
    }

    public void cancel() {
        if (doneState.getAndSet(STATE_CANCELLED) == STATE_NOT_DONE) {
            onCancel();
            clientConfig.releaseEventLoop();
        }
    }

    public void dispose() {
        cancel();
    }

    protected void onCancel() {}

    public boolean isCancelled() {
        return doneState.get() == STATE_CANCELLED;
    }

    public boolean isDisposed() {
        final int doneState = this.doneState.get();
        return (doneState == STATE_DONE) || (doneState == STATE_CANCELLED);
    }

    public @NotNull EventLoop getEventLoop() {
        return eventLoop;
    }
}
