package org.mqttbee.mqtt5.handler.subscribe;

import com.google.common.collect.ImmutableList;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.reactivex.Scheduler;
import io.reactivex.SingleEmitter;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.exceptions.PacketIdentifiersExceededException;
import org.mqttbee.api.mqtt.mqtt5.exceptions.Mqtt5MessageException;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5ReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.MqttServerConnectionData;
import org.mqttbee.mqtt.message.subscribe.suback.MqttSubAck;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;
import org.mqttbee.mqtt5.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt5.handler.publish.Mqtt5OutgoingQoSHandler;
import org.mqttbee.mqtt5.handler.publish.MqttIncomingPublishFlows;
import org.mqttbee.mqtt5.handler.publish.MqttSubscriptionFlow;
import org.mqttbee.mqtt5.handler.subscribe.MqttSubscribeWithFlow.MqttSubscribeWrapperWithFlow;
import org.mqttbee.mqtt5.handler.subscribe.MqttUnsubscribeWithFlow.MqttUnsubscribeWrapperWithFlow;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.util.Ranges;
import org.mqttbee.util.collections.IntMap;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;

/**
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttSubscriptionHandler extends ChannelDuplexHandler {

    public static final int MAX_SUB_PENDING = 10; // TODO configurable

    private final MqttIncomingPublishFlows subscriptionFlows;
    private final Scheduler.Worker worker;
    private final Ranges packetIdentifiers;
    private final Ranges subscriptionIdentifiers;
    private final IntMap<MqttSubscribeWrapperWithFlow> subscribes;
    private final IntMap<MqttUnsubscribeWrapperWithFlow> unsubscribes;
    private final LinkedList<Object> queued;
    private int pending;

    @Inject
    MqttSubscriptionHandler(
            final MqttIncomingPublishFlows subscriptionFlows, @Named("incomingPublish") final Scheduler.Worker worker,
            final MqttClientData clientData) {

        this.subscriptionFlows = subscriptionFlows;
        this.worker = worker;

        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;
        final MqttServerConnectionData serverConnectionData = clientData.getRawServerConnectionData();
        assert serverConnectionData != null;

        packetIdentifiers =
                new Ranges(Mqtt5OutgoingQoSHandler.getPubReceiveMaximum(serverConnectionData.getReceiveMaximum()),
                        MAX_SUB_PENDING);
        subscriptionIdentifiers = new Ranges(1, clientConnectionData.getSubscriptionIdentifierMaximum());
        subscribes = new IntMap<>(MAX_SUB_PENDING);
        unsubscribes = new IntMap<>(MAX_SUB_PENDING);
        queued = new LinkedList<>();
    }

    @Override
    public void write(final ChannelHandlerContext ctx, final Object msg, final ChannelPromise promise) {
        if (msg instanceof MqttSubscribeWithFlow) {
            handleSubscribe(ctx, (MqttSubscribeWithFlow) msg, promise);
        } else if (msg instanceof MqttUnsubscribeWithFlow) {
            handleUnsubscribe(ctx, (MqttUnsubscribeWithFlow) msg, promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private void handleSubscribe(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttSubscribeWithFlow subscribeWithFlow,
            @NotNull final ChannelPromise promise) {

        if (pending == MAX_SUB_PENDING) {
            queued.offer(subscribeWithFlow);
            return;
        }

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier == -1) {
            promise.setFailure(PacketIdentifiersExceededException.INSTANCE); // TODO must not happen
            return;
        }
        writeSubscribe(ctx, subscribeWithFlow, packetIdentifier, promise);
    }

    private void writeSubscribe(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttSubscribeWithFlow subscribeWithFlow,
            final int packetIdentifier, @NotNull final ChannelPromise promise) {

        final MqttSubscribeWrapperWithFlow subscribeWrapperWithFlow =
                subscribeWithFlow.wrap(packetIdentifier, subscriptionIdentifiers.getId());
        subscribes.put(packetIdentifier, subscribeWrapperWithFlow);
        pending++;
        final MqttSubscriptionFlow flow = subscribeWrapperWithFlow.getFlow();
        ctx.write(subscribeWrapperWithFlow.getSubscribe(), promise).addListener(future -> {
            if (!future.isSuccess()) {
                worker.schedule(() -> flow.onError(future.cause()));
                handleComplete(ctx, packetIdentifier);
            }
        });
    }

    private void handleUnsubscribe(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttUnsubscribeWithFlow unsubscribeWithFlow,
            @NotNull final ChannelPromise promise) {

        if (pending == MAX_SUB_PENDING) {
            queued.offer(unsubscribeWithFlow);
            return;
        }

        final int packetIdentifier = packetIdentifiers.getId();
        if (packetIdentifier == -1) {
            promise.setFailure(PacketIdentifiersExceededException.INSTANCE); // TODO must not happen
            return;
        }
        writeUnsubscribe(ctx, unsubscribeWithFlow, packetIdentifier, promise);
    }

    private void writeUnsubscribe(
            @NotNull final ChannelHandlerContext ctx, @NotNull final MqttUnsubscribeWithFlow unsubscribeWithFlow,
            final int packetIdentifier, @NotNull final ChannelPromise promise) {

        final MqttUnsubscribeWrapperWithFlow unsubscribeWrapperWithFlow = unsubscribeWithFlow.wrap(packetIdentifier);
        unsubscribes.put(packetIdentifier, unsubscribeWrapperWithFlow);
        pending++;
        final SingleEmitter<MqttUnsubAck> flow = unsubscribeWithFlow.getFlow();
        ctx.write(unsubscribeWrapperWithFlow.getUnsubscribe(), promise).addListener(future -> {
            if (!future.isSuccess()) {
                worker.schedule(() -> flow.onError(future.cause()));
                handleComplete(ctx, packetIdentifier);
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttSubAck) {
            handleSubAck(ctx, (MqttSubAck) msg);
        } else if (msg instanceof MqttUnsubAck) {
            handleUnsubAck(ctx, (MqttUnsubAck) msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handleSubAck(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttSubAck subAck) {
        final int packetIdentifier = subAck.getPacketIdentifier();
        final MqttSubscribeWrapperWithFlow subscribeWrapperWithFlow = subscribes.get(packetIdentifier);

        if (subscribeWrapperWithFlow == null) {
            MqttDisconnectUtil.disconnect(
                    ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "unknown packet identifier for SUBACK");
            return;
        }

        final MqttSubscriptionFlow flow = subscribeWrapperWithFlow.getFlow();
        if (allErrorCodes(subAck.getReasonCodes())) {
            worker.schedule(() -> flow.onError(new Mqtt5MessageException(subAck, "SUBACK contains only Error Codes")));
        } else {
            worker.schedule(() -> flow.onNext(subAck));
            subscriptionFlows.subscribe(subscribeWrapperWithFlow.getSubscribe(), subAck, flow);
        }

        handleComplete(ctx, packetIdentifier);
    }

    private void handleUnsubAck(@NotNull final ChannelHandlerContext ctx, @NotNull final MqttUnsubAck unsubAck) {
        final int packetIdentifier = unsubAck.getPacketIdentifier();
        final MqttUnsubscribeWrapperWithFlow unsubscribeWrapperWithFlow = unsubscribes.get(packetIdentifier);

        if (unsubscribeWrapperWithFlow == null) {
            MqttDisconnectUtil.disconnect(
                    ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "unknown packet identifier for UNSUBACK");
            return;
        }

        final SingleEmitter<MqttUnsubAck> flow = unsubscribeWrapperWithFlow.getFlow();
        if (allErrorCodes(unsubAck.getReasonCodes())) {
            worker.schedule(
                    () -> flow.onError(new Mqtt5MessageException(unsubAck, "UNSUBACK contains only Error Codes")));
        } else {
            worker.schedule(() -> flow.onSuccess(unsubAck));
            subscriptionFlows.unsubscribe(unsubscribeWrapperWithFlow.getUnsubscribe(), unsubAck);
        }

        handleComplete(ctx, packetIdentifier);
    }

    private void handleComplete(@NotNull final ChannelHandlerContext ctx, final int packetIdentifier) {
        pending--;
        final Object subscribeOrUnsubscribe = queued.poll();
        if (subscribeOrUnsubscribe == null) {
            packetIdentifiers.returnId(packetIdentifier);
        } else {
            if (subscribeOrUnsubscribe instanceof MqttSubscribeWithFlow) {
                writeSubscribe(ctx, (MqttSubscribeWithFlow) subscribeOrUnsubscribe, packetIdentifier, ctx.newPromise());
            } else {
                writeUnsubscribe(
                        ctx, (MqttUnsubscribeWithFlow) subscribeOrUnsubscribe, packetIdentifier, ctx.newPromise());
            }
        }
    }

    private static boolean allErrorCodes(@NotNull final ImmutableList<? extends Mqtt5ReasonCode> reasonCodes) {
        for (final Mqtt5ReasonCode reasonCode : reasonCodes) {
            if (!reasonCode.isError()) {
                return false;
            }
        }
        return true;
    }

}
