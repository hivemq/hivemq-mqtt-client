/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.mqtt.handler.publish.incoming;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.mqtt.ioc.ClientScope;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.util.collections.ChunkedArrayQueue;
import org.mqttbee.util.collections.HandleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * @author Silvio Giebl
 */
@ClientScope
class MqttIncomingPublishService {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(MqttIncomingPublishService.class);

    private final @NotNull MqttIncomingQosHandler incomingQosHandler;

    private final @NotNull ChunkedArrayQueue<QueueEntry> queue = new ChunkedArrayQueue<>(32);

    private int referencedFlowCount;
    private int runIndex;
    private int blockingFlowCount;

    MqttIncomingPublishService(final @NotNull MqttIncomingQosHandler incomingQosHandler) {
        this.incomingQosHandler = incomingQosHandler;
    }

    @CallByThread("Netty EventLoop")
    boolean onPublish(final @NotNull MqttStatefulPublish publish) {
        if (queue.size() >= incomingQosHandler.getReceiveMaximum()) {
            return false; // flow control error
        }
        final HandleList<MqttIncomingPublishFlow> flows =
                incomingQosHandler.getIncomingPublishFlows().findMatching(publish);
        if (flows.isEmpty()) {
            LOGGER.warn("No publish flow registered for {}.", publish);
        }

        drain();
        final boolean acknowledge = queue.isEmpty();
        for (final MqttIncomingPublishFlow flow : flows) {
            if (flow.reference() == 1) {
                referencedFlowCount++;
            }
        }
        emit(publish.getStatelessMessage(), flows);
        if (acknowledge && flows.isEmpty()) {
            incomingQosHandler.ack(publish);
        } else {
            queue.offer(new QueueEntry(publish, flows));
        }

        return true;
    }

    @CallByThread("Netty EventLoop")
    void drain() {
        runIndex++;
        blockingFlowCount = 0;
        boolean acknowledge = true;

        final Iterator<QueueEntry> queueIt = queue.iterator();
        while (queueIt.hasNext()) {
            final QueueEntry entry = queueIt.next();
            final MqttStatefulPublish publish = entry.publish;
            final HandleList<MqttIncomingPublishFlow> flows = entry.flows;
            emit(publish.getStatelessMessage(), flows);
            if (acknowledge && flows.isEmpty()) {
                queueIt.remove();
                incomingQosHandler.ack(publish);
            } else {
                acknowledge = false;
                if (blockingFlowCount == referencedFlowCount) {
                    break;
                }
            }
        }
    }

    @CallByThread("Netty EventLoop")
    private void emit(final @NotNull MqttPublish publish, final @NotNull HandleList<MqttIncomingPublishFlow> flows) {
        final Iterator<MqttIncomingPublishFlow> flowIt = flows.iterator();
        while (flowIt.hasNext()) {
            final MqttIncomingPublishFlow flow = flowIt.next();

            if (flow.isCancelled()) {
                flowIt.remove();
                if (flow.dereference() == 0) {
                    referencedFlowCount--;
                }
            } else {
                final long requested = flow.requested(runIndex);
                if (requested > 0) {
                    flow.onNext(publish);
                    flowIt.remove();
                    if (flow.dereference() == 0) {
                        referencedFlowCount--;
                        if (flow.isUnsubscribed()) {
                            flow.onComplete();
                        }
                    }
                } else if (requested == 0) {
                    blockingFlowCount++;
                    if (blockingFlowCount == referencedFlowCount) {
                        break;
                    }
                }
            }
        }
    }

    private static class QueueEntry {

        private final @NotNull MqttStatefulPublish publish;
        private final @NotNull HandleList<MqttIncomingPublishFlow> flows;

        private QueueEntry(
                final @NotNull MqttStatefulPublish publish, final @NotNull HandleList<MqttIncomingPublishFlow> flows) {

            this.publish = publish;
            this.flows = flows;
        }

    }

}
