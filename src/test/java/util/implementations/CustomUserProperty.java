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

import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;

/**
 * @author Michael Walter
 */
public class CustomUserProperty implements Mqtt5UserProperty {

    @Override
    public @NotNull MqttUtf8String getName() {
        return null;
    }

    @Override
    public @NotNull MqttUtf8String getValue() {
        return null;
    }

    @Override
    public int compareTo(@NotNull Mqtt5UserProperty o) {
        return 0;
    }
}
