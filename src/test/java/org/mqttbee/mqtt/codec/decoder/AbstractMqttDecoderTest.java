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

package org.mqttbee.mqtt.codec.decoder;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mqttbee.annotations.NotNull;

/** @author Silvio Giebl */
public class AbstractMqttDecoderTest {

  private final MqttMessageDecoders decoders;

  protected EmbeddedChannel channel;

  public AbstractMqttDecoderTest(@NotNull final MqttMessageDecoders decoders) {
    this.decoders = decoders;
  }

  @BeforeEach
  protected void setUp() {
    createChannel();
  }

  @AfterEach
  protected void tearDown() {
    channel.close();
  }

  protected void createChannel() {
    channel = new EmbeddedChannel(new MqttDecoder(decoders));
  }

  public static MqttPingRespDecoder createPingRespDecoder() {
    return new MqttPingRespDecoder();
  }
}
