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

package org.mqttbee.mqtt.handler.ping;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.handler.util.ChannelInboundHandlerWithTimeout;
import org.mqttbee.mqtt.ioc.ChannelScope;
import org.mqttbee.mqtt.message.ping.MqttPingReq;
import org.mqttbee.mqtt.message.ping.MqttPingResp;

/**
 * MQTT Keep Alive Handling.
 * <ul>
 * <li>Sends a PINGREQ message when no write has been performed for the Keep Alive interval.</li>
 * <li>Disconnects or closes the channel if the PINGRESP message is not received in the timeout.</li>
 * </ul>
 *
 * @author Silvio Giebl
 */
@ChannelScope
public class MqttPingHandler extends ChannelInboundHandlerWithTimeout {

    public static final String NAME = "ping";
    private static final String IDLE_STATE_HANDLER_NAME = "ping.idle";
    private static final int PING_RESP_TIMEOUT = 60; // TODO configurable

    private final int keepAlive;

    public MqttPingHandler(final int keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        super.handlerAdded(ctx);
        ctx.pipeline().addBefore(NAME, IDLE_STATE_HANDLER_NAME, new IdleStateHandler(0, keepAlive, 0));
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        if (msg instanceof MqttPingResp) {
            cancelTimeout();
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if ((evt instanceof IdleStateEvent) && ((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
            ctx.writeAndFlush(MqttPingReq.INSTANCE).addListener(this);
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

    @Override
    protected long getTimeout(@NotNull final ChannelHandlerContext ctx) {
        return PING_RESP_TIMEOUT;
    }

    @NotNull
    @Override
    protected Mqtt5DisconnectReasonCode getTimeoutReasonCode() {
        return Mqtt5DisconnectReasonCode.KEEP_ALIVE_TIMEOUT;
    }

    @NotNull
    @Override
    protected String getTimeoutReasonString() {
        return "Timeout while waiting for PINGRESP";
    }

}
