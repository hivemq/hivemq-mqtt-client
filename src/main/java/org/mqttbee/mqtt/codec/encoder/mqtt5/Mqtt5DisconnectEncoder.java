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

package org.mqttbee.mqtt.codec.encoder.mqtt5;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageWithUserPropertiesEncoder.Mqtt5MessageWithOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnect.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnectProperty.SERVER_REFERENCE;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnectProperty.SESSION_EXPIRY_INTERVAL;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5DisconnectEncoder
        extends Mqtt5MessageWithOmissibleReasonCodeEncoder<MqttDisconnect, Mqtt5DisconnectReasonCode> {

    private static final int FIXED_HEADER = Mqtt5MessageType.DISCONNECT.getCode() << 4;

    @Inject
    Mqtt5DisconnectEncoder() {
    }

    @Override
    protected int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    protected Mqtt5DisconnectReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

    @Override
    int additionalPropertyLength(@NotNull final MqttDisconnect message) {
        return intPropertyEncodedLength(message.getRawSessionExpiryInterval(), SESSION_EXPIRY_INTERVAL_FROM_CONNECT) +
                nullablePropertyEncodedLength(message.getRawServerReference());
    }

    @Override
    void encodeAdditionalProperties(@NotNull final MqttDisconnect message, @NotNull final ByteBuf out) {
        encodeIntProperty(SESSION_EXPIRY_INTERVAL, message.getRawSessionExpiryInterval(),
                SESSION_EXPIRY_INTERVAL_FROM_CONNECT, out);
        encodeNullableProperty(SERVER_REFERENCE, message.getRawServerReference(), out);
    }

}
