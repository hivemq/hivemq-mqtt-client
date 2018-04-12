package org.mqttbee.mqtt5.handler.publish;

import io.reactivex.Emitter;
import io.reactivex.internal.util.BackpressureHelper;
import org.jctools.queues.SpscChunkedArrayQueue;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import org.mqttbee.util.UnsignedDataTypes;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Silvio Giebl
 */
public class MqttIncomingAckFlow implements Emitter<Mqtt5PublishResult>, Subscription, Runnable {

    private static final int REQUEST_BATCH_LIMIT = 64;

    @NotNull
    private final Subscriber<? super Mqtt5PublishResult> actual;
    @NotNull
    private final MqttOutgoingPublishService outgoingPublishService;

    private final AtomicLong requested = new AtomicLong();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private volatile boolean done;
    private Throwable error;

    private final SpscChunkedArrayQueue<Mqtt5PublishResult> queue;
    private final AtomicInteger wip = new AtomicInteger();

    MqttIncomingAckFlow(
            @NotNull final Subscriber<? super Mqtt5PublishResult> actual,
            @NotNull final MqttOutgoingPublishService outgoingPublishService) {

        this.actual = actual;
        this.outgoingPublishService = outgoingPublishService;
        queue = new SpscChunkedArrayQueue<>(64, UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE);
    }

    @Override
    public void onNext(@NotNull final Mqtt5PublishResult result) {
        if (done) {
            return;
        }
        queue.offer(result);
        trySchedule();
    }

    @Override
    public void onError(@NotNull final Throwable t) {
        if (done) {
            return;
        }
        error = t;
        done = true;
        trySchedule();
    }

    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        done = true;
        trySchedule();
    }

    @Override
    public void request(final long n) {
        BackpressureHelper.add(requested, n);
        trySchedule();
    }

    @Override
    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            trySchedule();
        }
    }

    private void trySchedule() {
        if (wip.getAndIncrement() == 0) {
            outgoingPublishService.getRxEventLoop().schedule(this);
        }
    }

    @Override
    public void run() {
        int missed = 1;

        final Subscriber<? super Mqtt5PublishResult> actual = this.actual;
        final SpscChunkedArrayQueue<Mqtt5PublishResult> queue = this.queue;

        long emitted = 0;

        while (true) {
            long requested = this.requested.get();

            while (emitted != requested) {
                final boolean done = this.done;

                final Mqtt5PublishResult result = queue.poll();

                if (result == null) {
                    if (checkTerminated(done, true)) {
                        return;
                    }
                    break;
                }

                actual.onNext(result);

                emitted++;
                if (emitted == REQUEST_BATCH_LIMIT) {
                    if (requested != Long.MAX_VALUE) {
                        requested = this.requested.addAndGet(-emitted);
                    }
                    outgoingPublishService.request(emitted);
                    emitted = 0;
                }
            }

            if ((emitted == requested) && checkTerminated(done, queue.isEmpty())) {
                return;
            }

            final int wip = this.wip.get();
            if (missed == wip) {
                missed = this.wip.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            } else {
                missed = wip;
            }
        }
        if (emitted > 0) {
            if (requested.get() != Long.MAX_VALUE) {
                requested.addAndGet(-emitted);
            }
            outgoingPublishService.request(emitted);
        }
    }

    private boolean checkTerminated(final boolean done, final boolean empty) {
        if (cancelled.get()) {
            queue.clear();
            return true;
        }
        if (done && empty) {
            final Throwable e = error;
            if (e != null) {
                actual.onError(e);
            } else {
                actual.onComplete();
            }
            return true;
        }
        return false;
    }

}
