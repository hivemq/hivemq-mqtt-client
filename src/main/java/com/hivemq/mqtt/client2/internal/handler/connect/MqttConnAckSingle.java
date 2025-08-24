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

package com.hivemq.mqtt.client2.internal.handler.connect;

import com.hivemq.mqtt.client2.exceptions.ConnectionFailedException;
import com.hivemq.mqtt.client2.internal.MqttClientConfig;
import com.hivemq.mqtt.client2.internal.MqttTransportConfigImpl;
import com.hivemq.mqtt.client2.internal.exceptions.MqttClientStateExceptions;
import com.hivemq.mqtt.client2.internal.handler.MqttChannelInitializer;
import com.hivemq.mqtt.client2.internal.lifecycle.MqttDisconnectedContextImpl;
import com.hivemq.mqtt.client2.internal.lifecycle.MqttReconnector;
import com.hivemq.mqtt.client2.internal.logging.InternalLogger;
import com.hivemq.mqtt.client2.internal.logging.InternalLoggerFactory;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnect;
import com.hivemq.mqtt.client2.internal.netty.NettyEventLoopProvider;
import com.hivemq.mqtt.client2.lifecycle.MqttDisconnectSource;
import com.hivemq.mqtt.client2.lifecycle.MqttDisconnectedListener;
import com.hivemq.mqtt.client2.mqtt5.message.connect.Mqtt5ConnAck;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoop;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

import static com.hivemq.mqtt.client2.MqttClientState.*;

/**
 * @author Silvio Giebl
 */
public class MqttConnAckSingle extends Single<Mqtt5ConnAck> {

    private static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(MqttConnAckSingle.class);

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttConnect connect;

    public MqttConnAckSingle(final @NotNull MqttClientConfig clientConfig, final @NotNull MqttConnect connect) {
        this.clientConfig = clientConfig;
        this.connect = connect.setDefaults(clientConfig);
    }

    @Override
    protected void subscribeActual(final @NotNull SingleObserver<? super Mqtt5ConnAck> observer) {
        if (!clientConfig.getRawState().compareAndSet(DISCONNECTED, CONNECTING)) {
            observer.onSubscribe(Disposable.disposed());
            observer.onError(MqttClientStateExceptions.alreadyConnected());
            return;
        }

        final MqttConnAckFlow flow = new MqttConnAckFlow(observer);
        observer.onSubscribe(flow.getDisposable());
        connect(clientConfig, connect, flow, clientConfig.acquireEventLoop());
    }

    private static void connect(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttConnect connect,
            final @NotNull MqttConnAckFlow flow,
            final @NotNull EventLoop eventLoop) {
        if (flow.getDisposable().isDisposed()) {
            clientConfig.releaseEventLoop();
            clientConfig.getRawState().set(DISCONNECTED);
        } else {
            final MqttTransportConfigImpl transportConfig = clientConfig.getCurrentTransportConfig();
            new Bootstrap().channelFactory(NettyEventLoopProvider.INSTANCE.getChannelFactory())
                    .handler(new MqttChannelInitializer(clientConfig, connect, flow))
                    .group(eventLoop)
                    .connect(transportConfig.getRemoteAddress(), transportConfig.getRawLocalAddress())
                    .addListener(future -> {
                        final Throwable cause = future.cause();
                        if (cause != null) {
                            final ConnectionFailedException e = new ConnectionFailedException(cause);
                            if (eventLoop.inEventLoop()) {
                                reconnect(clientConfig, MqttDisconnectSource.CLIENT, e, connect, flow, eventLoop);
                            } else {
                                eventLoop.execute(
                                        () -> reconnect(clientConfig, MqttDisconnectSource.CLIENT, e, connect, flow,
                                                eventLoop));
                            }
                        }
                    });
        }
    }

    public static void reconnect(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttDisconnectSource source,
            final @NotNull Throwable cause,
            final @NotNull MqttConnect connect,
            final @NotNull MqttConnAckFlow flow,
            final @NotNull EventLoop eventLoop) {
        if (flow.setDone()) {
            reconnect(clientConfig, source, cause, connect, flow.getAttempts() + 1, flow, eventLoop);
        }
    }

    public static void reconnect(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttDisconnectSource source,
            final @NotNull Throwable cause,
            final @NotNull MqttConnect connect,
            final @NotNull EventLoop eventLoop) {
        reconnect(clientConfig, source, cause, connect, 0, null, eventLoop);
    }

    private static void reconnect(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttDisconnectSource source,
            final @NotNull Throwable cause,
            final @NotNull MqttConnect connect,
            final int attempts,
            final @Nullable MqttConnAckFlow flow,
            final @NotNull EventLoop eventLoop) {
        final MqttReconnector reconnector =
                new MqttReconnector(eventLoop, attempts, connect, clientConfig.getCurrentTransportConfig());
        final MqttDisconnectedContextImpl context =
                new MqttDisconnectedContextImpl(clientConfig, source, cause, reconnector);
        for (final MqttDisconnectedListener<? super MqttDisconnectedContextImpl> disconnectedListener : clientConfig.getDisconnectedListeners()) {
            try {
                disconnectedListener.onDisconnected(context);
            } catch (final Throwable t) {
                LOGGER.error("Unexpected exception thrown by disconnected listener.", t);
            }
        }
        if (reconnector.isReconnect()) {
            clientConfig.getRawState().set(DISCONNECTED_RECONNECT);
            eventLoop.schedule(() -> {
                reconnector.getFuture().whenComplete((ignored, throwable) -> {
                    if (reconnector.isReconnect()) {
                        if (clientConfig.getRawState().compareAndSet(DISCONNECTED_RECONNECT, CONNECTING_RECONNECT)) {

                            clientConfig.setCurrentTransportConfig(reconnector.getTransportConfig());
                            connect(clientConfig, reconnector.getConnect(), new MqttConnAckFlow(flow), eventLoop);
                        }

                    } else if (clientConfig.getRawState().compareAndSet(DISCONNECTED_RECONNECT, DISCONNECTED)) {
                        clientConfig.releaseEventLoop();
                        if (flow != null) {
                            if (throwable == null) {
                                flow.onError(new ConnectionFailedException("Reconnect was cancelled."));
                            } else {
                                flow.onError(new ConnectionFailedException(throwable));
                            }
                        }
                    }
                });
            }, reconnector.getDelay(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
            clientConfig.setResubscribeIfSessionPresent(reconnector.isResubscribeIfSessionPresent());
            clientConfig.setResubscribeIfSessionExpired(reconnector.isResubscribeIfSessionExpired());
            clientConfig.setRepublishIfSessionExpired(reconnector.isRepublishIfSessionExpired());
            reconnector.afterOnDisconnected();
        } else {
            clientConfig.getRawState().set(DISCONNECTED);
            clientConfig.releaseEventLoop();
            if (flow != null) {
                flow.onError(cause);
            }
        }
    }
}
