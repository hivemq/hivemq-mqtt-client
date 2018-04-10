package org.mqttbee.mqtt5.handler.publish;

import io.netty.channel.EventLoop;
import io.reactivex.Scheduler;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.message.publish.MqttPublishWrapper;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.util.collections.ScNodeList;
import org.mqttbee.util.collections.SpscIterableChunkedArrayQueue;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttIncomingPublishService {

    private final MqttIncomingPublishFlows incomingPublishFlows;
    private final Scheduler.Worker rxEventLoop;
    private final EventLoop nettyEventLoop;

    private final SpscIterableChunkedArrayQueue<QueueEntry> queue;
    private final AtomicBoolean requestOnBlocking = new AtomicBoolean();

    private final Runnable publishRunnable;
    private final AtomicBoolean scheduled = new AtomicBoolean();

    @Inject
    MqttIncomingPublishService(
            final MqttIncomingPublishFlows incomingPublishFlows,
            @Named("incomingPublish") final Scheduler.Worker worker, final MqttClientData clientData) {

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;
        final int receiveMaximum = clientConnectionData.getReceiveMaximum();

        this.incomingPublishFlows = incomingPublishFlows;
        rxEventLoop = worker;
        nettyEventLoop = clientConnectionData.getChannel().eventLoop();

        queue = new SpscIterableChunkedArrayQueue<>(receiveMaximum, 64);

        publishRunnable = this::runPublish;
    }

    public boolean onPublish(@NotNull final MqttPublishWrapper publish) {
        if (!queue.canOffer()) {
            return false; // flow control error
        }
        final ScNodeList<MqttIncomingPublishFlow> flows = incomingPublishFlows.findMatching(publish);
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
        for (final Iterator<QueueEntry> queueIt = queue.iterator(); queueIt.hasNext(); ) {
            final QueueEntry entry = queueIt.next();
            for (final Iterator<MqttIncomingPublishFlow> flowIt = entry.flows.iterator(); flowIt.hasNext(); ) {
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
//                    netty.fireEvent(ack(entry.publish)); // TODO
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
    public MqttIncomingPublishFlows getIncomingPublishFlows() {
        return incomingPublishFlows;
    }

    @NotNull
    public Scheduler.Worker getRxEventLoop() {
        return rxEventLoop;
    }

    @NotNull
    public EventLoop getNettyEventLoop() {
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
