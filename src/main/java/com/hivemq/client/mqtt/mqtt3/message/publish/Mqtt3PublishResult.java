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

package com.hivemq.client.mqtt.mqtt3.message.publish;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Result for an {@link Mqtt3Publish MQTT 3 Publish message} sent by the client.
 * <p>
 * The result is provided if a Publish message is successfully delivered (sent or acknowledged respectively to its
 * {@link com.hivemq.client.mqtt.datatypes.MqttQos QoS} level).
 *
 * @author Silvio Giebl
 * @since 1.0
 */
@ApiStatus.NonExtendable
public interface Mqtt3PublishResult {

    /**
     * @return the Publish message this result is for.
     */
    @NotNull Mqtt3Publish getPublish();

    /**
     * @return the optional error that is present if the Publish message was not successfully delivered.
     */
    @NotNull Optional<Throwable> getError();
}
