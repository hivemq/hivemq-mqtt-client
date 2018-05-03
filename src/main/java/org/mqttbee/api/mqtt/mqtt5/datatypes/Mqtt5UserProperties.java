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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * Collection of {@link Mqtt5UserProperty User Properties}.
 *
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5UserProperties {

  /** @return the empty collection of User Properties. */
  @NotNull
  static Mqtt5UserProperties of() {
    return MqttUserPropertiesImpl.NO_USER_PROPERTIES;
  }

  /**
   * Creates a collection of User Properties of the given User Properties.
   *
   * @param userProperties the User Properties.
   * @return the created collection of User Properties.
   */
  @NotNull
  static Mqtt5UserProperties of(@NotNull final Mqtt5UserProperty... userProperties) {
    Preconditions.checkNotNull(userProperties);

    final ImmutableList.Builder<MqttUserPropertyImpl> builder =
        ImmutableList.builderWithExpectedSize(userProperties.length);
    for (final Mqtt5UserProperty userProperty : userProperties) {
      builder.add(
          MustNotBeImplementedUtil.checkNotImplemented(userProperty, MqttUserPropertyImpl.class));
    }
    return MqttUserPropertiesImpl.of(builder.build());
  }

  @NotNull
  static Mqtt5UserPropertiesBuilder builder() {
    return new Mqtt5UserPropertiesBuilder();
  }

  @NotNull
  static Mqtt5UserPropertiesBuilder extend(@NotNull final Mqtt5UserProperties userProperties) {
    return new Mqtt5UserPropertiesBuilder(userProperties);
  }

  /** @return the User Properties as an immutable list. */
  @NotNull
  ImmutableList<? extends Mqtt5UserProperty> asList();
}
