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
import org.jetbrains.annotations.NotNull;
import org.mqttbee.mqtt.MqttClientData;
import org.mqttbee.mqtt.handler.publish.MqttIncomingQosHandler;
import org.mqttbee.mqtt.handler.publish.MqttOutgoingQosHandler;
import org.mqttbee.mqtt.handler.subscribe.MqttSubscriptionHandler;

/**
 * Component for a single client. A new one is created for each client and exists as long as the client object exists.
 *
 * @author Silvio Giebl
 */
@Subcomponent(modules = {ClientModule.class})
@ClientScope
public interface ClientComponent {

    @NotNull ConnectionComponent.Builder connectionComponentBuilder();

    @NotNull MqttSubscriptionHandler subscriptionHandler();

    @NotNull MqttIncomingQosHandler incomingQosHandler();

    @NotNull MqttOutgoingQosHandler outgoingQosHandler();

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        @NotNull Builder clientData(@NotNull MqttClientData clientData);

        @NotNull ClientComponent build();

    }

}
