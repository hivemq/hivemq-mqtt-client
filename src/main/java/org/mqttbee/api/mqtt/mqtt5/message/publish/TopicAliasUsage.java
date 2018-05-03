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

/**
 * The handling for using a topic alias.
 *
 * @author Silvio Giebl
 */
public enum TopicAliasUsage {

  /** Indicates that an outgoing PUBLISH packet must not use a topic alias. */
  MUST_NOT,
  /** Indicates that an outgoing PUBLISH packet may use a topic alias. */
  MAY,
  /**
   * Indicates that an outgoing PUBLISH packet may use a topic alias and also may overwrite an
   * existing topic alias mapping.
   */
  MAY_OVERWRITE,
  /** Indicates that an incoming PUBLISH packet does not have a topic alias. */
  HAS_NOT,
  /** Indicates that an incoming PUBLISH packet has a topic alias. */
  HAS
}
