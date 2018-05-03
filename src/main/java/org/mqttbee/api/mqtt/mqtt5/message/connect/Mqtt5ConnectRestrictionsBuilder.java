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

package org.mqttbee.api.mqtt.mqtt5.message.connect;

import com.google.common.base.Preconditions;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.message.connect.MqttConnectRestrictions;
import org.mqttbee.util.UnsignedDataTypes;

/** @author Silvio Giebl */
public class Mqtt5ConnectRestrictionsBuilder {

  private int receiveMaximum = Mqtt5ConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM;
  private int topicAliasMaximum = Mqtt5ConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM;
  private int maximumPacketSize = Mqtt5ConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;

  Mqtt5ConnectRestrictionsBuilder() {}

  @NotNull
  public Mqtt5ConnectRestrictionsBuilder withReceiveMaximum(final int receiveMaximum) {
    Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(receiveMaximum));
    this.receiveMaximum = receiveMaximum;
    return this;
  }

  @NotNull
  public Mqtt5ConnectRestrictionsBuilder withTopicAliasMaximum(final int topicAliasMaximum) {
    Preconditions.checkArgument(UnsignedDataTypes.isUnsignedShort(topicAliasMaximum));
    this.topicAliasMaximum = topicAliasMaximum;
    return this;
  }

  @NotNull
  public Mqtt5ConnectRestrictionsBuilder withMaximumPacketSize(final int maximumPacketSize) {
    Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(maximumPacketSize));
    this.maximumPacketSize = maximumPacketSize;
    return this;
  }

  @NotNull
  public Mqtt5ConnectRestrictions build() {
    return new MqttConnectRestrictions(receiveMaximum, topicAliasMaximum, maximumPacketSize);
  }
}
