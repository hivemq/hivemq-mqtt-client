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

package com.hivemq.mqtt.client2.internal.codec.decoder;

import com.hivemq.mqtt.client2.exceptions.MqttDecodeException;
import com.hivemq.mqtt.client2.internal.MqttClientConfig;
import com.hivemq.mqtt.client2.internal.codec.decoder.mqtt3.Mqtt3ClientMessageDecoders;
import com.hivemq.mqtt.client2.internal.codec.decoder.mqtt5.Mqtt5ClientMessageDecoders;
import com.hivemq.mqtt.client2.internal.datatypes.MqttVariableByteInteger;
import com.hivemq.mqtt.client2.internal.handler.disconnect.MqttDisconnectUtil;
import com.hivemq.mqtt.client2.internal.message.connect.MqttConnectRestrictions;
import com.hivemq.mqtt.client2.mqtt5.message.Mqtt5MessageType;
import com.hivemq.mqtt.client2.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Main decoder for MQTT messages which delegates to the individual {@link MqttMessageDecoder}s when the fixed header
 * has been read and validated.
 *
 * @author Silvio Giebl
 */
public class MqttDecoder extends ByteToMessageDecoder {

    public static final @NotNull String NAME = "decoder";
    private static final int MIN_FIXED_HEADER_LENGTH = 2;

    public static @NotNull MqttDecoder create(
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttConnectRestrictions connectRestrictions) {
        switch (clientConfig.getMqttVersion()) {
            case MQTT_5_0:
                return new MqttDecoder(Mqtt5ClientMessageDecoders.INSTANCE, clientConfig, connectRestrictions);
            case MQTT_3_1_1:
                return new MqttDecoder(Mqtt3ClientMessageDecoders.INSTANCE, clientConfig, connectRestrictions);
            default:
                throw new IllegalStateException();
        }
    }

    private final @Nullable MqttMessageDecoder @NotNull [] decoders;
    private final @NotNull MqttDecoderContext context;

    MqttDecoder(
            final @Nullable MqttMessageDecoder @NotNull [] decoders,
            final @NotNull MqttClientConfig clientConfig,
            final @NotNull MqttConnectRestrictions connectRestrictions) {
        this.decoders = decoders;
        context = new MqttDecoderContext(
                connectRestrictions.getMaximumPacketSize(), connectRestrictions.getTopicAliasMaximum(),
                connectRestrictions.isRequestProblemInformation(), connectRestrictions.isRequestResponseInformation(),
                clientConfig.getAdvancedConfig().isValidatePayloadFormat(), false, false, false);
    }

    @Override
    protected void decode(
            final @NotNull ChannelHandlerContext ctx,
            final @NotNull ByteBuf in,
            final @NotNull List<Object> out) {
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

            final MqttMessageDecoder decoder;
            if ((messageType < 0) || (messageType >= decoders.length) || ((decoder = decoders[messageType]) == null)) {
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
