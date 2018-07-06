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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.mqttbee.annotations.CallByThread;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQos;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ServerConnectionData;
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
import org.mqttbee.mqtt.ioc.ChannelScope;
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
import org.mqttbee.util.collections.IntMap;
import org.mqttbee.util.collections.SpscArrayQueueUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mqttbee.mqtt.message.publish.MqttStatefulPublish.*;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttOutgoingQosHandler extends ChannelInboundHandlerAdapter {

    public static final String NAME = "qos.outgoing";
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttOutgoingQosHandler.class);

    public static int getPubReceiveMaximum(final int receiveMaximum) {
        final int max = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MqttSubscriptionHandler.MAX_SUB_PENDING;
        return Math.min(receiveMaximum, max);
    }

    private final MqttClientData clientData;
    private final Queue<MqttPublishWithFlow> publishQueue;
    private final Runnable publishRunnable = this::runPublish;
    private final AtomicInteger wip = new AtomicInteger();
    private final Ranges packetIdentifiers;
    private final IntMap<MqttPublishWithFlow> qos1Or2Publishes;

    private ChannelHandlerContext ctx; // TODO temp

    @Inject
    MqttOutgoingQosHandler(final MqttClientData clientData) {
        final Mqtt5ServerConnectionData serverConnectionData = clientData.getRawServerConnectionData();
        assert serverConnectionData != null;

        this.clientData = clientData;
        final int pubReceiveMaximum = getPubReceiveMaximum(serverConnectionData.getReceiveMaximum());
        publishQueue = SpscArrayQueueUtil.create(pubReceiveMaximum, 64);
        packetIdentifiers = new Ranges(1, pubReceiveMaximum);
        qos1Or2Publishes = IntMap.range(1, pubReceiveMaximum);
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

    @CallByThread("Netty EventLoop")
    private void runPublish() {
        final int working = Math.min(wip.get(), 64);
        for (int i = 0; i < working; i++) {
            final MqttPublishWithFlow publishWithFlow = publishQueue.poll();
            assert publishWithFlow != null; // ensured by wip
            handlePublish(publishWithFlow);
        }
        ctx.flush();
        if (wip.addAndGet(-working) > 0) {
            ctx.executor().execute(publishRunnable);
        }
    }

    private void handlePublish(@NotNull final MqttPublishWithFlow publishWithFlow) {
        if (publishWithFlow.getPublish().getQos() == MqttQos.AT_MOST_ONCE) {
            handlePublishQos0(publishWithFlow);
        } else {
            handlePublishQos1Or2(publishWithFlow);
        }
    }

    private void handlePublishQos0(@NotNull final MqttPublishWithFlow publishWithFlow) {
        final MqttStatefulPublish publish =
                createStatefulPublish(publishWithFlow.getPublish(), NO_PACKET_IDENTIFIER_QOS_0, false);
        ctx.write(publish)
                .addListener(future -> publishWithFlow.getIncomingAckFlow()
                        .onNext(new MqttPublishResult(publishWithFlow.getPublish(), future.cause())));
    }

    private void handlePublishQos1Or2(@NotNull final MqttPublishWithFlow publishWithFlow) {
        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier < 0) {
            LOGGER.error("No Packet Identifier available for QoS 1 or 2 Publish message");
            return;
        }

        qos1Or2Publishes.put(packetIdentifier, publishWithFlow);
        final MqttStatefulPublish publish =
                createStatefulPublish(publishWithFlow.getPublish(), packetIdentifier, false);
        ctx.write(publish);
    }

    @NotNull
    private MqttStatefulPublish createStatefulPublish(
            @NotNull final MqttPublish publish, final int packetIdentifier, final boolean isDup) {

        final MqttServerConnectionData serverConnectionData = clientData.getRawServerConnectionData();
        assert serverConnectionData != null; // TODO
        final MqttTopicAliasMapping topicAliasMapping = serverConnectionData.getTopicAliasMapping();
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
        final MqttPublishWithFlow publishWithFlow = checkAndRemovePublishWithFlow(ctx, pubAck);
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
        final MqttPublishWithFlow publishWithFlow = checkAndRemovePublishWithFlow(ctx, pubRec);
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
        final MqttPublishWithFlow publishWithFlow = checkAndGetPublishWithFlow(ctx, pubRec);
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
        final MqttPublishWithFlow publishWithFlow = checkAndRemovePublishWithFlow(ctx, pubComp);
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
    private MqttPublishWithFlow checkAndGetPublishWithFlow(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttQosMessage qosMessage) {

        return checkPublishWithFlow(ctx, qos1Or2Publishes.get(qosMessage.getPacketIdentifier()), qosMessage);
    }

    @Nullable
    private MqttPublishWithFlow checkAndRemovePublishWithFlow(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttQosMessage qosMessage) {

        final int packetIdentifier = qosMessage.getPacketIdentifier();
        final MqttPublishWithFlow publishWithFlow = qos1Or2Publishes.remove(packetIdentifier);
        final MqttPublishWithFlow checkedPublishWithFlow = checkPublishWithFlow(ctx, publishWithFlow, qosMessage);
        if (checkedPublishWithFlow == null) {
            if (publishWithFlow != null) {
                qos1Or2Publishes.put(packetIdentifier, publishWithFlow);
            }
        } else {
            packetIdentifiers.returnId(packetIdentifier);
        }
        return checkedPublishWithFlow;
    }

    @Nullable
    private static MqttPublishWithFlow checkPublishWithFlow(
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
                    qosMessage.getType() + " must not be received for a Publish with a QoS other than " +
                            qosMessage.getQos().getCode());
            return null;
        }
        return publishWithFlow;
    }

}
