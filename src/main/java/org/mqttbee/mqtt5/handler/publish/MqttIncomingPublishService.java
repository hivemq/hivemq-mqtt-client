package org.mqttbee.mqtt5.handler.publish;

import io.reactivex.Scheduler;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.util.ScLinkedList;
import org.mqttbee.util.SpscIterableChunkedArrayQueue;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttIncomingPublishService {

    private final MqttSubscriptionFlows subscriptionFlows;
    private final Scheduler.Worker worker;

    private final SpscIterableChunkedArrayQueue<QueueEntry> queue;
    private final AtomicBoolean requestOnBlocking = new AtomicBoolean();

    private final Runnable publishRunnable;
    private final AtomicBoolean scheduled = new AtomicBoolean();

    @Inject
    MqttIncomingPublishService(final MqttClientData clientData, final MqttSubscriptionFlows subscriptionFlows) {
        this.subscriptionFlows = subscriptionFlows;
        worker = clientData.getExecutorConfig().getRxJavaScheduler().createWorker();

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;
        final int receiveMaximum = clientConnectionData.getReceiveMaximum();
        queue = new SpscIterableChunkedArrayQueue<>(receiveMaximum, 64);

        publishRunnable = this::runPublish;
    }

    public boolean onPublish(@NotNull final MqttPublishWrapper publish) {
        if (!queue.canOffer()) {
            return false; // flow control error
        }
        final ScLinkedList<MqttSubscriptionFlow> flows =
                subscriptionFlows.findMatching(publish.getWrapped().getTopic());
        final QueueEntry entry = new QueueEntry(publish, flows);
        queue.offer(entry);
        if (scheduled.compareAndSet(false, true)) {
            worker.schedule(publishRunnable);
        }
        return true;
    }

    private void runPublish() {
        scheduled.set(false);
        eventLoop();
    }

    void onSubscribe(@NotNull final MqttSubscriptionFlow subscriptionFlow) {
        subscriptionFlows.add(subscriptionFlow);
    }

    void onRequest(@NotNull final MqttSubscriptionFlow subscriptionFlow) {
        subscriptionFlow.scheduleRequest(worker);
    }

    void onCancel(@NotNull final MqttSubscriptionFlow subscriptionFlow) {
        subscriptionFlows.remove(subscriptionFlow);
        subscriptionFlow.scheduleCancel(worker);
    }

    void onUnsubscribe(@NotNull final MqttSubscriptionFlow subscriptionFlow) {
        subscriptionFlows.remove(subscriptionFlow);
        subscriptionFlow.scheduleUnsubscribe(worker);
    }

    void requestOnBlocking() {
        requestOnBlocking.set(true);
    }

    void eventLoop() {
        requestOnBlocking.set(false);
        boolean acknowledge = true;
        for (final Iterator<QueueEntry> queueIt = queue.iterator(); queueIt.hasNext(); ) {
            final QueueEntry entry = queueIt.next();
            for (final Iterator<MqttSubscriptionFlow> flowIt = entry.flows.iterator(); flowIt.hasNext(); ) {
                final MqttSubscriptionFlow flow = flowIt.next();

                final long requested = (acknowledge) ? flow.applyRequests() : flow.requested();
                if (flow.isCancelled()) {
                    flow.onComplete();
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
//                    netty.fireEvent(ack(entry.publish)); // TODO
                } else {
                    acknowledge = false;
                    for (final MqttSubscriptionFlow flow : entry.flows) {
                        flow.setBlocking();
                    }
                }
            } else if (requestOnBlocking.get()) {
                break;
            }
        }
    }

    private static class QueueEntry {

        private final MqttPublishWrapper publish;
        private final ScLinkedList<MqttSubscriptionFlow> flows;

        private QueueEntry(
                @NotNull final MqttPublishWrapper publish, @NotNull final ScLinkedList<MqttSubscriptionFlow> flows) {

            this.publish = publish;
            this.flows = flows;
        }

    }

}
