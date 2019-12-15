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

package com.hivemq.client.internal.mqtt.handler.auth;

import com.hivemq.client.internal.logging.InternalLogger;
import com.hivemq.client.internal.logging.InternalLoggerFactory;
import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.handler.util.MqttTimeoutInboundHandler;
import com.hivemq.client.internal.mqtt.message.auth.MqttAuth;
import com.hivemq.client.internal.mqtt.message.auth.MqttAuthBuilder;
import com.hivemq.client.internal.util.Checks;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5AuthException;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5Auth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode.CONTINUE_AUTHENTICATION;

/**
 * Base for enhanced auth handling according to the MQTT 5 specification.
 *
 * @author Silvio Giebl
 */
abstract class AbstractMqttAuthHandler extends MqttTimeoutInboundHandler implements MqttAuthHandler {

    static final @NotNull InternalLogger LOGGER = InternalLoggerFactory.getLogger(AbstractMqttAuthHandler.class);

    enum MqttAuthState {
        NONE,
        WAIT_FOR_SERVER,
        IN_PROGRESS_INIT,
        IN_PROGRESS_RESPONSE,
        IN_PROGRESS_DONE
    }

    final @NotNull MqttClientConfig clientConfig;
    final @NotNull Mqtt5EnhancedAuthMechanism authMechanism;
    @NotNull MqttAuthState state = MqttAuthState.NONE;

    AbstractMqttAuthHandler(
            final @NotNull MqttClientConfig clientConfig, final @NotNull Mqtt5EnhancedAuthMechanism authMechanism) {

        this.clientConfig = clientConfig;
        this.authMechanism = authMechanism;
    }

