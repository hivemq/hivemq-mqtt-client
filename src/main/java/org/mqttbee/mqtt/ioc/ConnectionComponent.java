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

package org.mqttbee.mqtt.ioc;

import dagger.BindsInstance;
import dagger.Subcomponent;
import io.netty.bootstrap.Bootstrap;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt.codec.MqttCodecModule;
import org.mqttbee.mqtt.message.connect.MqttConnect;
import org.mqttbee.rx.SingleFlow;

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
        @NotNull Builder connAckFlow(@NotNull SingleFlow<Mqtt5ConnAck> connAckFlow);

        @NotNull ConnectionComponent build();
    }
}
