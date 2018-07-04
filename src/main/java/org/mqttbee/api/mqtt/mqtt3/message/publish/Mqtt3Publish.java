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

package org.mqttbee.api.mqtt.mqtt3.message.publish;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.api.mqtt.mqtt3.message.subscribe.Mqtt3SubscribeResult;

/** MQTT 3 PUBLISH packet. */
@DoNotImplement
public interface Mqtt3Publish extends Mqtt3Message, Mqtt3SubscribeResult {

    @NotNull
    static Mqtt3PublishBuilder<Void> builder() {
        return new Mqtt3PublishBuilder<>((Function<Mqtt3Publish, Void>) null);
    }

    @NotNull
    static Mqtt3PublishBuilder<Void> extend(@NotNull final Mqtt3Publish publish) {
        return new Mqtt3PublishBuilder<>(publish);
    }

    /** @return the topic of this PUBLISH packet. */
    @NotNull
    MqttTopic getTopic();

    /** @return the optional payload of this PUBLISH packet. */
    @NotNull
    Optional<ByteBuffer> getPayload();

    /**
     * @return the payload of this PUBLISH packet as byte array. Empty byte array if the payload is
     *     null.
     */
    @NotNull
    byte[] getPayloadAsBytes();

    /** @return the QoS of this PUBLISH packet. */
    @NotNull
    MqttQoS getQos();

    /** @return whether this PUBLISH packet is a retained message. */
    boolean isRetain();

    @NotNull
    @Override
    default Mqtt3MessageType getType() {
        return Mqtt3MessageType.PUBLISH;
    }
}
