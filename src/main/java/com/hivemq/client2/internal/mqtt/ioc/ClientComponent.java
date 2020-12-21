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

import com.hivemq.client2.internal.mqtt.MqttClientConfig;
import com.hivemq.client2.internal.mqtt.handler.publish.incoming.MqttIncomingQosHandler;
import com.hivemq.client2.internal.mqtt.handler.publish.outgoing.MqttOutgoingQosHandler;
import com.hivemq.client2.internal.mqtt.handler.subscribe.MqttSubscriptionHandler;
import dagger.BindsInstance;
import dagger.Subcomponent;
import org.jetbrains.annotations.NotNull;

/**
 * Component for a single client. A new one is created for each client and exists as long as the client object exists.
 *
 * @author Silvio Giebl
 */
@Subcomponent
@ClientScope
public interface ClientComponent {

    ConnectionComponent.@NotNull Builder connectionComponentBuilder();

    @NotNull MqttSubscriptionHandler subscriptionHandler();

    @NotNull MqttIncomingQosHandler incomingQosHandler();

    @NotNull MqttOutgoingQosHandler outgoingQosHandler();

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        @NotNull Builder clientConfig(@NotNull MqttClientConfig clientConfig);

        @NotNull ClientComponent build();
    }
}
