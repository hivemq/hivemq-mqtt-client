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

package com.hivemq.client.mqtt.mqtt5.advanced.interceptor.qos2;

import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRecBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRel;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for interceptors of the QoS 2 control flow of incoming Publish messages.
 *
 * @author Silvio Giebl
 * @since 1.0
 */
public interface Mqtt5IncomingQos2Interceptor {

    /**
     * Called when a server sent a Publish message with QoS 2.
     * <p>
     * This method must not block and just add some properties to the outgoing PubRec message.
     *
     * @param clientConfig  the config of the client.
     * @param publish       the Publish message with QoS 2 sent by the server.
     * @param pubRecBuilder the builder for the outgoing PubRec message.
     */
    void onPublish(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Publish publish,
            @NotNull Mqtt5PubRecBuilder pubRecBuilder);

    /**
     * Called when a server sent a PubRel message for a Publish message with QoS 2.
     * <p>
     * This method must not block and just add some properties to the outgoing PubComp message.
     *
     * @param clientConfig   the config of the client.
     * @param pubRel         the PubRel message sent by the server.
     * @param pubCompBuilder the builder for the outgoing PubComp message.
     */
    void onPubRel(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5PubRel pubRel,
            @NotNull Mqtt5PubCompBuilder pubCompBuilder);
}
