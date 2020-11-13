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

package com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos1;

import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PubAckBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for interceptors of the QoS 1 control flow for incoming Publish messages.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.OverrideOnly
public interface Mqtt5IncomingQos1Interceptor {

    /**
     * Called when a server sent a Publish message with QoS 1.
     * <p>
     * This method must not block and just add some properties to the outgoing PubAck message.
     *
     * @param clientConfig  the config of the client.
     * @param publish       the Publish message with QoS 1 sent by the server.
     * @param pubAckBuilder the builder for the outgoing PubAck message.
     */
    void onPublish(
            @NotNull Mqtt5ClientConfig clientConfig,
            @NotNull Mqtt5Publish publish,
            @NotNull Mqtt5PubAckBuilder pubAckBuilder);
}
