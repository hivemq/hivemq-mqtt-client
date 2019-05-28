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

package com.hivemq.client.internal.mqtt.handler.connect;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.exceptions.MqttClientStateExceptions;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttClientReconnector;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectedListenerContext;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.mqtt.exceptions.ConnectionFailedException;
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedListener;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoop;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.internal.disposables.EmptyDisposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.hivemq.client.mqtt.MqttClientState.*;

/**
 * @author Silvio Giebl
 */
public class MqttConnAckSingle extends Single<Mqtt5ConnAck> {

    private final @NotNull MqttClientConfig clientConfig;
    private final @NotNull MqttConnect connect;

    public MqttConnAckSingle(final @NotNull MqttClientConfig clientConfig, final @NotNull MqttConnect connect) {
        this.clientConfig = clientConfig;
        this.connect = connect;
    }

    @Override
    protected void subscribeActual(final @NotNull SingleObserver<? super Mqtt5ConnAck> observer) {
        if (!clientConfig.getRawState().compareAndSet(DISCONNECTED, CONNECTING)) {
            EmptyDisposable.error(MqttClientStateExceptions.alreadyConnected(), observer);
            return;
        }

        final MqttConnAckFlow flow = new MqttConnAckFlow(observer, clientConfig.getServerAddress());
        observer.onSubscribe(flow);
        if (!flow.isDisposed()) {
            connect(clientConfig, connect, flow, clientConfig.acquireEventLoop());
        }
    }

    public static void connect(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttConnect connect,
            final @NotNull MqttConnAckFlow flow, final @NotNull EventLoop eventLoop) {

        final Bootstrap bootstrap = clientConfig.getClientComponent()
                .connectionComponentBuilder()
                .connect(connect)
                .connAckFlow(flow)
                .build()
                .bootstrap();

        bootstrap.group(eventLoop).connect(flow.getServerAddress()).addListener(future -> {
            final Throwable cause = future.cause();
            if (cause != null) {
                reconnect(clientConfig, MqttClientDisconnectedListener.Source.CLIENT,
                        new ConnectionFailedException(cause), connect, flow, eventLoop);
            }
        });
    }

    public static void reconnect(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttClientDisconnectedListener.Source source,
            final @NotNull Throwable cause, final @NotNull MqttConnect connect, final @NotNull MqttConnAckFlow flow,
            final @NotNull EventLoop eventLoop) {

        if (flow.setDone() &&
                !reconnect(clientConfig, source, cause, connect, flow.getServerAddress(), flow.getAttempts() + 1, flow,
                        eventLoop)) {
            clientConfig.getRawState().set(DISCONNECTED);
            flow.onError(cause);
        }
    }

    public static void reconnect(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttClientDisconnectedListener.Source source,
            final @NotNull Throwable cause, final @NotNull MqttConnect connect,
            final @NotNull InetSocketAddress serverAddress, final @NotNull EventLoop eventLoop) {

        if (!reconnect(clientConfig, source, cause, connect, serverAddress, 0, null, eventLoop)) {
            clientConfig.getRawState().set(DISCONNECTED);
        }
    }

    private static boolean reconnect(
            final @NotNull MqttClientConfig clientConfig, final @NotNull MqttClientDisconnectedListener.Source source,
            final @NotNull Throwable cause, final @NotNull MqttConnect connect,
            final @NotNull InetSocketAddress serverAddress, final int attempts, final @Nullable MqttConnAckFlow flow,
            final @NotNull EventLoop eventLoop) {

        final MqttClientReconnector reconnector =
                new MqttClientReconnector(clientConfig, eventLoop, attempts, connect, serverAddress);
        final MqttDisconnectedListenerContext context =
                new MqttDisconnectedListenerContext(clientConfig, source, cause, reconnector);

        for (final MqttClientDisconnectedListener disconnectedListener : clientConfig.getDisconnectedListeners()) {
            disconnectedListener.onDisconnect(context);
        }

        if (!reconnector.isReconnect()) {
            return false;
        }
        clientConfig.getRawState().set(DISCONNECTED_RECONNECT);
        clientConfig.acquireEventLoop();
        eventLoop.schedule(() -> {
            reconnector.getFuture().whenComplete((ignored, throwable) -> {
                if (reconnector.isReconnect()) {
                    if (clientConfig.getRawState().compareAndSet(DISCONNECTED_RECONNECT, CONNECTING_RECONNECT)) {

                        final MqttConnAckFlow newFlow = new MqttConnAckFlow(flow, reconnector.getServerAddress());
                        connect(clientConfig, reconnector.getConnect(), newFlow, eventLoop);
                    }

                } else if (clientConfig.getRawState().compareAndSet(DISCONNECTED_RECONNECT, DISCONNECTED)) {
                    clientConfig.releaseEventLoop();
                    if (flow != null) {
                        clientConfig.getRawState().set(DISCONNECTED);
                        if (throwable == null) {
                            flow.onError(new ConnectionFailedException("Reconnect was cancelled."));
                        } else {
                            flow.onError(new ConnectionFailedException(throwable));
                        }
                    }
                }
            });
        }, reconnector.getDelay(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
        return true;
    }
}
