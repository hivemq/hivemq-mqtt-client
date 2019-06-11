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

package util.implementations;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3SubscriptionBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * @author Michael Walter
 */
public class CustomMqtt3Subscription implements Mqtt3Subscription {

    @Override
    public @NotNull MqttTopicFilter getTopicFilter() {
        return null;
    }

    @Override
    public @NotNull MqttQos getQos() {
        return null;
    }

    @Override
    public @NotNull Mqtt3SubscriptionBuilder extend() {
        return null;
    }
}

