/*
 * Copyright 2018 dc-square and the HiveMQ MQTT Client Project
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

package com.hivemq.client.internal.mqtt.ioc;

import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttSubscriptionFlowTree;
import com.hivemq.client.internal.mqtt.handler.publish.incoming.MqttSubscriptionFlows;
import dagger.Binds;
import dagger.Module;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
@Module
abstract class ClientModule {

    @Binds
    abstract @NotNull MqttSubscriptionFlows provideSubscriptionFlows(final @NotNull MqttSubscriptionFlowTree tree);
}
