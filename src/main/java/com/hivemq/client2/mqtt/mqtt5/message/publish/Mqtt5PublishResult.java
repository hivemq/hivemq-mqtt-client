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

package com.hivemq.client2.mqtt.mqtt5.message.publish;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Result for an {@link Mqtt5Publish MQTT 5 Publish message} sent by the client.
 * <p>
 * The result is provided if a Publish message is successfully delivered (sent or acknowledged respectively to its
 * {@link com.hivemq.client2.mqtt.datatypes.MqttQos QoS} level).
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt5PublishResult {

    /**
     * @return the Publish message this result is for.
     */
    @NotNull Mqtt5Publish getPublish();

    /**
     * @return the optional error that is present if the Publish message was not successfully delivered.
     */
    @NotNull Optional<Throwable> getError();

    /**
     * Result for a {@link Mqtt5Publish MQTT 5 Publish message} with {@link com.hivemq.client2.mqtt.datatypes.MqttQos#AT_LEAST_ONCE
     * QoS level 1} sent by the client.
     * <p>
     * This result additionally provides the {@link Mqtt5PubAck PubAck message} that acknowledged the Publish message.
     */
    @ApiStatus.NonExtendable
    interface Mqtt5Qos1Result extends Mqtt5PublishResult {

        /**
         * @return the PubAck message that acknowledged the Publish message.
         */
        @NotNull Mqtt5PubAck getPubAck();
    }

    /**
     * Result for a {@link Mqtt5Publish MQTT 5 Publish message} with {@link com.hivemq.client2.mqtt.datatypes.MqttQos#EXACTLY_ONCE
     * QoS level 2} sent by the client.
     * <p>
     * This result additionally provides the {@link Mqtt5PubRec PubRec message} that acknowledged the Publish message.
     */
    @ApiStatus.NonExtendable
    interface Mqtt5Qos2Result extends Mqtt5PublishResult {

        /**
         * @return the PubRec message that acknowledged the Publish message.
         */
        @NotNull Mqtt5PubRec getPubRec();
    }

    /**
     * Result for a {@link Mqtt5Publish MQTT 5 Publish message} with {@link com.hivemq.client2.mqtt.datatypes.MqttQos#EXACTLY_ONCE
     * QoS level 2} sent by the client.
     * <p>
     * This result additionally provides the {@link Mqtt5PubRec PubRec message}, {@link Mqtt5PubRel PubRel message} and
     * {@link Mqtt5PubComp PubComp message} that acknowledged the Publish message.
     * <p>
     * By default just a {@link Mqtt5Qos2Result} is provided as a result for a Publish message with QoS level 2.
     */
    @ApiStatus.NonExtendable
    interface Mqtt5Qos2CompleteResult extends Mqtt5Qos2Result {

        /**
         * @return the PubRel message that acknowledged the PubRec message.
         */
        @NotNull Mqtt5PubRel getPubRel();

        /**
         * @return the PubComp message that acknowledged the PubRel message.
         */
        @NotNull Mqtt5PubComp getPubComp();
    }
}
