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

package com.hivemq.client2.internal.mqtt.handler.publish.incoming;

import com.hivemq.client2.internal.checkpoint.Confirmable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Silvio Giebl
 */
class MqttIncomingPublishConfirmable implements Confirmable, Runnable {

    private final @NotNull MqttIncomingPublishFlow flow;
    private final @NotNull MqttStatefulPublishWithFlows publishWithFlows;
    private final @NotNull AtomicBoolean confirmed = new AtomicBoolean(false);

    MqttIncomingPublishConfirmable(
            final @NotNull MqttIncomingPublishFlow flow, final @NotNull MqttStatefulPublishWithFlows publishWithFlows) {

        this.flow = flow;
        this.publishWithFlows = publishWithFlows;
    }

    @Override
    public long getId() {
        return publishWithFlows.id;
    }

    @Override
    public boolean confirm() {
        if (confirmed.compareAndSet(false, true)) {
            flow.getEventLoop().execute(this);
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        publishWithFlows.acknowledge(flow);
    }

    static class Qos0 implements Confirmable {

        private final @NotNull AtomicBoolean confirmed = new AtomicBoolean(false);

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public boolean confirm() {
            return confirmed.compareAndSet(false, true);
        }
    }
}
