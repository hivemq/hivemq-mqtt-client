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

package org.mqttbee.api.mqtt.mqtt5.advanced.qos2;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.Mqtt5ClientData;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5Publish;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubComp;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrec.Mqtt5PubRec;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelBuilder;

/**
 * Interface for providers for controlling the QoS 2 control flow of outgoing PUBLISH messages.
 *
 * @author Silvio Giebl
 */
public interface Mqtt5OutgoingQos2ControlProvider {

    /**
     * Called when a server sent a PUBREC message for a PUBLISH with QoS 2.
     * <p>
     * This method must not block and just add some properties to the outgoing PubRel message.
     *
     * @param clientData    the data of the client.
     * @param publish       the PUBLISH message with QoS 2 sent by the client.
     * @param pubRec        the PUBREC message sent by the server.
     * @param pubRelBuilder the builder for the outgoing PUBREL message.
     */
    void onPubRec(
            @NotNull Mqtt5ClientData clientData, @NotNull final Mqtt5Publish publish, @NotNull Mqtt5PubRec pubRec,
            @NotNull Mqtt5PubRelBuilder pubRelBuilder);

    /**
     * Called when a server sent a PUBREC message for a PUBLISH with QoS 2 with an Error Code.
     * <p>
     * This method must not block.
     *
     * @param clientData the data of the client.
     * @param publish    the PUBLISH message with QoS 2 sent by the client.
     * @param pubRec     the PUBREC message sent by the server.
     */
    void onPubRecError(@NotNull Mqtt5ClientData clientData, @NotNull Mqtt5Publish publish, @NotNull Mqtt5PubRec pubRec);

    /**
     * Called when a server sent a PUBCOMP message for a PUBLISH with QoS 2.
     * <p>
     * This method must not block.
     *
     * @param clientData the data of the client.
     * @param publish    the PUBLISH message with QoS 2 sent by the client.
     * @param pubComp    the PUBCOMP message sent by the server.
     */
    void onPubComp(@NotNull Mqtt5ClientData clientData, @NotNull Mqtt5Publish publish, @NotNull Mqtt5PubComp pubComp);

}
