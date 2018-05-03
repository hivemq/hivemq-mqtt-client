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

package org.mqttbee.mqtt.handler.auth;

import io.reactivex.CompletableEmitter;
import org.mqttbee.annotations.NotNull;

/**
 * Event that is fired when the user triggers reauth.
 *
 * @author Silvio Giebl
 */
public class MqttReAuthEvent {

  private final CompletableEmitter reAuthEmitter;

  public MqttReAuthEvent(@NotNull final CompletableEmitter reAuthEmitter) {
    this.reAuthEmitter = reAuthEmitter;
  }

  @NotNull
  public CompletableEmitter getReAuthEmitter() {
    return reAuthEmitter;
  }
}
