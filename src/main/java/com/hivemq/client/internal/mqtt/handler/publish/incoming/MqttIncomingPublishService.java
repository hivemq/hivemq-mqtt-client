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

package com.hivemq.client.internal.mqtt.handler.publish.incoming;

import com.hivemq.client.internal.annotations.CallByThread;
import com.hivemq.client.internal.logging.InternalLogger;
import com.hivemq.client.internal.logging.InternalLoggerFactory;
import com.hivemq.client.internal.mqtt.ioc.ClientScope;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client.internal.util.collections.HandleList;
import com.hivemq.client.internal.util.collections.HandleList.Handle;
import com.hivemq.client.internal.util.collections.NodeList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
@ClientScope
class MqttIncomingPublishService {

    private static final @NotNull InternalLogger LOGGER =
            InternalLoggerFactory.getLogger(MqttIncomingPublishService.class);
    private static final boolean QOS_0_DROP_OLDEST = true; // TODO configurable

    private final @NotNull MqttIncomingQosHandler incomingQosHandler;

    private final @NotNull NodeList<QueueEntry> qos0Queue = new NodeList<>();
    private final @NotNull NodeList<QueueEntry> qos1Or2Queue = new NodeList<>();

    private int referencedFlowCount;
    private int runIndex;
    private int blockingFlowCount;

    MqttIncomingPublishService(final @NotNull MqttIncomingQosHandler incomingQosHandler) {
        this.incomingQosHandler = incomingQosHandler;
    }

    @CallByThread("Netty EventLoop")
    void onPublishQos0(final @NotNull MqttStatefulPublish publish, final int receiveMaximum) {
        if (qos0Queue.size() >= receiveMaximum) { // TODO receiveMaximum
            LOGGER.warn("QoS 0 publish message dropped.");
            if (QOS_0_DROP_OLDEST) {
                final QueueEntry first = qos0Queue.getFirst();
                assert first != null;
                qos0Queue.remove(first);
            } else {
                return;
            }
        }
        final HandleList<MqttIncomingPublishFlow> flows = onPublish(publish);
        if (!flows.isEmpty()) {
            qos0Queue.add(new QueueEntry(publish, flows));
        }
    }

    @CallByThread("Netty EventLoop")
    boolean onPublishQos1Or2(final @NotNull MqttStatefulPublish publish, final int receiveMaximum) {
        if (qos1Or2Queue.size() >= receiveMaximum) {
            return false; // flow control error
        }
        final HandleList<MqttIncomingPublishFlow> flows = onPublish(publish);
        if (qos1Or2Queue.isEmpty() && flows.isEmpty()) {
            incomingQosHandler.ack(publish);
        } else {
            qos1Or2Queue.add(new QueueEntry(publish, flows));
        }
        return true;
    }

    @CallByThread("Netty EventLoop")
    private @NotNull HandleList<MqttIncomingPublishFlow> onPublish(final @NotNull MqttStatefulPublish publish) {
        final HandleList<MqttIncomingPublishFlow> flows =
                incomingQosHandler.getIncomingPublishFlows().findMatching(publish);
        if (flows.isEmpty()) {
            LOGGER.warn("No publish flow registered for {}.", publish);
        }
        drain();
        for (Handle<MqttIncomingPublishFlow> h = flows.getFirst(); h != null; h = h.getNext()) {
            if (h.getElement().reference() == 1) {
                referencedFlowCount++;
            }
        }
        emit(publish.stateless(), flows);
        return flows;
    }

    @CallByThread("Netty EventLoop")
    void drain() {
        runIndex++;
        blockingFlowCount = 0;

        for (QueueEntry entry = qos1Or2Queue.getFirst(); entry != null; entry = entry.getNext()) {
            final MqttStatefulPublish publish = entry.publish;
            final HandleList<MqttIncomingPublishFlow> flows = entry.flows;
            emit(publish.stateless(), flows);
            if (flows.isEmpty() && (entry == qos1Or2Queue.getFirst())) {
                qos1Or2Queue.remove(entry);
                incomingQosHandler.ack(publish);
            } else if (blockingFlowCount == referencedFlowCount) {
                return;
            }
        }
        for (QueueEntry entry = qos0Queue.getFirst(); entry != null; entry = entry.getNext()) {
            final MqttStatefulPublish publish = entry.publish;
            final HandleList<MqttIncomingPublishFlow> flows = entry.flows;
            emit(publish.stateless(), flows);
            if (flows.isEmpty()) {
                qos0Queue.remove(entry);
            } else if (blockingFlowCount == referencedFlowCount) {
                return;
            }
        }
    }

    @CallByThread("Netty EventLoop")
    private void emit(final @NotNull MqttPublish publish, final @NotNull HandleList<MqttIncomingPublishFlow> flows) {
        for (Handle<MqttIncomingPublishFlow> h = flows.getFirst(); h != null; h = h.getNext()) {
            final MqttIncomingPublishFlow flow = h.getElement();

            if (flow.isCancelled()) {
                flows.remove(h);
                if (flow.dereference() == 0) {
                    referencedFlowCount--;
                }
            } else {
                final long requested = flow.requested(runIndex);
                if (requested > 0) {
                    flow.onNext(publish);
                    flows.remove(h);
                    if (flow.dereference() == 0) {
                        referencedFlowCount--;
                        flow.checkDone();
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

    private static class QueueEntry extends NodeList.Node<QueueEntry> {

        final @NotNull MqttStatefulPublish publish;
        final @NotNull HandleList<MqttIncomingPublishFlow> flows;

        QueueEntry(
                final @NotNull MqttStatefulPublish publish, final @NotNull HandleList<MqttIncomingPublishFlow> flows) {

            this.publish = publish;
            this.flows = flows;
        }
    }
}
