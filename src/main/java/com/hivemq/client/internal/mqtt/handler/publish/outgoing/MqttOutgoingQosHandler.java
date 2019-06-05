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

package com.hivemq.client.internal.mqtt.handler.publish.outgoing;

import com.hivemq.client.internal.annotations.CallByThread;
import com.hivemq.client.internal.logging.InternalLogger;
import com.hivemq.client.internal.logging.InternalLoggerFactory;
import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.advanced.interceptor.MqttClientInterceptors;
import com.hivemq.client.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client.internal.mqtt.handler.MqttSessionAwareHandler;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttPubRelWithFlow.MqttQos2CompleteWithFlow;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttPubRelWithFlow.MqttQos2IntermediateWithFlow;
import com.hivemq.client.internal.mqtt.handler.subscribe.MqttSubscriptionHandler;
import com.hivemq.client.internal.mqtt.ioc.ClientScope;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishResult;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishResult.MqttQos1Result;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishResult.MqttQos2CompleteResult;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishResult.MqttQos2IntermediateResult;
import com.hivemq.client.internal.mqtt.message.publish.MqttPublishResult.MqttQos2Result;
import com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish;
import com.hivemq.client.internal.mqtt.message.publish.puback.MqttPubAck;
import com.hivemq.client.internal.mqtt.message.publish.pubcomp.MqttPubComp;
import com.hivemq.client.internal.mqtt.message.publish.pubrec.MqttPubRec;
import com.hivemq.client.internal.mqtt.message.publish.pubrel.MqttPubRel;
import com.hivemq.client.internal.mqtt.message.publish.pubrel.MqttPubRelBuilder;
import com.hivemq.client.internal.util.Ranges;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.internal.util.collections.IntMap;
import com.hivemq.client.internal.util.netty.ContextFuture;
import com.hivemq.client.internal.util.netty.DefaultContextPromise;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.exceptions.ConnectionClosedException;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1.Mqtt5OutgoingQos1Interceptor;
import com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2.Mqtt5OutgoingQos2Interceptor;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5PubRecException;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import org.jctools.queues.SpscUnboundedArrayQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Subscription;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hivemq.client.internal.mqtt.message.publish.MqttStatefulPublish.NO_PACKET_IDENTIFIER_QOS_0;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttOutgoingQosHandler extends MqttSessionAwareHandler
        implements FlowableSubscriber<MqttPublishWithFlow>, Runnable, ContextFuture.Listener<MqttPublishWithFlow> {

    public static final @NotNull String NAME = "qos.outgoing";
    private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(MqttOutgoingQosHandler.class);
    private static final int MAX_CONCURRENT_PUBLISH_FLOWABLES = 64; // TODO configurable
    private static final boolean QOS_2_COMPLETE_RESULT = false; // TODO configurable

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttPublishFlowables publishFlowables;

    private final @NotNull SpscUnboundedArrayQueue<MqttPublishWithFlow> queue = new SpscUnboundedArrayQueue<>(32);
    private final @NotNull AtomicInteger queuedCounter = new AtomicInteger();

    private int sendMaximum;
    private @Nullable Ranges packetIdentifiers;
    private @Nullable IntMap<MqttPubOrRelWithFlow> pending;
    private @Nullable MqttPubOrRelWithFlow firstPending, lastPending, resendPending;
    private @Nullable MqttPublishWithFlow currentPending;
    private @Nullable MqttTopicAliasMapping topicAliasMapping;
    private int shrinkIds;
    private int shrinkRequests;

    private @Nullable Subscription subscription;

    @Inject
    MqttOutgoingQosHandler(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttPublishFlowables publishFlowables) {

        this.clientConfig = clientConfig;
        this.publishFlowables = publishFlowables;
    }

    @Override
    public void onSessionStartOrResume(
            final @NotNull MqttClientConnectionConfig connectionConfig, final @NotNull EventLoop eventLoop) {

        super.onSessionStartOrResume(connectionConfig, eventLoop);

        final int oldSendMaximum = sendMaximum;
        final int newSendMaximum = Math.min(
                connectionConfig.getSendMaximum(),
                UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MqttSubscriptionHandler.MAX_SUB_PENDING);
        sendMaximum = newSendMaximum;
        if (oldSendMaximum == 0) {
            publishFlowables.flatMap(
                    f -> f, true, MAX_CONCURRENT_PUBLISH_FLOWABLES, Math.min(newSendMaximum, Flowable.bufferSize()))
                    .subscribe(this);
            assert subscription != null;
            packetIdentifiers = new Ranges(1, newSendMaximum);
            pending = IntMap.range(1, newSendMaximum);
            subscription.request(newSendMaximum);
        } else {
            assert packetIdentifiers != null;
            assert pending != null;
            assert subscription != null;
            resize();
            final int newRequests = newSendMaximum - oldSendMaximum - shrinkRequests;
            if (newRequests > 0) {
                shrinkRequests = 0;
                subscription.request(newRequests);
            } else {
                shrinkRequests = -newRequests;
            }
        }
        topicAliasMapping = connectionConfig.getSendTopicAliasMapping();

        if ((firstPending != null) || queuedCounter.get() > 0) {
            resendPending = firstPending;
            eventLoop.execute(this);
        }
    }

    private void resize() {
        assert packetIdentifiers != null;
        assert pending != null;

        shrinkIds = packetIdentifiers.resize(sendMaximum);
        if (shrinkIds == 0) {
            pending = IntMap.resize(pending, sendMaximum);
        }
    }

    @Override
    public void onSubscribe(final @NotNull Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public void onNext(final @NotNull MqttPublishWithFlow publishWithFlow) {
        queue.offer(publishWithFlow);
        if (queuedCounter.getAndIncrement() == 0) {
            publishWithFlow.getAckFlow().getEventLoop().execute(this);
        }
    }

    @Override
    public void onComplete() {
        LOGGER.error("MqttPublishFlowables is global and must never complete. This must not happen and is a bug.");
    }

    @Override
    public void onError(final @NotNull Throwable t) {
        LOGGER.error("MqttPublishFlowables is global and must never error. This must not happen and is a bug.", t);
    }

    @CallByThread("Netty EventLoop")
    void request(final long n) {
        assert subscription != null;

        final int shrinkRequests = this.shrinkRequests;
        if (this.shrinkRequests == 0) {
            subscription.request(n);
        } else if (n > shrinkRequests) {
            this.shrinkRequests = 0;
            subscription.request(n - shrinkRequests);
        } else {
            this.shrinkRequests -= n;
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void run() {
        if (!hasSession) {
            clearQueued(MqttClientStateExceptions.notConnected());
            return;
        }
        final ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            return;
        }
        assert pending != null;
        int resent = 0;
        while (resendPending != null) {
            if (resent == sendMaximum) {
                ctx.flush();
                return;
            }
            if (resendPending instanceof MqttPublishWithFlow) {
                final MqttPublishWithFlow publishWithFlow = (MqttPublishWithFlow) this.resendPending;
                final MqttStatefulPublish publish = publishWithFlow.getPublish()
                        .createStateful(publishWithFlow.packetIdentifier, true, topicAliasMapping);
                writeQos1Or2Publish(ctx, publish, publishWithFlow);
            } else {
                final MqttPubRelWithFlow pubRelWithFlow = (MqttPubRelWithFlow) this.resendPending;
                writePubRel(ctx, pubRelWithFlow.getPubRel());
            }
            resent++;
            resendPending = resendPending.next;
        }
        final int working = Math.min(Math.min(queuedCounter.get(), 64), sendMaximum - pending.size());
        for (int i = 0; i < working; i++) {
            final MqttPublishWithFlow publishWithFlow = queue.poll();
            assert publishWithFlow != null; // ensured by queuedCounter
            writePublish(ctx, publishWithFlow);
        }
        ctx.flush();
        if (queuedCounter.addAndGet(-working) > 0) {
            ctx.channel().eventLoop().execute(this);
        }
    }

    private void writePublish(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPublishWithFlow publishWithFlow) {

        if (publishWithFlow.getPublish().getQos() == MqttQos.AT_MOST_ONCE) {
            writeQos0Publish(ctx, publishWithFlow);
        } else {
            writeQos1Or2Publish(ctx, publishWithFlow);
        }
    }

    private void writeQos0Publish(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPublishWithFlow publishWithFlow) {

        ctx.write(
                publishWithFlow.getPublish().createStateful(NO_PACKET_IDENTIFIER_QOS_0, false, topicAliasMapping),
                new DefaultContextPromise<>(ctx.channel(), publishWithFlow)).addListener(this);
    }

    @Override
    public void operationComplete(final @NotNull ContextFuture<? extends MqttPublishWithFlow> future) {
        final MqttPublishWithFlow publishWithFlow = future.getContext();
        final MqttPublish publish = publishWithFlow.getPublish();
        final MqttAckFlow ackFlow = publishWithFlow.getAckFlow();
        final Throwable cause = future.cause();
        if (!(cause instanceof IOException)) {
            ackFlow.onNext(new MqttPublishResult(publish, cause));
        } else {
            ackFlow.onNext(new MqttPublishResult(publish, new ConnectionClosedException(cause)));
            future.channel().pipeline().fireExceptionCaught(cause);
        }
    }

    private void writeQos1Or2Publish(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPublishWithFlow publishWithFlow) {

        assert packetIdentifiers != null;
        assert pending != null;

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier < 0) {
            LOGGER.error("No Packet Identifier available for QoS 1 or 2 PUBLISH. This must not happen and is a bug.");
            return;
        }
        publishWithFlow.packetIdentifier = packetIdentifier;
        pending.put(packetIdentifier, publishWithFlow);
        final MqttPubOrRelWithFlow lastPending = this.lastPending;
        if (lastPending == null) {
            firstPending = this.lastPending = publishWithFlow;
        } else {
            lastPending.next = publishWithFlow;
            publishWithFlow.prev = lastPending;
            this.lastPending = publishWithFlow;
        }

        writeQos1Or2Publish(
                ctx,
                publishWithFlow.getPublish().createStateful(packetIdentifier, false, topicAliasMapping),
                publishWithFlow);
    }

    private void writeQos1Or2Publish(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttStatefulPublish publish,
            final @NotNull MqttPublishWithFlow publishWithFlow) {

        currentPending = publishWithFlow;
        ctx.write(publish, ctx.voidPromise());
        currentPending = null;
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof MqttPubAck) {
            readPubAck(ctx, (MqttPubAck) msg);
        } else if (msg instanceof MqttPubRec) {
            readPubRec(ctx, (MqttPubRec) msg);
        } else if (msg instanceof MqttPubComp) {
            readPubComp(ctx, (MqttPubComp) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readPubAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubAck pubAck) {
        assert pending != null;

        final int packetIdentifier = pubAck.getPacketIdentifier();
        final MqttPubOrRelWithFlow removed = pending.remove(packetIdentifier);

        if (removed == null) {
            error(ctx, "PUBACK contained unknown packet identifier");
            return;
        }
        if (!(removed instanceof MqttPublishWithFlow)) { // MqttPubRelWithFlow
            pending.put(packetIdentifier, removed); // revert
            error(ctx, "PUBACK must not be received for a PUBREL");
            return;
        }
        final MqttPublishWithFlow publishWithFlow = (MqttPublishWithFlow) removed;
        final MqttPublish publish = publishWithFlow.getPublish();
        if (publish.getQos() != MqttQos.AT_LEAST_ONCE) { // EXACTLY_ONCE
            pending.put(packetIdentifier, removed); // revert
            error(ctx, "PUBACK must not be received for a QoS 2 PUBLISH");
            return;
        }

        completePending(ctx, publishWithFlow);

        onPubAck(publish, pubAck);

        final Throwable t = (pubAck.getReasonCode().isError()) ?
                new Mqtt5PubAckException(pubAck, "PUBACK contained an Error Code") : null;
        publishWithFlow.getAckFlow().onNext(new MqttQos1Result(publish, t, pubAck));
    }

    private void readPubRec(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubRec pubRec) {
        assert pending != null;

        final int packetIdentifier = pubRec.getPacketIdentifier();
        final MqttPubOrRelWithFlow got = pending.get(packetIdentifier);

        if (got == null) {
            error(ctx, "PUBREC contained unknown packet identifier");
            return;
        }
        if (!(got instanceof MqttPublishWithFlow)) { // MqttPubRelWithFlow
            error(ctx, "PUBREC must not be received when the PUBREL has already been sent");
            return;
        }
        final MqttPublishWithFlow publishWithFlow = (MqttPublishWithFlow) got;
        final MqttPublish publish = publishWithFlow.getPublish();
        if (publish.getQos() != MqttQos.EXACTLY_ONCE) { // AT_LEAST_ONCE
            error(ctx, "PUBREC must not be received for a QoS 1 PUBLISH");
            return;
        }
        final MqttAckFlow ackFlow = publishWithFlow.getAckFlow();

        if (pubRec.getReasonCode().isError()) {
            pending.remove(packetIdentifier);
            completePending(ctx, publishWithFlow);

            onPubRecError(publish, pubRec);

            final Throwable t = new Mqtt5PubRecException(pubRec, "PUBREC contained an Error Code");
            ackFlow.onNext(new MqttQos2Result(publish, t, pubRec));

        } else {
            final MqttPubRel pubRel = buildPubRel(publish, pubRec);

            if (QOS_2_COMPLETE_RESULT) {
                replacePending(publishWithFlow, new MqttQos2CompleteWithFlow(publish, pubRec, pubRel, ackFlow));
            } else {
                final MqttQos2IntermediateWithFlow pubRelWithFlow = new MqttQos2IntermediateWithFlow(pubRel, ackFlow);
                replacePending(publishWithFlow, pubRelWithFlow);

                ackFlow.onNext(new MqttQos2IntermediateResult(publish, pubRec, pubRelWithFlow));
            }

            writePubRel(ctx, pubRel);
        }
    }

    private void writePubRel(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubRel pubRel) {
        ctx.writeAndFlush(pubRel, ctx.voidPromise());
    }

    private void replacePending(
            final @NotNull MqttPublishWithFlow publishWithFlow, final @NotNull MqttPubRelWithFlow pubRelWithFlow) {

        assert pending != null;

        final int packetIdentifier = publishWithFlow.packetIdentifier;
        pubRelWithFlow.packetIdentifier = packetIdentifier;
        pending.put(packetIdentifier, pubRelWithFlow);

        final MqttPubOrRelWithFlow prev = publishWithFlow.prev;
        final MqttPubOrRelWithFlow next = publishWithFlow.next;
        pubRelWithFlow.prev = prev;
        pubRelWithFlow.next = next;
        if (prev == null) {
            firstPending = pubRelWithFlow;
        } else {
            prev.next = pubRelWithFlow;
        }
        if (next == null) {
            lastPending = pubRelWithFlow;
        } else {
            next.prev = pubRelWithFlow;
        }
    }

    private void readPubComp(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubComp pubComp) {
        assert pending != null;

        final int packetIdentifier = pubComp.getPacketIdentifier();
        final MqttPubOrRelWithFlow removed = pending.remove(packetIdentifier);

        if (removed == null) {
            error(ctx, "PUBCOMP contained unknown packet identifier");
            return;
        }
        if (!(removed instanceof MqttPubRelWithFlow)) { // MqttPublishWithFlow
            pending.put(packetIdentifier, removed); // revert
            final MqttPublishWithFlow publishWithFlow = (MqttPublishWithFlow) removed;
            if (publishWithFlow.getPublish().getQos() == MqttQos.AT_LEAST_ONCE) {
                error(ctx, "PUBCOMP must not be received for a QoS 1 PUBLISH");
            } else { // EXACTLY_ONCE
                error(ctx, "PUBCOMP must not be received when the PUBREL has not been sent yet");
            }
            return;
        }
        final MqttPubRelWithFlow pubRelWithFlow = (MqttPubRelWithFlow) removed;
        final MqttPubRel pubRel = pubRelWithFlow.getPubRel();
        final MqttAckFlow ackFlow = pubRelWithFlow.getAckFlow();

        completePending(ctx, pubRelWithFlow);

        onPubComp(pubRel, pubComp);

        if (QOS_2_COMPLETE_RESULT) {
            final MqttQos2CompleteWithFlow complete = (MqttQos2CompleteWithFlow) pubRelWithFlow;
            ackFlow.onNext(new MqttQos2CompleteResult(complete.getPublish(), complete.getPubRec(), pubRel, pubComp));
        } else {
            final MqttQos2IntermediateWithFlow intermediate = (MqttQos2IntermediateWithFlow) pubRelWithFlow;
            if (intermediate.getAsBoolean()) {
                ackFlow.acknowledged(1);
            }
        }
    }

    private void completePending(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubOrRelWithFlow oldPending) {

        assert packetIdentifiers != null;

        final MqttPubOrRelWithFlow prev = oldPending.prev;
        final MqttPubOrRelWithFlow next = oldPending.next;
        if (prev == null) {
            firstPending = next;
        } else {
            prev.next = next;
        }
        if (next == null) {
            lastPending = prev;
        } else {
            next.prev = prev;
        }

        final int packetIdentifier = oldPending.packetIdentifier;
        packetIdentifiers.returnId(packetIdentifier);
        if ((packetIdentifier > sendMaximum) && (--shrinkIds == 0)) {
            resize();
        }

        if (resendPending != null) {
            ctx.channel().eventLoop().execute(this);
        }
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        if (!(cause instanceof IOException) && (currentPending != null)) {
            assert pending != null;
            pending.remove(currentPending.packetIdentifier);
            currentPending.getAckFlow().onNext(new MqttPublishResult(currentPending.getPublish(), cause));
            completePending(ctx, currentPending);
            currentPending = null;
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }

    @Override
    public void onSessionEnd(final @NotNull Throwable cause) {
        super.onSessionEnd(cause);

        assert packetIdentifiers != null;
        assert pending != null;

        MqttPubOrRelWithFlow current = firstPending;
        while (current != null) {
            packetIdentifiers.returnId(current.packetIdentifier);
            if (current instanceof MqttPublishWithFlow) {
                final MqttPublishWithFlow publishWithFlow = (MqttPublishWithFlow) current;
                current.getAckFlow().onNext(new MqttPublishResult(publishWithFlow.getPublish(), cause));
            } else if (QOS_2_COMPLETE_RESULT) {
                final MqttQos2CompleteWithFlow complete = (MqttQos2CompleteWithFlow) current;
                current.getAckFlow().onNext(new MqttQos2Result(complete.getPublish(), cause, complete.getPubRec()));
                // TODO actually not an error, default PubComp?
            } else {
                final MqttQos2IntermediateWithFlow intermediate = (MqttQos2IntermediateWithFlow) current;
                if (intermediate.getAsBoolean()) {
                    intermediate.getAckFlow().acknowledged(1);
                }
            }
            current = current.next;
        }
        pending.clear();
        firstPending = lastPending = resendPending = null;

        clearQueued(cause);
    }

    private void clearQueued(final @NotNull Throwable cause) {
        int polled = 0;
        while (true) {
            final MqttPublishWithFlow publishWithFlow = queue.poll();
            if (publishWithFlow == null) {
                if (queuedCounter.addAndGet(-polled) == 0) {
                    break;
                } else {
                    polled = 0;
                    continue;
                }
            }
            publishWithFlow.getAckFlow().onNext(new MqttPublishResult(publishWithFlow.getPublish(), cause));
            polled++;
        }
    }

    private static void error(final @NotNull ChannelHandlerContext ctx, final @NotNull String reasonString) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, reasonString);
    }

    private void onPubAck(final @NotNull MqttPublish publish, final @NotNull MqttPubAck pubAck) {
        final MqttClientInterceptors interceptors = clientConfig.getAdvancedConfig().getInterceptors();
        if (interceptors != null) {
            final Mqtt5OutgoingQos1Interceptor interceptor = interceptors.getOutgoingQos1Interceptor();
            if (interceptor != null) {
                interceptor.onPubAck(clientConfig, publish, pubAck);
            }
        }
    }

    private void onPubRecError(final @NotNull MqttPublish publish, final @NotNull MqttPubRec pubRec) {
        final MqttClientInterceptors interceptors = clientConfig.getAdvancedConfig().getInterceptors();
        if (interceptors != null) {
            final Mqtt5OutgoingQos2Interceptor interceptor = interceptors.getOutgoingQos2Interceptor();
            if (interceptor != null) {
                interceptor.onPubRecError(clientConfig, publish, pubRec);
            }
        }
    }

    private @NotNull MqttPubRel buildPubRel(final @NotNull MqttPublish publish, final @NotNull MqttPubRec pubRec) {
        final MqttPubRelBuilder pubRelBuilder = new MqttPubRelBuilder(pubRec);
        final MqttClientInterceptors interceptors = clientConfig.getAdvancedConfig().getInterceptors();
        if (interceptors != null) {
            final Mqtt5OutgoingQos2Interceptor interceptor = interceptors.getOutgoingQos2Interceptor();
            if (interceptor != null) {
                interceptor.onPubRec(clientConfig, publish, pubRec, pubRelBuilder);
            }
        }
        return pubRelBuilder.build();
    }

    private void onPubComp(final @NotNull MqttPubRel pubRel, final @NotNull MqttPubComp pubComp) {
        final MqttClientInterceptors interceptors = clientConfig.getAdvancedConfig().getInterceptors();
        if (interceptors != null) {
            final Mqtt5OutgoingQos2Interceptor interceptor = interceptors.getOutgoingQos2Interceptor();
            if (interceptor != null) {
                interceptor.onPubComp(clientConfig, pubRel, pubComp);
            }
        }
    }

    @NotNull MqttPublishFlowables getPublishFlowables() {
        return publishFlowables;
    }
}
