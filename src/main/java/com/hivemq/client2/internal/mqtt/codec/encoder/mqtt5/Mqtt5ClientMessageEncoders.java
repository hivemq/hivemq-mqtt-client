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

import com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client2.internal.mqtt.codec.encoder.MqttPingReqEncoder;
import com.hivemq.client2.mqtt.mqtt5.message.Mqtt5MessageType;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Collection of encoders for MQTT 5 messages a client can send.
 *
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ClientMessageEncoders extends MqttMessageEncoders {

    @Inject
    Mqtt5ClientMessageEncoders(
            final @NotNull Mqtt5ConnectEncoder connectEncoder,
            final @NotNull Mqtt5PublishEncoder publishEncoder,
            final @NotNull Mqtt5PubAckEncoder pubAckEncoder,
            final @NotNull Mqtt5PubRecEncoder pubRecEncoder,
            final @NotNull Mqtt5PubRelEncoder pubRelEncoder,
            final @NotNull Mqtt5PubCompEncoder pubCompEncoder,
            final @NotNull Mqtt5SubscribeEncoder subscribeEncoder,
            final @NotNull Mqtt5UnsubscribeEncoder unsubscribeEncoder,
            final @NotNull MqttPingReqEncoder pingReqEncoder,
            final @NotNull Mqtt5DisconnectEncoder disconnectEncoder,
            final @NotNull Mqtt5AuthEncoder authEncoder) {

        encoders[Mqtt5MessageType.CONNECT.getCode()] = connectEncoder;
        encoders[Mqtt5MessageType.PUBLISH.getCode()] = publishEncoder;
        encoders[Mqtt5MessageType.PUBACK.getCode()] = pubAckEncoder;
        encoders[Mqtt5MessageType.PUBREC.getCode()] = pubRecEncoder;
        encoders[Mqtt5MessageType.PUBREL.getCode()] = pubRelEncoder;
        encoders[Mqtt5MessageType.PUBCOMP.getCode()] = pubCompEncoder;
        encoders[Mqtt5MessageType.SUBSCRIBE.getCode()] = subscribeEncoder;
        encoders[Mqtt5MessageType.UNSUBSCRIBE.getCode()] = unsubscribeEncoder;
        encoders[Mqtt5MessageType.PINGREQ.getCode()] = pingReqEncoder;
        encoders[Mqtt5MessageType.DISCONNECT.getCode()] = disconnectEncoder;
        encoders[Mqtt5MessageType.AUTH.getCode()] = authEncoder;
    }
}
