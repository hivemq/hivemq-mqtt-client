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
import io.reactivex.Scheduler;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.util.collections.ScNodeList;
import org.mqttbee.util.collections.SpscIterableChunkedArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttIncomingPublishService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttIncomingPublishService.class);

    private final MqttIncomingQoSHandler incomingQoSHandler; // TODO temp
    private final MqttIncomingPublishFlows incomingPublishFlows;
    private final Scheduler.Worker rxEventLoop;
    private final EventLoop nettyEventLoop;

    private final SpscIterableChunkedArrayQueue<QueueEntry> queue;
    private final AtomicBoolean requestOnBlocking = new AtomicBoolean();

    private final Runnable publishRunnable;
    private final AtomicBoolean scheduled = new AtomicBoolean();

    @Inject
    MqttIncomingPublishService(
        final MqttIncomingQoSHandler incomingQoSHandler, final MqttIncomingPublishFlows incomingPublishFlows,
        @Named("incomingPublish") final Scheduler.Worker rxEventLoop, final MqttClientData clientData) {

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        this.incomingQoSHandler = incomingQoSHandler; // TODO temp
        this.incomingPublishFlows = incomingPublishFlows;
        this.rxEventLoop = rxEventLoop;
        nettyEventLoop = clientConnectionData.getChannel().eventLoop();

        final int receiveMaximum = clientConnectionData.getReceiveMaximum();
        queue = new SpscIterableChunkedArrayQueue<>(receiveMaximum, 64);

        publishRunnable = this::runPublish;
    }

    public boolean onPublish(@NotNull final MqttPublishWrapper publish) {
        if (!queue.canOffer()) {
            return false; // flow control error
        }
        final ScNodeList<MqttIncomingPublishFlow> flows = incomingPublishFlows.findMatching(publish);
        if (flows.isEmpty()) {
            LOGGER.warn("No publish flow registered for {}.", publish);
        }
        final QueueEntry entry = new QueueEntry(publish, flows);
        queue.offer(entry);
        if (scheduled.compareAndSet(false, true)) {
            rxEventLoop.schedule(publishRunnable);
        }
        return true;
    }

    private void runPublish() {
        scheduled.set(false);
        eventLoop();
    }

    void requestOnBlocking() {
        requestOnBlocking.set(true);
    }

    void eventLoop() {
        requestOnBlocking.set(false);
        boolean acknowledge = true;

        final Iterator<QueueEntry> queueIt = queue.iterator();
        while (queueIt.hasNext()) {
            final QueueEntry entry = queueIt.next();

            final Iterator<MqttIncomingPublishFlow> flowIt = entry.flows.iterator();
            while (flowIt.hasNext()) {
                final MqttIncomingPublishFlow flow = flowIt.next();

                final long requested = (acknowledge) ? flow.applyRequests() : flow.requested();
                if (flow.isCancelled()) {
                    flowIt.remove(); // no need to dereference as the flow will stay cancelled
                } else if (requested > 0) {
                    flow.onNext(entry.publish.getWrapped());
                    flowIt.remove();
                    if ((flow.dereference() == 0) && flow.isUnsubscribed()) {
                        flow.onComplete();
                    }
                }
            }
            if (acknowledge) {
                if (entry.flows.isEmpty()) {
                    queueIt.remove();
                    incomingQoSHandler.ack(entry.publish); // TODO temp
                } else {
                    acknowledge = false;
                    for (final MqttIncomingPublishFlow flow : entry.flows) {
                        flow.setBlocking();
                    }
                }
            } else if (requestOnBlocking.get()) {
                break;
            }
        }
    }

    @NotNull
    MqttIncomingPublishFlows getIncomingPublishFlows() {
        return incomingPublishFlows;
    }

    @NotNull
    Scheduler.Worker getRxEventLoop() {
        return rxEventLoop;
    }

    @NotNull
    EventLoop getNettyEventLoop() {
        return nettyEventLoop;
    }


    private static class QueueEntry {

        private final MqttPublishWrapper publish;
        private final ScNodeList<MqttIncomingPublishFlow> flows;

        private QueueEntry(
                @NotNull final MqttPublishWrapper publish, @NotNull final ScNodeList<MqttIncomingPublishFlow> flows) {

            this.publish = publish;
            this.flows = flows;
        }

    }

}
