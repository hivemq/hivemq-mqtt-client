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

package com.hivemq.client2.mqtt.mqtt5.advanced.interceptor.qos2;

import com.hivemq.client2.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client2.mqtt.mqtt5.message.publish.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for interceptors of the QoS 2 control flow of outgoing Publish messages.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.OverrideOnly
public interface Mqtt5OutboundQos2Interceptor {

    /**
     * Called when a server sent a PubRec message for a Publish message with QoS 2.
     * <p>
     * This method must not block and just add some properties to the outgoing PubRel message.
     *
     * @param clientConfig  the config of the client.
     * @param publish       the Publish message with QoS 2 sent by the client.
     * @param pubRec        the PubRec message sent by the server.
     * @param pubRelBuilder the builder for the outgoing PubRel message.
     */
    void onPubRec(
            @NotNull Mqtt5ClientConfig clientConfig,
            @NotNull Mqtt5Publish publish,
            @NotNull Mqtt5PubRec pubRec,
            @NotNull Mqtt5PubRelBuilder pubRelBuilder);

    /**
     * Called when a server sent a PubRec message for a Publish message with QoS 2 with an Error Code.
     * <p>
     * This method must not block.
     *
     * @param clientConfig the config of the client.
     * @param publish      the Publish message with QoS 2 sent by the client.
     * @param pubRec       the PubRec message sent by the server.
     */
    void onPubRecError(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Publish publish, @NotNull Mqtt5PubRec pubRec);

    /**
     * Called when a server sent a PubComp message for a Publish message with QoS 2.
     * <p>
     * This method must not block.
     *
     * @param clientConfig the config of the client.
     * @param pubRel       the PubRel message sent by the client.
     * @param pubComp      the PubComp message sent by the server.
     */
    void onPubComp(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5PubRel pubRel, @NotNull Mqtt5PubComp pubComp);
}
