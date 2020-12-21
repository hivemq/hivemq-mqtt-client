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

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5;

import com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import com.hivemq.client2.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.hivemq.client2.internal.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect.DEFAULT_REASON_CODE;
import static com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
import static com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnectProperty.SERVER_REFERENCE;
import static com.hivemq.client2.internal.mqtt.message.disconnect.MqttDisconnectProperty.SESSION_EXPIRY_INTERVAL;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5DisconnectEncoder extends
        Mqtt5MessageWithUserPropertiesEncoder.WithReason.WithOmissibleCode<MqttDisconnect, Mqtt5DisconnectReasonCode> {

    private static final int FIXED_HEADER = Mqtt5MessageType.DISCONNECT.getCode() << 4;

    @Inject
    Mqtt5DisconnectEncoder() {}

    @Override
    int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    @NotNull Mqtt5DisconnectReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

    @Override
    int additionalPropertyLength(final @NotNull MqttDisconnect message) {
        return intPropertyEncodedLength(message.getRawSessionExpiryInterval(), SESSION_EXPIRY_INTERVAL_FROM_CONNECT) +
                nullablePropertyEncodedLength(message.getRawServerReference());
    }

    @Override
    void encodeAdditionalProperties(final @NotNull MqttDisconnect message, final @NotNull ByteBuf out) {
        encodeIntProperty(SESSION_EXPIRY_INTERVAL, message.getRawSessionExpiryInterval(),
                SESSION_EXPIRY_INTERVAL_FROM_CONNECT, out);
        encodeNullableProperty(SERVER_REFERENCE, message.getRawServerReference(), out);
    }
}
