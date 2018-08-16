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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.EventExecutor;
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
import org.mqttbee.mqtt.message.publish.*;
import org.mqttbee.mqtt.message.publish.MqttPublishResult.MqttQos1Result;
import org.mqttbee.mqtt.message.publish.MqttPublishResult.MqttQos2Result;
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

    public static final String NAME = "qos.outgoing";
    private static final int MAX_CONCURRENT_PUBLISH_FLOWABLES = 64; // TODO configurable
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttOutgoingQosHandler.class);

    private final MqttClientData clientData;

    private final SpscChunkedArrayQueue<MqttPublishWithFlow> publishQueue = new SpscChunkedArrayQueue<>(32, 1 << 30);
    private final AtomicInteger queuedCounter = new AtomicInteger();
    private final ChunkedArrayQueue<MqttPublishWithFlow> qos0PublishQueue = new ChunkedArrayQueue<>(32);
    private final ChunkedIntArrayQueue qos1Or2PublishQueue = new ChunkedIntArrayQueue(32);

    private ChannelHandlerContext ctx;
    private volatile ChannelHandlerContext ctxVolatile; // TODO inject EventLoop/get from clientData
    private int sendMaximum;
    private Ranges packetIdentifiers;
    private IntMap<MqttPublishWithFlow> qos1Or2PublishMap;
    private MqttTopicAliasMapping topicAliasMapping;
    private int shrinkIds;
    private int shrinkRequests;

    private Subscription subscription;

    @Inject
    MqttOutgoingQosHandler(final MqttClientData clientData, final MqttPublishFlowables publishFlowables) {
        this.clientData = clientData;
        publishFlowables.flatMap(f -> f, true, MAX_CONCURRENT_PUBLISH_FLOWABLES).subscribe(this);
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
        ctxVolatile = ctx;

        final MqttServerConnectionData serverConnectionData = clientData.getRawServerConnectionData();
        assert serverConnectionData != null;

        final int oldSendMaximum = sendMaximum;
        final int newSendMaximum = Math.min(
                serverConnectionData.getReceiveMaximum(),
                UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MqttSubscriptionHandler.MAX_SUB_PENDING);
        sendMaximum = newSendMaximum;
        if (oldSendMaximum == 0) {
            packetIdentifiers = new Ranges(1, newSendMaximum);
            qos1Or2PublishMap = IntMap.range(1, newSendMaximum);
            subscription.request(newSendMaximum);
        } else {
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
        shrinkIds = packetIdentifiers.resize(sendMaximum);
        if (shrinkIds == 0) {
            qos1Or2PublishMap = IntMap.resize(qos1Or2PublishMap, sendMaximum);
        }
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public void onNext(final MqttPublishWithFlow publishWithFlow) {
        publishQueue.offer(publishWithFlow);
        if (queuedCounter.getAndIncrement() == 0) {
            getNettyEventLoop().execute(this);
        }
    }

    @Override
    public void onComplete() {
        LOGGER.error("MqttPublishFlowables is global and must never complete. This must not happen and is a bug.");
    }

    @Override
    public void onError(final Throwable t) {
        LOGGER.error("MqttPublishFlowables is global and must never error. This must not happen and is a bug.");
    }

    @CallByThread("Netty EventLoop")
    void request(final long amount) {
        if (shrinkRequests == 0) {
            subscription.request(amount);
        } else {
            shrinkRequests--;
        }
    }

    @CallByThread("Netty EventLoop")
    public void run() {
        final int working = Math.min(queuedCounter.get(), 64);
        for (int i = 0; i < working; i++) {
            final MqttPublishWithFlow publishWithFlow = publishQueue.poll();
            assert publishWithFlow != null; // ensured by wip
            handlePublish(publishWithFlow);
        }
        ctx.flush();
        if (queuedCounter.addAndGet(-working) > 0) {
            ctx.executor().execute(this);
        }
    }

    private void handlePublish(@NotNull final MqttPublishWithFlow publishWithFlow) {
        if (publishWithFlow.getPublish().getQos() == MqttQos.AT_MOST_ONCE) {
            handleQos0Publish(publishWithFlow);
        } else {
            handleQos1Or2Publish(publishWithFlow);
        }
    }

    private void handleQos0Publish(@NotNull final MqttPublishWithFlow publishWithFlow) {
        qos0PublishQueue.offer(publishWithFlow);
        ctx.write(addState(publishWithFlow.getPublish(), NO_PACKET_IDENTIFIER_QOS_0, false)).addListener(this);
    }

    @Override
    public void operationComplete(final ChannelFuture future) {
        final MqttPublishWithFlow publishWithFlow = qos0PublishQueue.poll();
        assert publishWithFlow != null; // ensured by handleQos0Publish
        publishWithFlow.getIncomingAckFlow()
                .onNext(new MqttPublishResult(publishWithFlow.getPublish(), future.cause()));
    }

    private void handleQos1Or2Publish(@NotNull final MqttPublishWithFlow publishWithFlow) {
        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier < 0) {
            LOGGER.error("No Packet Identifier available for QoS 1 or 2 PUBLISH. This must not happen and is a bug.");
            return;
        }
        qos1Or2PublishMap.put(packetIdentifier, publishWithFlow);
        qos1Or2PublishQueue.offer(packetIdentifier);
        ctx.write(addState(publishWithFlow.getPublish(), packetIdentifier, false), ctx.voidPromise());
    }

    @NotNull
    private MqttStatefulPublish addState(
            @NotNull final MqttPublish publish, final int packetIdentifier, final boolean isDup) {

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
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttPubAck) {
            handlePubAck(ctx, (MqttPubAck) msg);
        } else if (msg instanceof MqttPubRec) {
            handlePubRec(ctx, (MqttPubRec) msg);
        } else if (msg instanceof MqttPubComp) {
            handlePubComp(ctx, (MqttPubComp) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handlePubAck(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubAck pubAck) {
        final MqttPublishWithFlow publishWithFlow = checkAndRemoveQos1Or2PublishWithFlow(ctx, pubAck);
        if (publishWithFlow == null) {
            return;
        }
        final MqttPublish publish = publishWithFlow.getPublish();

        publishWithFlow.getIncomingAckFlow().onNext(new MqttQos1Result(publish, null, pubAck));

        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5OutgoingQos1ControlProvider control = advanced.getOutgoingQos1ControlProvider();
            if (control != null) {
                control.onPubAck(clientData, publish, pubAck);
            }
        }
    }

    private void handlePubRec(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRec pubRec) {
        if (pubRec.getReasonCode().isError()) {
            handlePubRecError(ctx, pubRec);
        } else {
            handlePubRecSuccess(ctx, pubRec);
        }
    }

    private void handlePubRecError(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRec pubRec) {
        final MqttPublishWithFlow publishWithFlow = checkAndRemoveQos1Or2PublishWithFlow(ctx, pubRec);
        if (publishWithFlow == null) {
            return;
        }
        final MqttPublish publish = publishWithFlow.getPublish();

        publishWithFlow.getIncomingAckFlow().onNext(new MqttPublishResult(
                publish,
                        new Mqtt5MessageException(pubRec, "PUBREC contained an Error Code")));

        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5OutgoingQos2ControlProvider control = advanced.getOutgoingQos2ControlProvider();
            if (control != null) {
                control.onPubRecError(clientData, publish, pubRec);
            }
        }
    }

    private void handlePubRecSuccess(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRec pubRec) {
        final MqttPublishWithFlow publishWithFlow = checkAndGetQos1Or2PublishWithFlow(ctx, pubRec);
        if (publishWithFlow == null) {
            return;
        }

        final MqttPubRelBuilder pubRelBuilder = new MqttPubRelBuilder(pubRec);
        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5OutgoingQos2ControlProvider control = advanced.getOutgoingQos2ControlProvider();
            if (control != null) {
                control.onPubRec(clientData, publishWithFlow.getPublish(), pubRec, pubRelBuilder);
            }
        }

        final MqttPubRel pubRel = pubRelBuilder.build();
        publishWithFlow.setPubRel(pubRel);
        ctx.writeAndFlush(pubRel);
    }

    private void handlePubComp(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubComp pubComp) {
        final MqttPublishWithFlow publishWithFlow = checkAndRemoveQos1Or2PublishWithFlow(ctx, pubComp);
        if (publishWithFlow == null) {
            return;
        }
        final MqttPublish publish = publishWithFlow.getPublish();
        final MqttPubRel pubRel = publishWithFlow.getPubRel();
        assert pubRel != null;

        publishWithFlow.getIncomingAckFlow().onNext(new MqttQos2Result(publish, null, pubRel, pubComp));

        final MqttAdvancedClientData advanced = clientData.getRawAdvancedClientData();
        if (advanced != null) {
            final Mqtt5OutgoingQos2ControlProvider control = advanced.getOutgoingQos2ControlProvider();
            if (control != null) {
                control.onPubComp(clientData, publish, pubComp);
            }
        }
    }

    @Nullable
    private MqttPublishWithFlow checkAndGetQos1Or2PublishWithFlow(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttQosMessage qosMessage) {

        return checkQos1Or2PublishWithFlow(ctx, qos1Or2PublishMap.get(qosMessage.getPacketIdentifier()), qosMessage);
    }

    @Nullable
    private MqttPublishWithFlow checkAndRemoveQos1Or2PublishWithFlow(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttQosMessage qosMessage) {

        final int packetIdentifier = qosMessage.getPacketIdentifier();
        final MqttPublishWithFlow publishWithFlow = qos1Or2PublishMap.remove(packetIdentifier);
        final MqttPublishWithFlow checkedPublishWithFlow =
                checkQos1Or2PublishWithFlow(ctx, publishWithFlow, qosMessage);
        if (checkedPublishWithFlow == null) {
            if (publishWithFlow != null) {
                qos1Or2PublishMap.put(packetIdentifier, publishWithFlow);
            }
        } else {
            qos1Or2PublishQueue.removeFirst(packetIdentifier);
            packetIdentifiers.returnId(packetIdentifier);
            if (packetIdentifier > sendMaximum) {
                if (--shrinkIds == 0) {
                    resize();
                }
            }
        }
        return checkedPublishWithFlow;
    }

    @Nullable
    private static MqttPublishWithFlow checkQos1Or2PublishWithFlow(
            @NotNull final ChannelHandlerContext ctx, @Nullable final MqttPublishWithFlow publishWithFlow,
            @NotNull final MqttQosMessage qosMessage) {

        if (publishWithFlow == null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    qosMessage.getType() + " contained unknown Packet Identifier");
            return null;
        }
        final MqttPublish publish = publishWithFlow.getPublish();
        if (publish.getQos() != qosMessage.getQos()) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    qosMessage.getType() + " must not be received for a PUBLISH with a QoS other than " +
                            qosMessage.getQos().getCode());
            return null;
        }
        return publishWithFlow;
    }

    @NotNull EventExecutor getNettyEventLoop() {
        return ctxVolatile.executor();
    }

}
