/*
 *
 * *
 *  * Copyright 2018 The MQTT Bee project.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.mqttbee.api.mqtt.mqtt5.advanced.qos1;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.puback.Mqtt5PubAckBuilder;

/**
 * Interface for providers for controlling the QoS 1 control flow for incoming PUBLISH messages.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5IncomingQoS1ControlProvider {

    /**
     * Called when a server sent a PUBLISH message with QoS 1.
     * <p>
     * This method must not block and just add some properties to the outgoing PUBACK message.
     *
     * @param publish       the PUBLISH message with QoS 1 sent by the server.
     * @param pubAckBuilder the builder for the outgoing PUBACK message.
     */
    void onPublish(@NotNull Mqtt5Publish publish, @NotNull Mqtt5PubAckBuilder pubAckBuilder);

}
