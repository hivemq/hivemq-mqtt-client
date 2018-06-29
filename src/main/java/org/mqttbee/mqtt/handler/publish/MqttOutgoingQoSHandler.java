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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ServerConnectionData;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos1.Mqtt5OutgoingQoS1ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.advanced.qos2.Mqtt5OutgoingQoS2ControlProvider;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.advanced.MqttAdvancedClientData;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.handler.subscribe.MqttSubscriptionHandler;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.publish.MqttPublish;
import org.mqttbee.mqtt.message.publish.MqttPublishResult;
import org.mqttbee.mqtt.message.publish.MqttPublishResult.MqttQoS1Result;
import org.mqttbee.mqtt.message.publish.MqttPublishResult.MqttQoS2Result;
import org.mqttbee.mqtt.message.publish.MqttStatefulPublish;
import org.mqttbee.mqtt.message.publish.MqttTopicAliasMapping;
import org.mqttbee.mqtt.message.publish.puback.MqttPubAck;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubComp;
import org.mqttbee.mqtt.message.publish.pubrec.MqttPubRec;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRel;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelBuilder;
import org.mqttbee.util.Ranges;
import org.mqttbee.util.UnsignedDataTypes;
import org.mqttbee.util.collections.IntMap;
import org.mqttbee.util.collections.SpscArrayQueueUtil;

