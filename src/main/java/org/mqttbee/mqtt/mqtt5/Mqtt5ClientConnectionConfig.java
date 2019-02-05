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

package org.mqttbee.mqtt.mqtt5;

import org.jetbrains.annotations.NotNull;
import org.mqttbee.annotations.DoNotImplement;
import org.mqttbee.mqtt.MqttClientConnectionConfig;
import org.mqttbee.mqtt.datatypes.MqttQos;
import org.mqttbee.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;

import java.util.Optional;

/**
 * @author Silvio Giebl
 */
@DoNotImplement
public interface Mqtt5ClientConnectionConfig extends MqttClientConnectionConfig {

    long getSessionExpiryInterval();

    @NotNull Optional<Mqtt5EnhancedAuthMechanism> getEnhancedAuthMechanism();

    @NotNull ReceiveConfig getReceiveConfig();

    @NotNull SendConfig getSendConfig();

    @DoNotImplement
    interface ReceiveConfig {

        int getReceiveMaximum();

        int getMaximumPacketSize();

        int getTopicAliasMaximum();

        boolean isProblemInformationRequested();

        boolean isResponseInformationRequested();
    }

    @DoNotImplement
    interface SendConfig {

        int getSendMaximum();

        int getSendMaximumPacketSize();

        int getSendTopicAliasMaximum();

        @NotNull MqttQos getMaximumQos();

        boolean isRetainAvailable();

        boolean isWildcardSubscriptionAvailable();

        boolean isSharedSubscriptionAvailable();

        boolean areSubscriptionIdentifiersAvailable();
    }
}
