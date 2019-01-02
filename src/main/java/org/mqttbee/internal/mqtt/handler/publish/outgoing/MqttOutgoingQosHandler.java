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

package org.mqttbee.internal.mqtt.handler.publish.outgoing;

import io.netty.channel.ChannelHandlerContext;
import io.reactivex.FlowableSubscriber;
import org.jctools.queues.SpscUnboundedArrayQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.internal.mqtt.MqttClientConfig;
import org.mqttbee.internal.mqtt.MqttClientConnectionConfig;
import org.mqttbee.internal.mqtt.MqttServerConnectionConfig;
import org.mqttbee.internal.mqtt.advanced.MqttAdvancedClientConfig;
import org.mqttbee.internal.mqtt.exceptions.MqttClientStateExceptions;
import org.mqttbee.internal.mqtt.handler.MqttSessionAwareHandler;
import org.mqttbee.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.internal.mqtt.handler.publish.outgoing.MqttPubRelWithFlow.MqttQos2CompleteWithFlow;
import org.mqttbee.internal.mqtt.handler.publish.outgoing.MqttPubRelWithFlow.MqttQos2IntermediateWithFlow;
import org.mqttbee.internal.mqtt.handler.subscribe.MqttSubscriptionHandler;
import org.mqttbee.internal.mqtt.ioc.ClientScope;
import org.mqttbee.internal.mqtt.message.publish.MqttPublish;
import org.mqttbee.internal.mqtt.message.publish.MqttPublishResult;
import org.mqttbee.internal.mqtt.message.publish.MqttPublishResult.MqttQos1Result;
import org.mqttbee.internal.mqtt.message.publish.MqttPublishResult.MqttQos2CompleteResult;
import org.mqttbee.internal.mqtt.message.publish.MqttPublishResult.MqttQos2IntermediateResult;
import org.mqttbee.internal.mqtt.message.publish.MqttPublishResult.MqttQos2Result;
import org.mqttbee.internal.mqtt.message.publish.MqttTopicAliasMapping;
import org.mqttbee.internal.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.internal.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.internal.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.internal.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.internal.mqtt.message.publish.pubrel.MqttPubRelBuilder;
import org.mqttbee.mqtt.datatypes.MqttQos;
import org.mqttbee.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQos1Interceptor;
import org.mqttbee.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQos2Interceptor;
import org.mqttbee.mqtt.mqtt5.exceptions.Mqtt5PubAckException;
import org.mqttbee.mqtt.mqtt5.exceptions.Mqtt5PubRecException;
import org.mqttbee.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.util.Ranges;
import org.mqttbee.util.UnsignedDataTypes;
import org.mqttbee.util.collections.ChunkedIntArrayQueue;
import org.mqttbee.util.collections.IntMap;
import org.mqttbee.util.netty.ContextFuture;
import org.mqttbee.util.netty.DefaultContextPromise;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mqttbee.internal.mqtt.message.publish.MqttStatefulPublish.NO_PACKET_IDENTIFIER_QOS_0;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttOutgoingQosHandler extends MqttSessionAwareHandler
        implements FlowableSubscriber<MqttPublishWithFlow>, Runnable, ContextFuture.Listener<MqttPublishWithFlow> {

    public static final @NotNull String NAME = "qos.outgoing";
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(MqttOutgoingQosHandler.class);
    private static final int MAX_CONCURRENT_PUBLISH_FLOWABLES = 64; // TODO configurable
    private static final boolean QOS_2_COMPLETE_RESULT = false; // TODO configurable

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttPublishFlowables publishFlowables;

    private final @NotNull SpscUnboundedArrayQueue<MqttPublishWithFlow> queue = new SpscUnboundedArrayQueue<>(32);
    private final @NotNull AtomicInteger queuedCounter = new AtomicInteger();
    private final @NotNull ChunkedIntArrayQueue qos1Or2Queue = new ChunkedIntArrayQueue(32);

    private int sendMaximum;
    private @Nullable Ranges packetIdentifiers;
    private @Nullable IntMap<MqttPubOrRelWithFlow> qos1Or2Map;
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
            final @NotNull MqttClientConnectionConfig clientConnectionConfig,
            final @NotNull MqttServerConnectionConfig serverConnectionConfig) {

        super.onSessionStartOrResume(clientConnectionConfig, serverConnectionConfig);

        final int oldSendMaximum = sendMaximum;
        final int newSendMaximum = Math.min(
                serverConnectionConfig.getReceiveMaximum(),
                UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MqttSubscriptionHandler.MAX_SUB_PENDING);
        sendMaximum = newSendMaximum;
        if (oldSendMaximum == 0) {
            publishFlowables.flatMap(f -> f, true, MAX_CONCURRENT_PUBLISH_FLOWABLES).subscribe(this);
            assert subscription != null;
            packetIdentifiers = new Ranges(1, newSendMaximum);
            qos1Or2Map = IntMap.range(1, newSendMaximum);
            subscription.request(newSendMaximum);
        } else {
            assert packetIdentifiers != null;
            assert qos1Or2Map != null;
            assert subscription != null;
            resize();
            final int newRequests = newSendMaximum - oldSendMaximum - shrinkRequests;
            if (newRequests > 0) {
                subscription.request(newRequests);
                shrinkRequests = 0;
            } else {
                shrinkRequests = -newRequests;
            }
//            resend(); // TODO
        }
        topicAliasMapping = serverConnectionConfig.getTopicAliasMapping();
    }

    private void resize() {
        assert packetIdentifiers != null;
        assert qos1Or2Map != null;

        shrinkIds = packetIdentifiers.resize(sendMaximum);
        if (shrinkIds == 0) {
            qos1Or2Map = IntMap.resize(qos1Or2Map, sendMaximum);
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
            clientConfig.executeInEventLoop(this);
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
    void request(final long amount) {
        assert subscription != null;

        if (shrinkRequests == 0) {
            subscription.request(amount);
        } else {
            shrinkRequests--;
        }
    }

    @CallByThread("Netty EventLoop")
    public void run() {
        if (!hasSession) {
            clearQueued(MqttClientStateExceptions.notConnected());
            return;
        }
        if (ctx == null) {
            return;
        }
        final int working = Math.min(queuedCounter.get(), 64);
        for (int i = 0; i < working; i++) {
            final MqttPublishWithFlow publishWithFlow = queue.poll();
            assert publishWithFlow != null; // ensured by queuedCounter
            writePublish(ctx, publishWithFlow);
        }
        ctx.flush();
        if (queuedCounter.addAndGet(-working) > 0) {
            clientConfig.executeInEventLoop(this);
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
        publishWithFlow.getIncomingAckFlow()
                .onNext(new MqttPublishResult(publishWithFlow.getPublish(), future.cause()));
    }

    private void writeQos1Or2Publish(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPublishWithFlow publishWithFlow) {

        assert packetIdentifiers != null;
        assert qos1Or2Map != null;

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier < 0) {
            LOGGER.error("No Packet Identifier available for QoS 1 or 2 PUBLISH. This must not happen and is a bug.");
            return;
        }
        qos1Or2Map.put(packetIdentifier, publishWithFlow);
        qos1Or2Queue.offer(packetIdentifier);
        ctx.write(
                publishWithFlow.getPublish().createStateful(packetIdentifier, false, topicAliasMapping),
                ctx.voidPromise());
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
        assert qos1Or2Map != null;

        final int packetIdentifier = pubAck.getPacketIdentifier();
        final MqttPubOrRelWithFlow removed = qos1Or2Map.remove(packetIdentifier);

        if (removed == null) {
            error(ctx, "PUBACK contained unknown packet identifier");
            return;
        }
        if (!(removed instanceof MqttPublishWithFlow)) { // MqttPubRelWithFlow
            qos1Or2Map.put(packetIdentifier, removed); // revert
            error(ctx, "PUBACK must not be received for a PUBREL");
            return;
        }
        final MqttPublishWithFlow publishWithFlow = (MqttPublishWithFlow) removed;
        final MqttPublish publish = publishWithFlow.getPublish();
        if (publish.getQos() != MqttQos.AT_LEAST_ONCE) { // EXACTLY_ONCE
            qos1Or2Map.put(packetIdentifier, removed); // revert
            error(ctx, "PUBACK must not be received for a QoS 2 PUBLISH");
            return;
        }

        removed(packetIdentifier);

        onPubAck(publish, pubAck);

        final Throwable t = (pubAck.getReasonCode().isError()) ?
                new Mqtt5PubAckException(pubAck, "PUBACK contained an Error Code") : null;
        publishWithFlow.getIncomingAckFlow().onNext(new MqttQos1Result(publish, t, pubAck));
    }

    private void readPubRec(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubRec pubRec) {
        assert qos1Or2Map != null;

        final int packetIdentifier = pubRec.getPacketIdentifier();
        final MqttPubOrRelWithFlow got = qos1Or2Map.get(packetIdentifier);

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
        final MqttIncomingAckFlow ackFlow = publishWithFlow.getIncomingAckFlow();

        if (pubRec.getReasonCode().isError()) {
            qos1Or2Map.remove(packetIdentifier);
            removed(packetIdentifier);

            onPubRecError(publish, pubRec);

            final Throwable t = new Mqtt5PubRecException(pubRec, "PUBREC contained an Error Code");
            ackFlow.onNext(new MqttQos2Result(publish, t, pubRec));

        } else {
            final MqttPubRel pubRel = buildPubRel(publish, pubRec);

            if (QOS_2_COMPLETE_RESULT) {
                qos1Or2Map.put(packetIdentifier, new MqttQos2CompleteWithFlow(publish, pubRec, pubRel, ackFlow));
            } else {
                final MqttQos2IntermediateWithFlow pubRelWithFlow = new MqttQos2IntermediateWithFlow(pubRel, ackFlow);
                qos1Or2Map.put(packetIdentifier, pubRelWithFlow);

                ackFlow.onNext(new MqttQos2IntermediateResult(publish, pubRec, pubRelWithFlow));
            }

            ctx.writeAndFlush(pubRel, ctx.voidPromise());
        }
    }

    private void readPubComp(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubComp pubComp) {
        assert qos1Or2Map != null;

        final int packetIdentifier = pubComp.getPacketIdentifier();
        final MqttPubOrRelWithFlow removed = qos1Or2Map.remove(packetIdentifier);

        if (removed == null) {
            error(ctx, "PUBCOMP contained unknown packet identifier");
            return;
        }
        if (!(removed instanceof MqttPubRelWithFlow)) { // MqttPublishWithFlow
            qos1Or2Map.put(packetIdentifier, removed); // revert
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
        final MqttIncomingAckFlow ackFlow = pubRelWithFlow.getIncomingAckFlow();

        removed(packetIdentifier);

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

    private void removed(final int packetIdentifier) {
        assert packetIdentifiers != null;

        qos1Or2Queue.removeFirst(packetIdentifier);
        packetIdentifiers.returnId(packetIdentifier);
        if ((packetIdentifier > sendMaximum) && (--shrinkIds == 0)) {
            resize();
        }
    }

    @Override
    public void onSessionEnd(final @NotNull Throwable cause) {
        super.onSessionEnd(cause);

        int qos1Or2PacketId;
        while ((qos1Or2PacketId = qos1Or2Queue.poll(-1)) != -1) {
            assert packetIdentifiers != null;
            assert qos1Or2Map != null;

            packetIdentifiers.returnId(qos1Or2PacketId);
            final MqttPubOrRelWithFlow removed = qos1Or2Map.remove(qos1Or2PacketId);
            assert removed != null;
            removed.getIncomingAckFlow().onError(cause);
        }

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
            publishWithFlow.getIncomingAckFlow().onError(cause);
            polled++;
        }
    }

    private static void error(final @NotNull ChannelHandlerContext ctx, final @NotNull String reasonString) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, reasonString);
    }

    private void onPubAck(final @NotNull MqttPublish publish, final @NotNull MqttPubAck pubAck) {
        final MqttAdvancedClientConfig advanced = clientConfig.getRawAdvancedClientConfig();
        if (advanced != null) {
            final Mqtt5OutgoingQos1Interceptor interceptor = advanced.getOutgoingQos1Interceptor();
            if (interceptor != null) {
                interceptor.onPubAck(clientConfig, publish, pubAck);
            }
        }
    }

    private void onPubRecError(final @NotNull MqttPublish publish, final @NotNull MqttPubRec pubRec) {
        final MqttAdvancedClientConfig advanced = clientConfig.getRawAdvancedClientConfig();
        if (advanced != null) {
            final Mqtt5OutgoingQos2Interceptor interceptor = advanced.getOutgoingQos2Interceptor();
            if (interceptor != null) {
                interceptor.onPubRecError(clientConfig, publish, pubRec);
            }
        }
    }

    private @NotNull MqttPubRel buildPubRel(final @NotNull MqttPublish publish, final @NotNull MqttPubRec pubRec) {
        final MqttPubRelBuilder pubRelBuilder = new MqttPubRelBuilder(pubRec);
        final MqttAdvancedClientConfig advanced = clientConfig.getRawAdvancedClientConfig();
        if (advanced != null) {
            final Mqtt5OutgoingQos2Interceptor interceptor = advanced.getOutgoingQos2Interceptor();
            if (interceptor != null) {
                interceptor.onPubRec(clientConfig, publish, pubRec, pubRelBuilder);
            }
        }
        return pubRelBuilder.build();
    }

    private void onPubComp(final @NotNull MqttPubRel pubRel, final @NotNull MqttPubComp pubComp) {
        final MqttAdvancedClientConfig advanced = clientConfig.getRawAdvancedClientConfig();
        if (advanced != null) {
            final Mqtt5OutgoingQos2Interceptor interceptor = advanced.getOutgoingQos2Interceptor();
            if (interceptor != null) {
                interceptor.onPubComp(clientConfig, pubRel, pubComp);
            }
        }
    }

    @NotNull MqttClientConfig getClientConfig() {
        return clientConfig;
    }

    @NotNull MqttPublishFlowables getPublishFlowables() {
        return publishFlowables;
    }
}
