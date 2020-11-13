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

package com.hivemq.client.internal.mqtt.handler;

import com.hivemq.client.internal.annotations.CallByThread;
import com.hivemq.client.internal.mqtt.MqttClientConnectionConfig;
import com.hivemq.client.internal.mqtt.codec.decoder.MqttDecoder;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttIncomingQosHandler;
import com.hivemq.client.internal.mqtt.handler.publish.outgoing.MqttOutgoingQosHandler;
import com.hivemq.client.internal.mqtt.handler.subscribe.MqttSubscriptionHandler;
import com.hivemq.client.internal.mqtt.ioc.ClientScope;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnAck;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.mqtt.exceptions.MqttSessionExpiredException;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.ScheduledFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * @author Silvio Giebl
 */
@ClientScope
public class MqttSession {

    private final @NotNull MqttSubscriptionHandler subscriptionHandler;
    private final @NotNull MqttIncomingQosHandler incomingQosHandler;
    private final @NotNull MqttOutgoingQosHandler outgoingQosHandler;
    private boolean hasSession;
    private @Nullable ScheduledFuture<?> expireFuture;

    @Inject
    MqttSession(
            final @NotNull MqttSubscriptionHandler subscriptionHandler,
            final @NotNull MqttIncomingQosHandler incomingQosHandler,
            final @NotNull MqttOutgoingQosHandler outgoingQosHandler) {

        this.subscriptionHandler = subscriptionHandler;
        this.incomingQosHandler = incomingQosHandler;
        this.outgoingQosHandler = outgoingQosHandler;
    }

    @CallByThread("Netty EventLoop")
    public void startOrResume(
            final @NotNull MqttConnAck connAck,
            final @NotNull MqttClientConnectionConfig connectionConfig,
            final @NotNull ChannelPipeline pipeline,
            final @NotNull EventLoop eventLoop) {

        if (hasSession && !connAck.isSessionPresent()) {
            final String message = "Session expired as CONNACK did not contain the session present flag.";
            end(new MqttSessionExpiredException(message, new Mqtt5ConnAckException(connAck, message)));
        }
        hasSession = true;

        if (expireFuture != null) {
            expireFuture.cancel(false);
            expireFuture = null;
        }

        pipeline.addAfter(MqttDecoder.NAME, MqttSubscriptionHandler.NAME, subscriptionHandler);
        pipeline.addAfter(MqttDecoder.NAME, MqttIncomingQosHandler.NAME, incomingQosHandler);
        pipeline.addAfter(MqttDecoder.NAME, MqttOutgoingQosHandler.NAME, outgoingQosHandler);
        subscriptionHandler.onSessionStartOrResume(connectionConfig, eventLoop);
        incomingQosHandler.onSessionStartOrResume(connectionConfig, eventLoop);
        outgoingQosHandler.onSessionStartOrResume(connectionConfig, eventLoop);
    }

    @CallByThread("Netty EventLoop")
    public void expire(
            final @NotNull Throwable cause,
            final @NotNull MqttClientConnectionConfig connectionConfig,
            final @NotNull EventLoop eventLoop) {

        final long expiryInterval = connectionConfig.getSessionExpiryInterval();

        if (expiryInterval == 0) {
            // execute later to finish any current write before clearing the session state
            eventLoop.execute(
                    () -> end(new MqttSessionExpiredException("Session expired as connection was closed.", cause)));
        } else if (expiryInterval != MqttConnect.NO_SESSION_EXPIRY) {
            expireFuture = eventLoop.schedule(() -> {
                if (expireFuture != null) {
                    expireFuture = null;
                    end(new MqttSessionExpiredException("Session expired after expiry interval", cause));
                }
            }, (long) (TimeUnit.SECONDS.toMillis(expiryInterval) * 1.1), TimeUnit.MILLISECONDS);
        }
    }

    @CallByThread("Netty EventLoop")
    private void end(final @NotNull Throwable cause) {
        if (hasSession) {
            hasSession = false;
            outgoingQosHandler.onSessionEnd(cause);
            incomingQosHandler.onSessionEnd(cause);
            subscriptionHandler.onSessionEnd(cause);
        }
    }
}
