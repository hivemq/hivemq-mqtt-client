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

package com.hivemq.client.internal.mqtt.handler.ssl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Silvio Giebl
 */
class MqttSslAdapterHandler extends ChannelInboundHandlerAdapter {

    public static final @NotNull String NAME = "ssl.adapter";

    private final @NotNull SslHandler sslHandler;
    private final @NotNull String host;
    private final @Nullable HostnameVerifier hostnameVerifier;
    private final @NotNull Consumer<Channel> onSuccess;
    private final @NotNull BiConsumer<Channel, Throwable> onError;
    private boolean handshakeDone = false;

    public MqttSslAdapterHandler(
            final @NotNull SslHandler sslHandler,
            final @NotNull String host,
            final @Nullable HostnameVerifier hostnameVerifier,
            final @NotNull Consumer<Channel> onSuccess,
            final @NotNull BiConsumer<Channel, Throwable> onError) {

        this.sslHandler = sslHandler;
        this.host = host;
        this.hostnameVerifier = hostnameVerifier;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }

    @Override
    public void userEventTriggered(final @NotNull ChannelHandlerContext ctx, final @NotNull Object evt) {
        if (evt instanceof SslHandshakeCompletionEvent) {
            handshakeComplete(ctx, (SslHandshakeCompletionEvent) evt);
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    private void handshakeComplete(
            final @NotNull ChannelHandlerContext ctx, final @NotNull SslHandshakeCompletionEvent evt) {

        if (setHandshakeDone()) {
            if (evt.isSuccess()) {
                ctx.pipeline().remove(this);
                if ((hostnameVerifier == null) || hostnameVerifier.verify(host, sslHandler.engine().getSession())) {
                    onSuccess.accept(ctx.channel());
                } else {
                    onError.accept(ctx.channel(), new SSLHandshakeException("Hostname verification failed"));
                }
            } else {
                // this handler is not removed here as the exception might also be fired so exceptionCaught is called
                // otherwise "An exceptionCaught() event was fired, and it reached at the tail of the pipeline" would be
                // logged
                onError.accept(ctx.channel(), evt.cause());
            }
        }
    }

    @Override
    public void exceptionCaught(final @NotNull ChannelHandlerContext ctx, final @NotNull Throwable cause) {
        // to ensure that additional exceptions are not swallowed, the handler is removed on the first exception
        ctx.pipeline().remove(this);
        if (setHandshakeDone()) {
            onError.accept(ctx.channel(), cause);
        }
        // the exception is not fired in the else branch to avoid that
        // "An exceptionCaught() event was fired, and it reached at the tail of the pipeline" is logged although it was
        // already handled via the SslHandshakeCompletionEvent
    }

    private boolean setHandshakeDone() {
        if (!handshakeDone) {
            handshakeDone = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}
