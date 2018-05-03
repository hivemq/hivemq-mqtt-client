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

package org.mqttbee.api.mqtt.mqtt3.message.unsubscribe;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.datatypes.MqttTopicFilter;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3Message;
import org.mqttbee.api.mqtt.mqtt3.message.Mqtt3MessageType;

/** MQTT 3 UNSUBSCRIBE packet. */
@DoNotImplement
public interface Mqtt3Unsubscribe extends Mqtt3Message {

  @NotNull
  static Mqtt3UnsubscribeBuilder builder() {
    return new Mqtt3UnsubscribeBuilder();
  }

  /**
   * @return the Topic Filters of this UNSUBSCRIBE packet. The list contains at least one Topic
   *     Filter.
   */
  @NotNull
  ImmutableList<? extends MqttTopicFilter> getTopicFilters();

  @NotNull
  @Override
  default Mqtt3MessageType getType() {
    return Mqtt3MessageType.UNSUBSCRIBE;
  }
}
