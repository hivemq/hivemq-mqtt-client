package org.mqttbee.mqtt5.handler.publish;

import com.google.common.collect.ImmutableList;
import io.reactivex.Emitter;
import io.reactivex.Scheduler;
import io.reactivex.internal.util.BackpressureHelper;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeResult;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Silvio Giebl
 */
public class MqttSubscriptionFlow implements Emitter<MqttSubscribeResult>, Subscription {

    private final Subscriber<? super MqttSubscribeResult> actual;
    private final ImmutableList<MqttTopicFilter> topicFilters;
    private final MqttIncomingPublishService incomingPublishService;

    private long requested;
    private final AtomicLong newRequested = new AtomicLong();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final AtomicBoolean unsubscribed = new AtomicBoolean();
    private boolean done;

    private final AtomicInteger referenced = new AtomicInteger();
    private final AtomicBoolean blocking = new AtomicBoolean();

    private final Runnable requestRunnable = this::runRequest;
    private final Runnable cancelRunnable = this::runCancel;
    private final Runnable unsubscribeRunnable = this::unsubscribe;
    private final AtomicBoolean scheduled = new AtomicBoolean();

    MqttSubscriptionFlow(
            @NotNull final Subscriber<? super MqttSubscribeResult> actual,
            @NotNull final ImmutableList<MqttTopicFilter> topicFilters,
            @NotNull final MqttIncomingPublishService incomingPublishService) {

        this.actual = actual;
        this.topicFilters = topicFilters;
        this.incomingPublishService = incomingPublishService;
    }

    @Override
    public void onNext(@NotNull final MqttSubscribeResult result) {
        if (done) {
            return;
        }
        actual.onNext(result);
        if (requested != Long.MAX_VALUE) {
            requested--;
        }
    }

    @Override
    public void onError(@NotNull final Throwable t) {
        if (done) {
            return;
        }
        done = true;
        actual.onError(t);
    }

    @Override
    public void onComplete() {
        if (done) {
            return;
        }
        done = true;
        actual.onComplete();
    }

    @Override
    public void request(final long n) {
        BackpressureHelper.add(newRequested, n);
        if (!cancelled.get()) {
            incomingPublishService.onRequest(this);
        }
    }

    void scheduleRequest(@NotNull final Scheduler.Worker worker) {
        schedule(worker, requestRunnable);
    }

    private void runRequest() {
        scheduled.set(false);
        applyRequests();
        if (referenced() > 0) {
            incomingPublishService.eventLoop();
        }
    }

    long requested() {
        return requested;
    }

    long applyRequests() { // called sequentially with onNext
        long requested = this.requested;
        if (requested == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        final long newRequested = this.newRequested.getAndSet(0);
        if (newRequested != 0) {
            if (requested == 0) {
                blocking.set(false);
            }
            requested = BackpressureHelper.addCap(requested, newRequested);
            this.requested = requested;
        }
        return requested;
    }

    @Override
    public void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            incomingPublishService.onCancel(this);
        }
    }

    void scheduleCancel(@NotNull final Scheduler.Worker worker) {
        schedule(worker, cancelRunnable);
    }

    private void runCancel() {
        scheduled.set(false);
        if (referenced() == 0) {
            onComplete();
        } else {
            incomingPublishService.eventLoop();
        }
    }

    boolean isCancelled() {
        return cancelled.get();
    }

    void unsubscribe() {
        unsubscribed.set(true);
        incomingPublishService.onUnsubscribe(this);
    }

    void scheduleUnsubscribe(@NotNull final Scheduler.Worker worker) {
        schedule(worker, unsubscribeRunnable);
    }

    void runUnsubscribe() {
        scheduled.set(false);
        if (referenced() == 0) {
            onComplete();
        } else {
            incomingPublishService.eventLoop();
        }
    }

    boolean isUnsubscribed() {
        return unsubscribed.get();
    }

    public int referenced() {
        return referenced.get();
    }

    void reference() {
        referenced.getAndIncrement();
    }

    int dereference() {
        return referenced.decrementAndGet();
    }

    void setBlocking() {
        blocking.set(true);
    }

    boolean isBlocking() {
        return blocking.get();
    }

    @NotNull
    public ImmutableList<MqttTopicFilter> getTopicFilters() {
        return topicFilters;
    }

    private void schedule(@NotNull final Scheduler.Worker worker, @NotNull final Runnable runnable) {
        if (scheduled.compareAndSet(false, true)) {
            if (blocking.get()) {
                incomingPublishService.requestOnBlocking();
            }
            worker.schedule(runnable);
        }
    }

}