    /**
     * Handles an incoming AUTH message. Sends a DISCONNECT message if the AUTH message is not valid.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    final void readAuth(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        cancelTimeout();

        if (validateAuth(ctx, auth)) {
            switch (auth.getReasonCode()) {
                case CONTINUE_AUTHENTICATION:
                    readAuthContinue(ctx, auth);
                    break;
                case SUCCESS:
                    readAuthSuccess(ctx, auth);
                    break;
                case REAUTHENTICATE:
                    readReAuth(ctx, auth);
                    break;
            }
        }
    }

    /**
     * Validates an incoming AUTH message.
     * <p>
     * If validation fails, disconnection and closing of the channel is already handled.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     * @return true if the AUTH message is valid, otherwise false.
     */
    private boolean validateAuth(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        if (!auth.getMethod().equals(getMethod())) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5AuthException(auth, "Auth method in AUTH must be the same as in the CONNECT."));
            return false;
        }
        return true;
    }

    /**
     * Handles an incoming AUTH message with the Reason Code CONTINUE AUTHENTICATION.
     * <ul>
     * <li>Calls {@link Mqtt5EnhancedAuthMechanism#onContinue(Mqtt5ClientConfig, Mqtt5Auth, Mqtt5AuthBuilder)}.</li>
     * <li>Sends a new AUTH message if the enhanced auth mechanism accepted the incoming AUTH message.</li>
     * <li>Otherwise sends a DISCONNECT message.</li>
     * </ul>
     *
     * @param ctx  the channel handler context.
     * @param auth the received AUTH message.
     */
    private void readAuthContinue(final @NotNull ChannelHandlerContext ctx, final @NotNull MqttAuth auth) {
        if (state != MqttAuthState.WAIT_FOR_SERVER) {
            MqttDisconnectUtil.disconnect(ctx.channel(), Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    new Mqtt5AuthException(auth, "Must not receive AUTH with reason code CONTINUE_AUTHENTICATION " +
                            "if client side AUTH is pending."));
            return;
        }

        final MqttAuthBuilder authBuilder = new MqttAuthBuilder(CONTINUE_AUTHENTICATION, getMethod());
        state = MqttAuthState.IN_PROGRESS_RESPONSE;
        callMechanismFutureResult(() -> authMechanism.onContinue(clientConfig, auth, authBuilder), ctx2 -> {
            state = MqttAuthState.WAIT_FOR_SERVER;
            ctx2.writeAndFlush(authBuilder.build()).addListener(this);

        }, (ctx2, throwable) -> MqttDisconnectUtil.disconnect(ctx2.channel(), Mqtt5DisconnectReasonCode.NOT_AUTHORIZED,
                new Mqtt5AuthException(auth, "Server auth not accepted.")));
    }

    /**
     * Disconnects on an incoming AUTH message with the Reason Code SUCCESS.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    abstract void readAuthSuccess(@NotNull ChannelHandlerContext ctx, @NotNull MqttAuth auth);

    /**
     * Disconnects on an incoming AUTH message with the Reason Code REAUTHENTICATE.
     *
     * @param ctx  the channel handler context.
     * @param auth the incoming AUTH message.
     */
    abstract void readReAuth(@NotNull ChannelHandlerContext ctx, @NotNull MqttAuth auth);

    void callMechanism(final @NotNull Runnable call) {
        try {
            call.run();
        } catch (final Throwable throwable) {
            LOGGER.error("Auth cancelled. Unexpected exception thrown by auth mechanism.", throwable);
        }
    }

    void callMechanismFuture(
            final @NotNull Supplier<@NotNull CompletableFuture<Void>> supplier,
            final @NotNull Consumer<@NotNull ChannelHandlerContext> onSuccess,
            final @NotNull BiConsumer<@NotNull ChannelHandlerContext, @NotNull Throwable> onError) {

        if (ctx == null) {
            return;
        }
        try {
            supplier.get().whenComplete((aVoid, throwable) -> clientConfig.executeInEventLoop(() -> {
                if (ctx == null) {
                    return;
                }
                if (throwable != null) {
                    LOGGER.error("Auth cancelled. Unexpected exception thrown by auth mechanism.", throwable);
                    onError.accept(ctx, throwable);
                } else {
                    onSuccess.accept(ctx);
                }
            }));
        } catch (final Throwable throwable) {
            LOGGER.error("Auth cancelled. Unexpected exception thrown by auth mechanism.", throwable);
            onError.accept(ctx, throwable);
        }
    }

    void callMechanismFutureResult(
            final @NotNull Supplier<@NotNull CompletableFuture<Boolean>> supplier,
            final @NotNull Consumer<@NotNull ChannelHandlerContext> onSuccess,
            final @NotNull BiConsumer<@NotNull ChannelHandlerContext, @Nullable Throwable> onError) {

        if (ctx == null) {
            return;
        }
        try {
            supplier.get().whenComplete((accepted, throwable) -> clientConfig.executeInEventLoop(() -> {
                if (ctx == null) {
                    return;
                }
                if (throwable != null) {
                    LOGGER.error("Auth cancelled. Unexpected exception thrown by auth mechanism.", throwable);
                    onError.accept(ctx, throwable);
                } else if (accepted == null) {
                    LOGGER.error("Auth cancelled. Unexpected null result returned by auth mechanism.");
                    onError.accept(
                            ctx, new NullPointerException("Result returned by auth mechanism must not be null."));
                } else if (!accepted) {
                    onError.accept(ctx, null);
                } else {
                    onSuccess.accept(ctx);
                }
            }));
        } catch (final Throwable throwable) {
            LOGGER.error("Auth cancelled. Unexpected exception thrown by auth mechanism.", throwable);
            onError.accept(ctx, throwable);
        }
    }

    @NotNull MqttUtf8StringImpl getMethod() {
        return Checks.notImplemented(authMechanism.getMethod(), MqttUtf8StringImpl.class, "Auth method");
    }

    @Override
    protected final long getTimeout() {
        return authMechanism.getTimeout();
    }

    @Override
    protected final @NotNull Mqtt5DisconnectReasonCode getTimeoutReasonCode() {
        return Mqtt5DisconnectReasonCode.NOT_AUTHORIZED;
    }
}
