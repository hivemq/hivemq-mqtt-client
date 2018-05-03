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

package org.mqttbee.mqtt.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import java.util.LinkedList;
import java.util.List;
import org.mqttbee.annotations.NotNull;

/** @author Silvio Giebl */
public class NettyThreadLocals {

  private static final List<ThreadLocal<?>> threadLocalMessageEncoders = new LinkedList<>();

  public static void register(@NotNull final ThreadLocal<?> threadLocalMessageEncoder) {
    threadLocalMessageEncoders.add(threadLocalMessageEncoder);
  }

  private static void clear() {
    for (final ThreadLocal<?> threadLocalMessageEncoder : threadLocalMessageEncoders) {
      threadLocalMessageEncoder.remove();
    }
  }

  public static void clear(@NotNull final EventLoopGroup eventLoopGroup) {
    for (final EventExecutor eventExecutor : eventLoopGroup) {
      eventExecutor.execute(NettyThreadLocals::clear);
    }
  }
}
