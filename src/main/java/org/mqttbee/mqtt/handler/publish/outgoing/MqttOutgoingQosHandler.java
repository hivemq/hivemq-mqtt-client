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

package org.mqttbee.mqtt.handler.publish.outgoing;

import io.netty.channel.*;
import io.reactivex.FlowableSubscriber;
import org.jctools.queues.SpscChunkedArrayQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQos1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQos2ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.handler.subscribe.MqttSubscriptionHandler;
import org.mqttbee.mqtt.ioc.ClientScope;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishResult;
import org.mqttbee.mqtt.message.publish.MqttPublishResult.MqttQos1Result;
import org.mqttbee.mqtt.message.publish.MqttPublishResult.MqttQos2Result;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.message.publish.MqttTopicAliasMapping;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelBuilder;
import org.mqttbee.util.Ranges;
import org.mqttbee.util.UnsignedDataTypes;
import org.mqttbee.util.collections.ChunkedArrayQueue;
import org.mqttbee.util.collections.ChunkedIntArrayQueue;
import org.mqttbee.util.collections.IntMap;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mqttbee.mqtt.message.publish.MqttStatefulPublish.*;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttOutgoingQosHandler extends ChannelInboundHandlerAdapter
        implements FlowableSubscriber<MqttPublishWithFlow>, Runnable, ChannelFutureListener {

    public static final @NotNull String NAME = "qos.outgoing";
    private static final int MAX_CONCURRENT_PUBLISH_FLOWABLES = 64; // TODO configurable
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(MqttOutgoingQosHandler.class);

    private final @NotNull MqttClientData clientData;
    private final @NotNull MqttPublishFlowables publishFlowables;

    private final @NotNull SpscChunkedArrayQueue<MqttPublishWithFlow> publishQueue =
            new SpscChunkedArrayQueue<>(32, 1 << 30);
    private final @NotNull AtomicInteger queuedCounter = new AtomicInteger();
    private final @NotNull ChunkedArrayQueue<MqttPublishWithFlow> qos0PublishQueue = new ChunkedArrayQueue<>(32);
    private final @NotNull ChunkedIntArrayQueue qos1Or2PublishQueue = new ChunkedIntArrayQueue(32);

    private @Nullable ChannelHandlerContext ctx;
    private int sendMaximum;
    private @Nullable Ranges packetIdentifiers;
    private @Nullable IntMap<Object> qos1Or2Map; // contains MqttPublishWithFlow, MqttPubRelWithFlow
    private @Nullable MqttTopicAliasMapping topicAliasMapping;
    private int shrinkIds;
    private int shrinkRequests;

    private @Nullable Subscription subscription;

    @Inject
    MqttOutgoingQosHandler(
            final @NotNull MqttClientData clientData, final @NotNull MqttPublishFlowables publishFlowables) {

        this.clientData = clientData;
        this.publishFlowables = publishFlowables;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        this.ctx = ctx;

        final MqttServerConnectionData serverConnectionData = clientData.getRawServerConnectionData();
        assert serverConnectionData != null;

        final int oldSendMaximum = sendMaximum;
        final int newSendMaximum = Math.min(
                serverConnectionData.getReceiveMaximum(),
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
        topicAliasMapping = serverConnectionData.getTopicAliasMapping();
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
        publishQueue.offer(publishWithFlow);
        if (queuedCounter.getAndIncrement() == 0) {
            clientData.getEventLoop().execute(this);
        }
    }

    @Override
    public void onComplete() {
        LOGGER.error("MqttPublishFlowables is global and must never complete. This must not happen and is a bug.");
    }

    @Override
    public void onError(final @NotNull Throwable t) {
        LOGGER.error("MqttPublishFlowables is global and must never error. This must not happen and is a bug.");
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
        if (ctx == null) {
            return;
        }
        final int working = Math.min(queuedCounter.get(), 64);
        for (int i = 0; i < working; i++) {
            final MqttPublishWithFlow publishWithFlow = publishQueue.poll();
            assert publishWithFlow != null; // ensured by wip
            handlePublish(publishWithFlow);
        }
        ctx.flush();
        if (queuedCounter.addAndGet(-working) > 0) {
            clientData.getEventLoop().execute(this);
        }
    }

    private void handlePublish(final @NotNull MqttPublishWithFlow publishWithFlow) {
        if (publishWithFlow.getPublish().getQos() == MqttQos.AT_MOST_ONCE) {
            handleQos0Publish(publishWithFlow);
        } else {
            handleQos1Or2Publish(publishWithFlow);
        }
    }

    private void handleQos0Publish(final @NotNull MqttPublishWithFlow publishWithFlow) {
        assert ctx != null;

        qos0PublishQueue.offer(publishWithFlow);
        ctx.write(addState(publishWithFlow.getPublish(), NO_PACKET_IDENTIFIER_QOS_0, false)).addListener(this);
    }

    @Override
    public void operationComplete(final @NotNull ChannelFuture future) {
        final MqttPublishWithFlow publishWithFlow = qos0PublishQueue.poll();
        assert publishWithFlow != null; // ensured by handleQos0Publish
        publishWithFlow.getIncomingAckFlow()
                .onNext(new MqttPublishResult(publishWithFlow.getPublish(), future.cause()));
    }

    private void handleQos1Or2Publish(final @NotNull MqttPublishWithFlow publishWithFlow) {
        assert ctx != null;
        assert packetIdentifiers != null;
        assert qos1Or2Map != null;

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier < 0) {
            LOGGER.error("No Packet Identifier available for QoS 1 or 2 PUBLISH. This must not happen and is a bug.");
            return;
        }
        qos1Or2Map.put(packetIdentifier, publishWithFlow);
        qos1Or2PublishQueue.offer(packetIdentifier);
        ctx.write(addState(publishWithFlow.getPublish(), packetIdentifier, false), ctx.voidPromise());
    }

    private @NotNull MqttStatefulPublish addState(
            final @NotNull MqttPublish publish, final int packetIdentifier, final boolean isDup) {

        int topicAlias;
        final boolean isNewTopicAlias;
        if (topicAliasMapping == null) {
            topicAlias = DEFAULT_NO_TOPIC_ALIAS;
            isNewTopicAlias = false;
        } else {
            final MqttTopicImpl topic = publish.getTopic();
            topicAlias = topicAliasMapping.get(topic);
            if (topicAlias != DEFAULT_NO_TOPIC_ALIAS) {
                isNewTopicAlias = false;
            } else {
                topicAlias = topicAliasMapping.set(topic, publish.usesTopicAlias());
                isNewTopicAlias = topicAlias != DEFAULT_NO_TOPIC_ALIAS;
            }
        }
        return publish.createStateful(
                packetIdentifier, isDup, topicAlias, isNewTopicAlias, DEFAULT_NO_SUBSCRIPTION_IDENTIFIERS);
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
        final Object removed = qos1Or2Map.remove(packetIdentifier);

        if (removed == null) {
            disconnectUnknown(ctx, "PUBACK");
            return;
        }
        if (!(removed instanceof MqttPublishWithFlow)) { // MqttPubRelWithFlow
            qos1Or2Map.put(packetIdentifier, removed); // revert
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "PUBACK must not be received for a PUBREL");
            return;
        }
        final MqttPublishWithFlow publishWithFlow = (MqttPublishWithFlow) removed;
        final MqttPublish publish = publishWithFlow.getPublish();
        if (publish.getQos() != MqttQos.AT_LEAST_ONCE) { // EXACTLY_ONCE
            qos1Or2Map.put(packetIdentifier, removed); // revert
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "PUBACK must not be received for a QoS 2 PUBLISH");
            return;
        }

        removed(packetIdentifier);

        onPubAck(publish, pubAck);

        final Throwable t = (pubAck.getReasonCode().isError()) ?
                new Mqtt5MessageException(pubAck, "PUBACK contained an Error Code") : null;
        publishWithFlow.getIncomingAckFlow().onNext(new MqttQos1Result(publish, t, pubAck));
    }

    private void readPubRec(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubRec pubRec) {
        assert qos1Or2Map != null;
        final int packetIdentifier = pubRec.getPacketIdentifier();
        final Object got = qos1Or2Map.get(packetIdentifier);

        if (got == null) {
            disconnectUnknown(ctx, "PUBREC");
            return;
        }
        if (!(got instanceof MqttPublishWithFlow)) { // MqttPubRelWithFlow
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "PUBREC must not be received when the PUBREL has already been sent");
            return;
        }
        final MqttPublishWithFlow publishWithFlow = (MqttPublishWithFlow) got;
        final MqttPublish publish = publishWithFlow.getPublish();
        if (publish.getQos() != MqttQos.EXACTLY_ONCE) { // AT_LEAST_ONCE
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "PUBREC must not be received for a QoS 1 PUBLISH");
            return;
        }
        final MqttIncomingAckFlow incomingAckFlow = publishWithFlow.getIncomingAckFlow();

        if (pubRec.getReasonCode().isError()) {
            qos1Or2Map.remove(packetIdentifier);
            removed(packetIdentifier);

            onPubRecError(publish, pubRec);

            final Throwable t = new Mqtt5MessageException(pubRec, "PUBREC contained an Error Code");
            incomingAckFlow.onNext(new MqttQos2Result(publish, t, pubRec, null));

        } else {
            final MqttPubRel pubRel = buildPubRel(publish, pubRec);

            final MqttPubRelWithFlow pubRelWithFlow = new MqttPubRelWithFlow(pubRel, incomingAckFlow);
            qos1Or2Map.put(packetIdentifier, pubRelWithFlow);

            incomingAckFlow.onNext(new MqttQos2Result(publish, null, pubRec, pubRelWithFlow));

            ctx.writeAndFlush(pubRel, ctx.voidPromise());
        }
    }

    private void readPubComp(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttPubComp pubComp) {
        assert qos1Or2Map != null;
        final int packetIdentifier = pubComp.getPacketIdentifier();
        final Object removed = qos1Or2Map.remove(packetIdentifier);

        if (removed == null) {
            disconnectUnknown(ctx, "PUBCOMP");
            return;
        }
        if (!(removed instanceof MqttPubRelWithFlow)) { // MqttPublishWithFlow
            qos1Or2Map.put(packetIdentifier, removed); // revert
            final MqttPublishWithFlow publishWithFlow = (MqttPublishWithFlow) removed;
            if (publishWithFlow.getPublish().getQos() == MqttQos.AT_LEAST_ONCE) {
                MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                        "PUBCOMP must not be received for a QoS 1 PUBLISH");
            } else { // EXACTLY_ONCE
                MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                        "PUBCOMP must not be received when the PUBREL has not been sent yet");
            }
            return;
        }
        final MqttPubRelWithFlow pubRelWithFlow = (MqttPubRelWithFlow) removed;

        removed(packetIdentifier);

        onPubComp(pubRelWithFlow.getPubRel(), pubComp);

        if (pubRelWithFlow.getAsBoolean()) {
            pubRelWithFlow.getIncomingAckFlow().acknowledged(1);
        }
    }

    private void removed(final int packetIdentifier) {
        assert packetIdentifiers != null;

        qos1Or2PublishQueue.removeFirst(packetIdentifier);
        packetIdentifiers.returnId(packetIdentifier);
        if (packetIdentifier > sendMaximum) {
            if (--shrinkIds == 0) {
                resize();
            }
        }
    }

    private static void disconnectUnknown(final @NotNull ChannelHandlerContext ctx, final @NotNull String type) {
        MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                type + " contained unknown packet identifier");
    }

    private void onPubAck(final @NotNull MqttPublish publish, final @NotNull MqttPubAck pubAck) {
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5OutgoingQos1ControlProvider control = advanced.getOutgoingQos1ControlProvider();
            if (control != null) {
                control.onPubAck(clientData, publish, pubAck);
            }
        }
    }

    private void onPubRecError(final @NotNull MqttPublish publish, final @NotNull MqttPubRec pubRec) {
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5OutgoingQos2ControlProvider control = advanced.getOutgoingQos2ControlProvider();
            if (control != null) {
                control.onPubRecError(clientData, publish, pubRec);
            }
        }
    }

    private @NotNull MqttPubRel buildPubRel(final @NotNull MqttPublish publish, final @NotNull MqttPubRec pubRec) {
        final MqttPubRelBuilder pubRelBuilder = new MqttPubRelBuilder(pubRec);
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5OutgoingQos2ControlProvider control = advanced.getOutgoingQos2ControlProvider();
            if (control != null) {
                control.onPubRec(clientData, publish, pubRec, pubRelBuilder);
            }
        }
        return pubRelBuilder.build();
    }

    private void onPubComp(final @NotNull MqttPubRel pubRel, final @NotNull MqttPubComp pubComp) {
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5OutgoingQos2ControlProvider control = advanced.getOutgoingQos2ControlProvider();
            if (control != null) {
                control.onPubComp(clientData, pubRel, pubComp);
            }
        }
    }

    @NotNull EventLoop getEventLoop() {
        return clientData.getEventLoop();
    }

    @NotNull MqttPublishFlowables getPublishFlowables() {
        return publishFlowables;
    }

    @Override
    public boolean isSharable() {
        return clientData.getEventLoop().inEventLoop() && (ctx == null);
    }
}
