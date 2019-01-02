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

package org.mqttbee.mqtt.mqtt5.message.publish;

/**
 * The handling for using a topic alias.
 *
 * @author Silvio Giebl
 * @author Christian Hoff
 */
public enum TopicAliasUsage {

    /**
     * Indicates that a PUBLISH packet (incoming or outgoing) uses a topic alias. In case of an outgoing PUBLISH packet
     * and if all topic aliases are in use, it will override an existing topic alias.
     *
     * @see org.mqttbee.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckRestrictions#getTopicAliasMaximum()
     */
    YES,
    /**
     * Indicates that a PUBLISH packet (incoming or outgoing) does not use a topic alias.
     */
    NO,
    /**
     * Indicates that an outgoing PUBLISH packet will use a topic alias, if there are some unused topic aliases
     * available.
     *
     * @see org.mqttbee.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckRestrictions#getTopicAliasMaximum()
     */
    IF_AVAILABLE

}
