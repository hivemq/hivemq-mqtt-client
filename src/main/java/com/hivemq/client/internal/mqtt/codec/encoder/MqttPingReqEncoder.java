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

package com.hivemq.client.internal.mqtt.codec.encoder;

import com.hivemq.client.internal.mqtt.message.ping.MqttPingReq;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Silvio Giebl
 */
@Singleton
public class MqttPingReqEncoder extends MqttMessageEncoder<MqttPingReq> {

    private static final int ENCODED_LENGTH = 2;
    private static final @NotNull ByteBuf PACKET = UnpooledByteBufAllocator.DEFAULT.ioBuffer(ENCODED_LENGTH)
            .writeByte(Mqtt5MessageType.PINGREQ.getCode() << 4)
            .writeByte(0);

    @Inject
    MqttPingReqEncoder() {}

    @Override
    protected @NotNull ByteBuf encode(final @NotNull MqttPingReq message, final @NotNull MqttEncoderContext context) {
        return PACKET.retainedDuplicate();
    }
}
