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

package com.hivemq.client2.internal.mqtt.codec.decoder;

import com.hivemq.client2.mqtt.datatypes.MqttQos;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import static com.hivemq.client2.internal.mqtt.message.publish.MqttStatefulPublish.NO_PACKET_IDENTIFIER_QOS_0;

/**
 * Util for decoders for MQTT messages of different versions.
 *
 * @author Silvio Giebl
 */
public final class MqttMessageDecoderUtil {

    public static void checkFixedHeaderFlags(final int expected, final int actual) throws MqttDecoderException {
        if (expected != actual) {
            throw new MqttDecoderException("fixed header flags must be " + expected + " but were " + actual);
        }
    }

    public static void checkRemainingLength(final int expected, final int actual) throws MqttDecoderException {
        if (expected != actual) {
            throw new MqttDecoderException("remaining length must be " + expected + " but was " + actual);
        }
    }

    public static @NotNull MqttDecoderException remainingLengthTooShort() {
        return new MqttDecoderException("remaining length too short");
    }

    public static @NotNull MqttDecoderException malformedUTF8String(final @NotNull String name) {
        return new MqttDecoderException("malformed UTF-8 string for " + name);
    }

    public static @NotNull MqttDecoderException malformedTopic() {
        return new MqttDecoderException(Mqtt5DisconnectReasonCode.TOPIC_NAME_INVALID, "malformed topic");
    }

    public static @NotNull MqttQos decodePublishQos(final int flags, final boolean dup) throws MqttDecoderException {
        final MqttQos qos = MqttQos.fromCode((flags & 0b0110) >> 1);
        if (qos == null) {
            throw new MqttDecoderException("wrong QoS");
        }
        if ((qos == MqttQos.AT_MOST_ONCE) && dup) {
            throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "DUP flag must be 0 if QoS is 0");
        }
        return qos;
    }

    public static int decodePublishPacketIdentifier(final @NotNull MqttQos qos, final @NotNull ByteBuf in)
            throws MqttDecoderException {

        if (qos == MqttQos.AT_MOST_ONCE) {
            return NO_PACKET_IDENTIFIER_QOS_0;
        }
        if (in.readableBytes() < 2) {
            throw remainingLengthTooShort();
        }
        return in.readUnsignedShort();
    }

    private MqttMessageDecoderUtil() {}
}
