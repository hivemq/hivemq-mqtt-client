/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client.internal.mqtt.handler.subscribe;

import com.hivemq.client.internal.annotations.CallByThread;
import com.hivemq.client.internal.logging.InternalLogger;
import com.hivemq.client.internal.logging.InternalLoggerFactory;
import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client.internal.mqtt.handler.MqttSessionAwareHandler;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttGlobalIncomingPublishFlow;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttIncomingPublishFlows;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttSubscribedPublishFlow;
import com.hivemq.client.internal.mqtt.ioc.ClientScope;
import com.hivemq.client.internal.mqtt.message.MqttReasonCodes;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttStatefulSubscribe;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubAck;
import com.hivemq.client.internal.mqtt.message.subscribe.MqttSubscribe;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttStatefulUnsubscribe;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubAck;
import com.hivemq.client.internal.mqtt.message.unsubscribe.MqttUnsubscribe;
import com.hivemq.client.internal.mqtt.message.unsubscribe.mqtt3.Mqtt3UnsubAckView;
import com.hivemq.client.internal.util.Ranges;
import com.hivemq.client.internal.util.UnsignedDataTypes;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.internal.util.collections.IntIndex;
import com.hivemq.client.internal.util.collections.NodeList;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5SubAckException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5UnsubAckException;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5SubAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAckReasonCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttSubscriptionHandler extends MqttSessionAwareHandler implements Runnable {

    public static final @NotNull String NAME = "subscription";
    private static final @NotNull InternalLogger LOGGER =
            InternalLoggerFactory.getLogger(MqttSubscriptionHandler.class);
    private static final IntIndex.@NotNull Spec<MqttSubOrUnsubWithFlow> INDEX_SPEC =
            new IntIndex.Spec<>(x -> x.packetIdentifier, 4);
    public static final int MAX_SUB_PENDING = 10; // TODO configurable

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttIncomingPublishFlows incomingPublishFlows;

    // valid for session
    private final @NotNull NodeList<MqttSubOrUnsubWithFlow> pending = new NodeList<>();
    private final @NotNull Ranges packetIdentifiers;
    private int nextSubscriptionIdentifier = 1;

    // valid for connection
    private final @NotNull IntIndex<MqttSubOrUnsubWithFlow> pendingIndex = new IntIndex<>(INDEX_SPEC);
    private @Nullable MqttSubOrUnsubWithFlow sendPending, currentPending;
    private boolean subscriptionIdentifiersAvailable;

    @Inject
    MqttSubscriptionHandler(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttIncomingPublishFlows incomingPublishFlows) {

        this.clientConfig = clientConfig;
        this.incomingPublishFlows = incomingPublishFlows;

        final int maxPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE;
        final int minPacketIdentifier = UnsignedDataTypes.UNSIGNED_SHORT_MAX_VALUE - MAX_SUB_PENDING + 1;
        packetIdentifiers = new Ranges(minPacketIdentifier, maxPacketIdentifier);
    }

    @Override
    public void onSessionStartOrResume(
            final @NotNull MqttClientConnectionConfig connectionConfig, final @NotNull EventLoop eventLoop) {

        subscriptionIdentifiersAvailable = connectionConfig.areSubscriptionIdentifiersAvailable();

        if (!hasSession) {
            incomingPublishFlows.getSubscriptions().forEach((subscriptionIdentifier, subscriptions) -> {
                final MqttSubscribe subscribe = new MqttSubscribe(ImmutableList.copyOf(subscriptions),
                        MqttUserPropertiesImpl.NO_USER_PROPERTIES);
                pending.addFirst(new MqttSubscribeWithFlow(subscribe, subscriptionIdentifier, null));
            });
        }

        pendingIndex.clear();
        sendPending = pending.getFirst();
        if (sendPending != null) {
            eventLoop.execute(this);
        }

        super.onSessionStartOrResume(connectionConfig, eventLoop);
    }

    public void subscribe(
            final @NotNull MqttSubscribe subscribe, final @NotNull MqttSubscriptionFlow<MqttSubAck> flow) {

        flow.getEventLoop().execute(() -> {
            if (flow.init()) {
                final int subscriptionIdentifier = nextSubscriptionIdentifier++;
                incomingPublishFlows.subscribe(subscribe, subscriptionIdentifier,
                        (flow instanceof MqttSubscribedPublishFlow) ? (MqttSubscribedPublishFlow) flow : null);
                queue(new MqttSubscribeWithFlow(subscribe, subscriptionIdentifier, flow));
            }
        });
    }

    public void unsubscribe(
            final @NotNull MqttUnsubscribe unsubscribe, final @NotNull MqttSubOrUnsubAckFlow<MqttUnsubAck> flow) {

        flow.getEventLoop().execute(() -> {
            if (flow.init()) {
                queue(new MqttUnsubscribeWithFlow(unsubscribe, flow));
            }
        });
    }

    public void subscribeGlobal(final @NotNull MqttGlobalIncomingPublishFlow flow) {
        flow.getEventLoop().execute(() -> {
            if (flow.init()) {
                incomingPublishFlows.subscribeGlobal(flow);
            }
        });
    }

    private void queue(final @NotNull MqttSubOrUnsubWithFlow subOrUnsubWithFlow) {
        pending.add(subOrUnsubWithFlow);
        if (sendPending == null) {
            sendPending = subOrUnsubWithFlow;
            run();
        }
    }

    @CallByThread("Netty EventLoop")
    @Override
    public void run() {
        final ChannelHandlerContext ctx = this.ctx;
        if (ctx == null) {
            return;
        }
        int written = 0;
        for (MqttSubOrUnsubWithFlow subOrUnsubWithFlow = sendPending;
             (subOrUnsubWithFlow != null) && (pendingIndex.size() < MAX_SUB_PENDING);
             sendPending = subOrUnsubWithFlow = subOrUnsubWithFlow.getNext()) {

            if (subOrUnsubWithFlow.packetIdentifier == 0) {
                final int packetIdentifier = packetIdentifiers.getId();
                if (packetIdentifier == -1) {
                    LOGGER.error(
                            "No Packet Identifier available for (UN)SUBSCRIBE. This must not happen and is a bug.");
                    return;
                }
                subOrUnsubWithFlow.packetIdentifier = packetIdentifier;
            }
            pendingIndex.put(subOrUnsubWithFlow);
            if (sendPending instanceof MqttSubscribeWithFlow) {
                writeSubscribe(ctx, (MqttSubscribeWithFlow) subOrUnsubWithFlow);
            } else {
                writeUnsubscribe(ctx, (MqttUnsubscribeWithFlow) subOrUnsubWithFlow);
            }
            written++;
        }
        if (written > 0) {
            ctx.flush();
        }
    }

    private void writeSubscribe(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttSubscribeWithFlow subscribeWithFlow) {

        final int subscriptionIdentifier = subscriptionIdentifiersAvailable ? subscribeWithFlow.subscriptionIdentifier :
                MqttStatefulSubscribe.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER;
        final MqttStatefulSubscribe statefulSubscribe =
                subscribeWithFlow.subscribe.createStateful(subscribeWithFlow.packetIdentifier, subscriptionIdentifier);

        currentPending = subscribeWithFlow;
        ctx.write(statefulSubscribe, ctx.voidPromise());
        currentPending = null;
    }

    private void writeUnsubscribe(
            final @NotNull ChannelHandlerContext ctx, final @NotNull MqttUnsubscribeWithFlow unsubscribeWithFlow) {

        final MqttStatefulUnsubscribe statefulUnsubscribe =
                unsubscribeWithFlow.unsubscribe.createStateful(unsubscribeWithFlow.packetIdentifier);

        currentPending = unsubscribeWithFlow;
        ctx.write(statefulUnsubscribe, ctx.voidPromise());
        currentPending = null;
    }

    @Override
    public void channelRead(final @NotNull ChannelHandlerContext ctx, final @NotNull Object msg) {
        if (msg instanceof MqttSubAck) {
            readSubAck(ctx, (MqttSubAck) msg);
        } else if (msg instanceof MqttUnsubAck) {
            readUnsubAck(ctx, (MqttUnsubAck) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readSubAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttSubAck subAck) {
        final MqttSubOrUnsubWithFlow subOrUnsubWithFlow = pendingIndex.remove(subAck.getPacketIdentifier());

        if (subOrUnsubWithFlow == null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Unknown packet identifier for SUBACK");
            return;
        }
        if (!(subOrUnsubWithFlow instanceof MqttSubscribeWithFlow)) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "SUBACK received for an UNSUBSCRIBE");
            return;
        }
        final MqttSubscribeWithFlow subscribeWithFlow = (MqttSubscribeWithFlow) subOrUnsubWithFlow;
        final MqttSubscriptionFlow<MqttSubAck> flow = subscribeWithFlow.getFlow();

        final ImmutableList<Mqtt5SubAckReasonCode> reasonCodes = subAck.getReasonCodes();
        final boolean countNotMatching = subscribeWithFlow.subscribe.getSubscriptions().size() != reasonCodes.size();
        final boolean allErrors = MqttReasonCodes.allErrors(subAck.getReasonCodes());

        incomingPublishFlows.subAck(subscribeWithFlow.subscribe, subscribeWithFlow.subscriptionIdentifier, reasonCodes);

        if (flow != null) {
            if (!(countNotMatching || allErrors)) {
                if (!flow.isCancelled()) {
                    flow.onSuccess(subAck);
                } else {
                    LOGGER.warn("Subscribe was successful but the SubAck flow has been cancelled");
                }
            } else {
                final String errorMessage;
                if (countNotMatching) {
                    errorMessage = "Count of Reason Codes in SUBACK does not match count of subscriptions in SUBSCRIBE";
                } else { // allErrors
                    errorMessage = "SUBACK contains only Error Codes";
                }
                if (!flow.isCancelled()) {
                    flow.onError(new Mqtt5SubAckException(subAck, errorMessage));
                } else {
                    LOGGER.warn(errorMessage + " but the SubAck flow has been cancelled");
                }
            }
        }

        completePending(subscribeWithFlow);
    }

    private void readUnsubAck(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttUnsubAck unsubAck) {
        final MqttSubOrUnsubWithFlow subOrUnsubWithFlow = pendingIndex.remove(unsubAck.getPacketIdentifier());

        if (subOrUnsubWithFlow == null) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "Unknown packet identifier for UNSUBACK");
            return;
        }
        if (!(subOrUnsubWithFlow instanceof MqttUnsubscribeWithFlow)) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "UNSUBACK received for a SUBSCRIBE");
            return;
        }
        final MqttUnsubscribeWithFlow unsubscribeWithFlow = (MqttUnsubscribeWithFlow) subOrUnsubWithFlow;
        final MqttSubOrUnsubAckFlow<MqttUnsubAck> flow = unsubscribeWithFlow.getFlow();

        final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes = unsubAck.getReasonCodes();
        final boolean countNotMatching = unsubscribeWithFlow.unsubscribe.getTopicFilters().size() != reasonCodes.size();
        final boolean allErrors = MqttReasonCodes.allErrors(unsubAck.getReasonCodes());

        if ((reasonCodes == Mqtt3UnsubAckView.REASON_CODES_ALL_SUCCESS) || !(countNotMatching || allErrors)) {

            incomingPublishFlows.unsubscribe(unsubscribeWithFlow.unsubscribe, reasonCodes);

            if (!flow.isCancelled()) {
                flow.onSuccess(unsubAck);
            } else {
                LOGGER.warn("Unsubscribe was successful but the UnsubAck flow has been cancelled");
            }
        } else {
            final String errorMessage;
            if (countNotMatching) {
                errorMessage = "Count of Reason Codes in UNSUBACK does not match count of Topic Filters in UNSUBSCRIBE";
            } else { // allErrors
                errorMessage = "UNSUBACK contains only Error Codes";
            }
            if (!flow.isCancelled()) {
                flow.onError(new Mqtt5UnsubAckException(unsubAck, errorMessage));
            } else {
                LOGGER.warn(errorMessage + " but the UnsubAck flow has been cancelled");
            }
        }

        completePending(unsubscribeWithFlow);
    }

    private void completePending(final @NotNull MqttSubOrUnsubWithFlow oldPending) {
        pending.remove(oldPending);
        packetIdentifiers.returnId(oldPending.packetIdentifier);
        run();
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        if (!(cause instanceof IOException) && (currentPending != null)) {
            pending.remove(currentPending);
            packetIdentifiers.returnId(currentPending.packetIdentifier);
            pendingIndex.remove(currentPending.packetIdentifier);

            final MqttSubscriptionFlow<?> flow = currentPending.getFlow();
            if (flow != null) {
                flow.onError(cause);
            }

            if (currentPending instanceof MqttSubscribeWithFlow) {
                final MqttSubscribeWithFlow subscribeWithFlow = (MqttSubscribeWithFlow) currentPending;
                incomingPublishFlows.subAck(subscribeWithFlow.subscribe, subscribeWithFlow.subscriptionIdentifier,
                        ImmutableList.of(Mqtt5SubAckReasonCode.UNSPECIFIED_ERROR));
            }

            currentPending = null;
        } else {
            ctx.fireExceptionCaught(cause);
        }
    }

    @Override
    public void onSessionEnd(final @NotNull Throwable cause) {
        super.onSessionEnd(cause);

        pendingIndex.clear();
        sendPending = null;
        for (MqttSubOrUnsubWithFlow current = pending.getFirst(); current != null; current = current.getNext()) {
            if (current.packetIdentifier == 0) {
                break;
            }
            packetIdentifiers.returnId(current.packetIdentifier);
            current.packetIdentifier = 0;
        }

        if (clientConfig.isResubscribeIfSessionExpired() && (clientConfig.getState() != MqttClientState.DISCONNECTED)) {
            return;
        }

        incomingPublishFlows.clear(cause);
        for (MqttSubOrUnsubWithFlow current = pending.getFirst(); current != null; current = current.getNext()) {
            final MqttSubscriptionFlow<?> flow = current.getFlow();
            if (flow != null) {
                flow.onError(cause);
            }
        }
        pending.clear();
        nextSubscriptionIdentifier = 1;
    }
}