import javax.inject.Inject;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mqttbee.mqtt.message.publish.MqttStatefulPublish.*;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttOutgoingQoSHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "qos.outgoing";

    public static int getPubReceiveMaximum(final int receiveMaximum) {
        final int max = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MqttSubscriptionHandler.MAX_SUB_PENDING;
        return Math.min(receiveMaximum, max);
    }

    private final Queue<MqttPublishWithFlow> publishQueue;
    private final Runnable publishRunnable = this::runPublish;
    private final AtomicInteger wip = new AtomicInteger();
    private final Ranges packetIdentifiers;
    private final IntMap<MqttPublishWithFlow> qos1Or2Publishes;

    private ChannelHandlerContext ctx; // TODO temp

    @Inject
    MqttOutgoingQoSHandler(final MqttClientData clientData) {
        final Mqtt5ServerConnectionData serverConnectionData = clientData.getRawServerConnectionData();
        assert serverConnectionData != null;

        final int pubReceiveMaximum = getPubReceiveMaximum(serverConnectionData.getReceiveMaximum());
        publishQueue = SpscArrayQueueUtil.create(pubReceiveMaximum, 64);
        packetIdentifiers = new Ranges(1, pubReceiveMaximum);
        qos1Or2Publishes = new IntMap<>(1, pubReceiveMaximum);
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    void publish(@NotNull final MqttPublishWithFlow publishWithFlow) {
        publishQueue.offer(publishWithFlow);
        if (wip.getAndIncrement() == 0) {
            ctx.executor().execute(publishRunnable);
        }
    }

    private void runPublish() {
        final int working = Math.min(wip.get(), 64);
        for (int i = 0; i < working; i++) {
            final MqttPublishWithFlow publishWithFlow = publishQueue.poll();
            assert publishWithFlow != null; // ensured by wip
            handlePublish(ctx, publishWithFlow);
        }
        ctx.flush();
        if (wip.addAndGet(-working) > 0) {
            ctx.executor().execute(publishRunnable);
        }
    }

    private void handlePublish(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWithFlow publishWithFlow) {

        if (publishWithFlow.getPublish().getQos() == MqttQoS.AT_MOST_ONCE) {
            handlePublishQoS0(ctx, publishWithFlow);
        } else {
            handlePublishQoS1Or2(ctx, publishWithFlow);
        }
    }

    private void handlePublishQoS0(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWithFlow publishWithFlow) {

        final MqttStatefulPublish publish =
                createStatefulPublish(ctx.channel(), publishWithFlow.getPublish(), NO_PACKET_IDENTIFIER_QOS_0, false);
        ctx.write(publish)
                .addListener(future -> publishWithFlow.getIncomingAckFlow()
                        .onNext(new MqttPublishResult(publishWithFlow.getPublish(), null)));
    }

    private void handlePublishQoS1Or2(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttPublishWithFlow publishWithFlow) {

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier < 0) {
            // TODO must not happen
            return;
        }

        qos1Or2Publishes.put(packetIdentifier, publishWithFlow);
        final MqttStatefulPublish publish =
                createStatefulPublish(ctx.channel(), publishWithFlow.getPublish(), packetIdentifier, false);
        ctx.write(publish);
    }

    private MqttStatefulPublish createStatefulPublish(
            @NotNull final Channel channel, @NotNull final MqttPublish publish, final int packetIdentifier,
            final boolean isDup) {

        final MqttTopicAliasMapping topicAliasMapping = MqttServerConnectionData.getTopicAliasMapping(channel);
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
                topicAlias = topicAliasMapping.set(topic, publish.getTopicAliasUsage());
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
        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5OutgoingQoS1ControlProvider control = advanced.getOutgoingQoS1ControlProvider();
            if (control != null) {
                control.onPubAck(pubAck);
            }
        }

        final MqttPublishWithFlow publishWithFlow = remove(pubAck.getPacketIdentifier());
        if ((publishWithFlow == null) || (publishWithFlow.getPublish().getQos() != MqttQoS.AT_LEAST_ONCE)) {
            // TODO
            return;
        }
        publishWithFlow.getIncomingAckFlow().onNext(new MqttQoS1Result(publishWithFlow.getPublish(), null, pubAck));
    }

    private void handlePubRec(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRec pubRec) {
        if (pubRec.getReasonCode().isError()) {
            handlePubRecError(ctx, pubRec);
        } else {
            handlePubRecSuccess(ctx, pubRec);
        }
    }

    private void handlePubRecError(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRec pubRec) {
        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5OutgoingQoS2ControlProvider control = advanced.getOutgoingQoS2ControlProvider();
            if (control != null) {
                control.onPubRecError(pubRec);
            }
        }

        final MqttPublishWithFlow publishWithFlow = remove(pubRec.getPacketIdentifier());
        if ((publishWithFlow == null) || (publishWithFlow.getPublish().getQos() != MqttQoS.EXACTLY_ONCE)) {
            // TODO
            return;
        }
        publishWithFlow.getIncomingAckFlow()
                .onNext(new MqttPublishResult(publishWithFlow.getPublish(),
                        new Mqtt5MessageException(pubRec, "PUBREC contained an Error Code")));
    }

    private void handlePubRecSuccess(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubRec pubRec) {
        final MqttPubRelBuilder pubRelBuilder = new MqttPubRelBuilder(pubRec);

        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5OutgoingQoS2ControlProvider control = advanced.getOutgoingQoS2ControlProvider();
            if (control != null) {
                control.onPubRec(pubRec, pubRelBuilder);
            }
        }

        final MqttPubRel pubRel = pubRelBuilder.build();
        ctx.writeAndFlush(pubRel);
    }

    private void handlePubComp(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttPubComp pubComp) {
        final MqttAdvancedClientData advanced = MqttClientData.from(ctx.channel()).getRawAdvancedClientData();
        if ((advanced != null)) {
            final Mqtt5OutgoingQoS2ControlProvider control = advanced.getOutgoingQoS2ControlProvider();
            if (control != null) {
                control.onPubComp(pubComp);
            }
        }

        final MqttPublishWithFlow publishWithFlow = remove(pubComp.getPacketIdentifier());
        if ((publishWithFlow == null) || (publishWithFlow.getPublish().getQos() != MqttQoS.EXACTLY_ONCE)) {
            // TODO
            return;
        }
        publishWithFlow.getIncomingAckFlow().onNext(new MqttQoS2Result(publishWithFlow.getPublish(), null, pubComp));
    }

    private MqttPublishWithFlow remove(final int packetIdentifier) {
        packetIdentifiers.returnId(packetIdentifier);
        return qos1Or2Publishes.remove(packetIdentifier);
    }

}
