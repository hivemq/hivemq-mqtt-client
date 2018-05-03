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

package org.mqttbee.mqtt.datatypes;

import io.netty.buffer.ByteBuf;
import javax.annotation.concurrent.Immutable;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperty;

/**
 * @author Silvio Giebl
 * @see Mqtt5UserProperty
 */
@Immutable
public class MqttUserPropertyImpl implements Mqtt5UserProperty {

  /**
   * Creates an User Property of the given name and value.
   *
   * @param name the name of the User Property.
   * @param value the value of the User Property.
   * @return the created User Property.
   */
  public static MqttUserPropertyImpl of(
      @NotNull final MqttUTF8StringImpl name, @NotNull final MqttUTF8StringImpl value) {

    return new MqttUserPropertyImpl(name, value);
  }

  /**
   * Validates and decodes a User Property from the given byte buffer at the current reader index.
   *
   * @param in the byte buffer to decode from.
   * @return the decoded User Property or null if the name and/or value are not valid UTF-8 encoded
   *     Strings.
   */
  @Nullable
  public static MqttUserPropertyImpl decode(@NotNull final ByteBuf in) {
    final MqttUTF8StringImpl name = MqttUTF8StringImpl.from(in);
    if (name == null) {
      return null;
    }
    final MqttUTF8StringImpl value = MqttUTF8StringImpl.from(in);
    if (value == null) {
      return null;
    }
    return new MqttUserPropertyImpl(name, value);
  }

  private final MqttUTF8StringImpl name;
  private final MqttUTF8StringImpl value;

  public MqttUserPropertyImpl(
      @NotNull final MqttUTF8StringImpl name, @NotNull final MqttUTF8StringImpl value) {
    this.name = name;
    this.value = value;
  }

  @NotNull
  @Override
  public MqttUTF8StringImpl getName() {
    return name;
  }

  @NotNull
  @Override
  public MqttUTF8StringImpl getValue() {
    return value;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MqttUserPropertyImpl)) {
      return false;
    }
    final MqttUserPropertyImpl that = (MqttUserPropertyImpl) o;
    return name.equals(that.name) && value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return 31 * name.hashCode() + value.hashCode();
  }
}
