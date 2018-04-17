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

package org.mqttbee.mqtt5.persistence;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.ioc.ChannelScope;
import org.mqttbee.mqtt5.persistence.memory.IncomingQoSFlowMemoryPersistence;
import org.mqttbee.mqtt5.persistence.memory.OutgoingQoSFlowMemoryPersistence;

/**
 * @author Silvio Giebl
 */
@Module
public class Mqtt5PersistenceModule {

    @Provides
    @ChannelScope
    static OutgoingQoSFlowPersistence provideOutgoingQoSFlowPersistence(
            @NotNull final Lazy<OutgoingQoSFlowMemoryPersistence> memoryPersistence) {

        return memoryPersistence.get(); // TODO file persistence
    }

    @Provides
    @ChannelScope
    static IncomingQoSFlowPersistence provideIncomingQoSFlowPersistence(
            @NotNull final Lazy<IncomingQoSFlowMemoryPersistence> memoryPersistence) {

        return memoryPersistence.get(); // TODO file persistence
    }

}
