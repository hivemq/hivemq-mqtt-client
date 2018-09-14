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

package org.mqttbee.mqtt.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.datatypes.MqttTopicImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.handler.disconnect.MqttDisconnectUtil;
import org.mqttbee.mqtt.ioc.ConnectionScope;
import org.mqttbee.util.collections.IntMap;

import javax.inject.Inject;
import java.util.EnumSet;
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

    private final @NotNull MqttClientData clientData;
    private final @NotNull MqttMessageDecoders decoders;

    private int maximumPacketSize;
    final @NotNull EnumSet<MqttDecoderFlag> decoderFlags = EnumSet.noneOf(MqttDecoderFlag.class);
    // TODO make private when all decoder flags can be set via api
    private @Nullable IntMap<MqttTopicImpl> topicAliasMapping;

    @Inject
    MqttDecoder(final @NotNull MqttClientData clientData, final @NotNull MqttMessageDecoders decoders) {
        this.clientData = clientData;
        this.decoders = decoders;
    }

    @Override
    public void handlerAdded(final @NotNull ChannelHandlerContext ctx) {
        final MqttClientConnectionData clientConnectionData = clientData.getRawClientConnectionData();
        assert clientConnectionData != null;

        maximumPacketSize = clientConnectionData.getMaximumPacketSize();
        if (clientConnectionData.isProblemInformationRequested()) {
            decoderFlags.add(MqttDecoderFlag.PROBLEM_INFORMATION_REQUESTED);
        }
        if (clientConnectionData.isResponseInformationRequested()) {
            decoderFlags.add(MqttDecoderFlag.RESPONSE_INFORMATION_REQUESTED);
        }
        topicAliasMapping = clientConnectionData.getTopicAliasMapping();
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

            final int fixedHeaderLength = in.readerIndex() - readerIndexBeforeFixedHeader;
            final int packetSize = fixedHeaderLength + remainingLength;

            if (packetSize > maximumPacketSize) {
                throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PACKET_TOO_LARGE,
                        "incoming packet exceeded maximum packet size");
            }

            if (in.readableBytes() < remainingLength) {
                in.readerIndex(readerIndexBeforeFixedHeader);
                return;
            }

            final MqttMessageDecoder decoder = decoders.get(messageType);
            if (decoder == null) {
                throw new MqttDecoderException(
                        Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "must not receive this packet type");
            }

            out.add(decoder.decode(flags, in.readSlice(remainingLength), decoderFlags, topicAliasMapping));

        } catch (final MqttDecoderException e) {
            in.clear();
            e.setMessageType(Mqtt5MessageType.fromCode(messageType));
            MqttDisconnectUtil.disconnect(ctx.channel(), e.getReasonCode(), e);
        }
    }

}
