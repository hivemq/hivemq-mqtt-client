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

package org.mqttbee.api.mqtt.mqtt5.message.publish;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.datatypes.MqttTopic;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5Message;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.subscribe.Mqtt5SubscribeResult;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * MQTT 5 PUBLISH packet.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5Publish extends Mqtt5Message, Mqtt5SubscribeResult {

    /**
     * The default handling for using a topic alias.
     */
    @NotNull
    TopicAliasUsage DEFAULT_TOPIC_ALIAS_USAGE = TopicAliasUsage.MUST_NOT;

    @NotNull
    static Mqtt5PublishBuilder builder() {
        return new Mqtt5PublishBuilder();
    }

    @NotNull
    static Mqtt5PublishBuilder extend(@NotNull final Mqtt5Publish publish) {
        return new Mqtt5PublishBuilder(publish);
    }

    /**
     * @return the topic of this PUBLISH packet.
     */
    @NotNull
    MqttTopic getTopic();

    /**
     * @return the optional payload of this PUBLISH packet.
     */
    @NotNull
    Optional<ByteBuffer> getPayload();

    /**
     * @return the QoS of this PUBLISH packet.
     */
    @NotNull
    MqttQoS getQos();

    /**
     * @return whether this PUBLISH packet is a retained message.
     */
    boolean isRetain();

    /**
     * @return the optional message expiry interval of this PUBLISH packet.
     */
    @NotNull
    Optional<Long> getMessageExpiryInterval();

    /**
     * @return the optional payload format indicator of this PUBLISH packet.
     */
    @NotNull
    Optional<Mqtt5PayloadFormatIndicator> getPayloadFormatIndicator();

    /**
     * @return the optional content type of this PUBLISH packet.
     */
    @NotNull
    Optional<MqttUTF8String> getContentType();

    /**
     * @return the optional response topic of this PUBLISH packet.
     */
    @NotNull
    Optional<MqttTopic> getResponseTopic();

    /**
     * @return the optional correlation data of this PUBLISH packet.
     */
    @NotNull
    Optional<ByteBuffer> getCorrelationData();

    /**
     * @return the handling for using a topic alias.
     */
    @NotNull
    TopicAliasUsage getTopicAliasUsage();

    /**
     * @return the optional user properties of this PUBLISH packet.
     */
    @NotNull
    Mqtt5UserProperties getUserProperties();

    @NotNull
    @Override
    default Mqtt5MessageType getType() {
        return Mqtt5MessageType.PUBLISH;
    }

}
