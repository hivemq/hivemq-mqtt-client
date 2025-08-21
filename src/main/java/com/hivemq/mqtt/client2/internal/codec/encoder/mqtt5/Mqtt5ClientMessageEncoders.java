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

package com.hivemq.mqtt.client2.internal.codec.encoder.mqtt5;

import com.hivemq.mqtt.client2.internal.codec.encoder.MqttMessageEncoder;
import com.hivemq.mqtt.client2.internal.codec.encoder.MqttPingReqEncoder;
import com.hivemq.mqtt.client2.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Collection of encoders for MQTT 5 messages a client can send.
 *
 * @author Silvio Giebl
 */
public class Mqtt5ClientMessageEncoders {

    public static final @Nullable MqttMessageEncoder<?> @NotNull [] INSTANCE = new MqttMessageEncoder[16];

    static {
        INSTANCE[Mqtt5MessageType.CONNECT.getCode()] = Mqtt5ConnectEncoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PUBLISH.getCode()] = Mqtt5PublishEncoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PUBACK.getCode()] = Mqtt5PubAckEncoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PUBREC.getCode()] = Mqtt5PubRecEncoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PUBREL.getCode()] = Mqtt5PubRelEncoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PUBCOMP.getCode()] = Mqtt5PubCompEncoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.SUBSCRIBE.getCode()] = Mqtt5SubscribeEncoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.UNSUBSCRIBE.getCode()] = Mqtt5UnsubscribeEncoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.PINGREQ.getCode()] = MqttPingReqEncoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.DISCONNECT.getCode()] = Mqtt5DisconnectEncoder.INSTANCE;
        INSTANCE[Mqtt5MessageType.AUTH.getCode()] = Mqtt5AuthEncoder.INSTANCE;
    }
}
