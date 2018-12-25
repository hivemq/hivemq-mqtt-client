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

import dagger.Component;
import org.jetbrains.annotations.NotNull;
import org.mqttbee.mqtt.netty.NettyEventLoopProvider;
import org.mqttbee.mqtt.netty.NettyModule;

import javax.inject.Singleton;

/**
 * Singleton component for all clients. It exists the whole application lifetime.
 *
 * @author Silvio Giebl
 */
@Component(modules = {NettyModule.class})
@Singleton
public interface SingletonComponent {

    @NotNull SingletonComponent INSTANCE = DaggerSingletonComponent.create();

    @NotNull ClientComponent.Builder clientComponentBuilder();

    @NotNull NettyEventLoopProvider nettyEventLoopProvider();
}
