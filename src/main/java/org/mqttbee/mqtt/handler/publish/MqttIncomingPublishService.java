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

package org.mqttbee.mqtt.handler.publish;

import io.netty.channel.EventLoop;
import org.mqttbee.annotations.CallByThread;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.ioc.ClientScope;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.util.collections.ChunkedArrayQueue;
import org.mqttbee.util.collections.ScNodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Iterator;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttIncomingPublishService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttIncomingPublishService.class);

    private final MqttIncomingQosHandler incomingQosHandler; // TODO temp
    private final MqttIncomingPublishFlows incomingPublishFlows;
    private final EventLoop nettyEventLoop;

    private final ChunkedArrayQueue<QueueEntry> queue;
    private final int receiveMaximum;

    private int referencedFlowCount;
    private int runIndex;
    private int blockingFlowCount;

    @Inject
    MqttIncomingPublishService(
            final MqttIncomingQosHandler incomingQosHandler, final MqttIncomingPublishFlows incomingPublishFlows,
            final MqttClientData clientData) {

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        this.incomingQosHandler = incomingQosHandler; // TODO temp
        this.incomingPublishFlows = incomingPublishFlows;
        nettyEventLoop = clientConnectionData.getChannel().eventLoop();

        queue = new ChunkedArrayQueue<>(32);
        receiveMaximum = clientConnectionData.getReceiveMaximum();
    }

    @CallByThread("Netty EventLoop")
    boolean onPublish(@NotNull final MqttStatefulPublish publish) {
        if (queue.size() >= receiveMaximum) {
            return false; // flow control error
        }
        final ScNodeList<MqttIncomingPublishFlow> flows = incomingPublishFlows.findMatching(publish);
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
            final ScNodeList<MqttIncomingPublishFlow> flows = entry.flows;
            emit(publish.getStatelessMessage(), flows);
            if (acknowledge && flows.isEmpty()) {
                queueIt.remove();
                incomingQosHandler.ack(publish); // TODO temp
            } else {
                acknowledge = false;
                if (blockingFlowCount == referencedFlowCount) {
                    break;
                }
            }
        }
    }

    @CallByThread("Netty EventLoop")
    private void emit(@NotNull final MqttPublish publish, @NotNull final ScNodeList<MqttIncomingPublishFlow> flows) {
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

    @NotNull
    MqttIncomingPublishFlows getIncomingPublishFlows() {
        return incomingPublishFlows;
    }

    @NotNull
    EventLoop getNettyEventLoop() {
        return nettyEventLoop;
    }

    private static class QueueEntry {

        private final MqttStatefulPublish publish;
        private final ScNodeList<MqttIncomingPublishFlow> flows;

        private QueueEntry(
                @NotNull final MqttStatefulPublish publish, @NotNull final ScNodeList<MqttIncomingPublishFlow> flows) {

            this.publish = publish;
            this.flows = flows;
        }

    }

}
