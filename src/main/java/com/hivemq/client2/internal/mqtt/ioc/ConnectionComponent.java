/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
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
 */

package com.hivemq.client2.internal.mqtt.ioc;

import com.hivemq.client2.internal.mqtt.codec.MqttCodecModule;
import com.hivemq.client2.internal.mqtt.handler.connect.MqttConnAckFlow;
import com.hivemq.client2.internal.mqtt.message.connect.MqttConnect;
import dagger.BindsInstance;
import dagger.Subcomponent;
import io.netty.bootstrap.Bootstrap;
import org.jetbrains.annotations.NotNull;

/**
 * Component for a single client connection. A new one is created for each new client connection (also for reconnects).
 *
 * @author Silvio Giebl
 */
@Subcomponent(modules = {ConnectionModule.class, MqttCodecModule.class})
@ConnectionScope
public interface ConnectionComponent {

    @NotNull Bootstrap bootstrap();

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        @NotNull Builder connect(@NotNull MqttConnect connect);

        @BindsInstance
        @NotNull Builder connAckFlow(@NotNull MqttConnAckFlow connAckFlow);

        @NotNull ConnectionComponent build();
    }
}
