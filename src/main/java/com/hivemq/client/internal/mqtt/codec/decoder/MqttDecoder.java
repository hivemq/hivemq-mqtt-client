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

package com.hivemq.client.internal.mqtt.codec.decoder;

import com.hivemq.client.internal.mqtt.MqttClientConfig;
import com.hivemq.client.internal.mqtt.datatypes.MqttVariableByteInteger;
import com.hivemq.client.internal.mqtt.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.client.internal.mqtt.ioc.ConnectionScope;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnect;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnectRestrictions;
import com.hivemq.client.mqtt.exceptions.MqttDecodeException;
import com.hivemq.client.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.List;

/**
 * Main decoder for MQTT messages which delegates to the individual {@link MqttMessageDecoder}s when the fixed header
 * has been read and validated.
 *
 * @author Silvio Giebl
 */
@ConnectionScope
public class MqttDecoder extends ByteToMessageDecoder {

    public static final @NotNull String NAME = "decoder";
    private static final int MIN_FIXED_HEADER_LENGTH = 2;

    private final @NotNull MqttMessageDecoders decoders;
    private final @NotNull MqttDecoderContext context;

    @Inject
    MqttDecoder(
            final @NotNull MqttMessageDecoders decoders, final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttConnect connect) {

        this.decoders = decoders;
        final MqttConnectRestrictions restrictions = connect.getRestrictions();
        context = new MqttDecoderContext(restrictions.getMaximumPacketSize(), restrictions.getTopicAliasMaximum(),
                restrictions.isRequestProblemInformation(), restrictions.isRequestResponseInformation(),
                clientConfig.getAdvancedConfig().isValidatePayloadFormat(), false, false, false);
    }

    @Override
    protected void decode(
            final @NotNull ChannelHandlerContext ctx, final @NotNull ByteBuf in, final @NotNull List<Object> out) {

        if (in.readableBytes() < MIN_FIXED_HEADER_LENGTH) {
            return;
        }
        final int readerIndexBeforeFixedHeader = in.readerIndex();

        final short fixedHeader = in.readUnsignedByte();
        final int messageType = fixedHeader >> 4;
        final int flags = fixedHeader & 0xF;
        final int remainingLength = MqttVariableByteInteger.decode(in);

        try {
            if (remainingLength < 0) {
                if (remainingLength == MqttVariableByteInteger.NOT_ENOUGH_BYTES) {
                    in.readerIndex(readerIndexBeforeFixedHeader);
                    return;
                }
                throw new MqttDecoderException("malformed remaining length");
            }

            final int readerIndexAfterFixedHeader = in.readerIndex();
            final int fixedHeaderLength = readerIndexAfterFixedHeader - readerIndexBeforeFixedHeader;
            final int packetSize = fixedHeaderLength + remainingLength;

            if (packetSize > context.getMaximumPacketSize()) {
                throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE,
                        "incoming packet exceeded maximum packet size");
            }

            final int writerIndex = in.writerIndex();
            if (writerIndex < readerIndexAfterFixedHeader + remainingLength) {
                in.readerIndex(readerIndexBeforeFixedHeader);
                return;
            }

            final MqttMessageDecoder decoder = decoders.get(messageType);
            if (decoder == null) {
                throw new MqttDecoderException(
                        Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "must not receive this packet type");
            }

            in.writerIndex(readerIndexAfterFixedHeader + remainingLength);
            out.add(decoder.decode(flags, in, context));
            in.writerIndex(writerIndex);

        } catch (final MqttDecoderException e) {
            in.clear();
            final Mqtt5MessageType type = Mqtt5MessageType.fromCode(messageType);
            final String message =
                    "Exception while decoding " + ((type == null) ? "UNKNOWN" : type) + ": " + e.getMessage();
            MqttDisconnectUtil.disconnect(ctx.channel(), e.getReasonCode(), new MqttDecodeException(message));
        }
    }

    @Override
    public boolean isSharable() {
        return false;
    }
}
