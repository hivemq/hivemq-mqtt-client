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

package org.mqttbee.api.mqtt.mqtt5.datatypes;

import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;

/**
 * MQTT User Property according to the MQTT 5 specification.
 *
 * <p>A User Property consists of a name and value UTF-8 encoded String Pair.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5UserProperty {

  /**
   * Creates an User Property of the given name and value.
   *
   * @param name the name of the User Property.
   * @param value the value of the User Property.
   * @return the created User Property.
   */
  @NotNull
  static Mqtt5UserProperty of(@NotNull final String name, @NotNull final String value) {
    return MqttUserPropertyImpl.of(MqttBuilderUtil.string(name), MqttBuilderUtil.string(value));
  }

  /**
   * Creates an User Property of the given name and value.
   *
   * @param name the name of the User Property.
   * @param value the value of the User Property.
   * @return the created User Property.
   */
  @NotNull
  static Mqtt5UserProperty of(
      @NotNull final MqttUTF8String name, @NotNull final MqttUTF8String value) {
    return MqttUserPropertyImpl.of(MqttBuilderUtil.string(name), MqttBuilderUtil.string(value));
  }

  /** @return the name of this User Property. */
  @NotNull
  MqttUTF8String getName();

  /** @return the value of this User Property. */
  @NotNull
  MqttUTF8String getValue();
}
