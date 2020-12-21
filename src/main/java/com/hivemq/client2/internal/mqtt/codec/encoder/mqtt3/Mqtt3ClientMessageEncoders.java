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

package com.hivemq.client2.internal.mqtt.codec.encoder.mqtt3;

import com.hivemq.client2.internal.mqtt.codec.encoder.MqttMessageEncoders;
import com.hivemq.client2.internal.mqtt.codec.encoder.MqttPingReqEncoder;
import com.hivemq.client2.mqtt.mqtt3.message.Mqtt3MessageType;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Collection of encoders for MQTT 3 messages a client can send.
 *
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt3ClientMessageEncoders extends MqttMessageEncoders {

    @Inject
    Mqtt3ClientMessageEncoders(
            final @NotNull Mqtt3ConnectEncoder connectEncoder,
            final @NotNull Mqtt3PublishEncoder publishEncoder,
            final @NotNull Mqtt3PubAckEncoder pubAckEncoder,
            final @NotNull Mqtt3PubRecEncoder pubRecEncoder,
            final @NotNull Mqtt3PubRelEncoder pubRelEncoder,
            final @NotNull Mqtt3PubCompEncoder pubCompEncoder,
            final @NotNull Mqtt3SubscribeEncoder subscribeEncoder,
            final @NotNull Mqtt3UnsubscribeEncoder unsubscribeEncoder,
            final @NotNull MqttPingReqEncoder pingReqEncoder,
            final @NotNull Mqtt3DisconnectEncoder disconnectEncoder) {

        encoders[Mqtt3MessageType.CONNECT.getCode()] = connectEncoder;
        encoders[Mqtt3MessageType.PUBLISH.getCode()] = publishEncoder;
        encoders[Mqtt3MessageType.PUBACK.getCode()] = pubAckEncoder;
        encoders[Mqtt3MessageType.PUBREC.getCode()] = pubRecEncoder;
        encoders[Mqtt3MessageType.PUBREL.getCode()] = pubRelEncoder;
        encoders[Mqtt3MessageType.PUBCOMP.getCode()] = pubCompEncoder;
        encoders[Mqtt3MessageType.SUBSCRIBE.getCode()] = subscribeEncoder;
        encoders[Mqtt3MessageType.UNSUBSCRIBE.getCode()] = unsubscribeEncoder;
        encoders[Mqtt3MessageType.PINGREQ.getCode()] = pingReqEncoder;
        encoders[Mqtt3MessageType.DISCONNECT.getCode()] = disconnectEncoder;
    }
}
