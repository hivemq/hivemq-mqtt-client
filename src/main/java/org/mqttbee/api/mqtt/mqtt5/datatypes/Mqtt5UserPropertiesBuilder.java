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

package org.mqttbee.api.mqtt.mqtt5.datatypes;

import com.google.common.collect.ImmutableList;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.MustNotBeImplementedUtil;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UserPropertiesBuilder {

    private ImmutableList.Builder<MqttUserPropertyImpl> listBuilder;

    Mqtt5UserPropertiesBuilder() {
    }

    Mqtt5UserPropertiesBuilder(@NotNull final Mqtt5UserProperties userProperties) {
        final MqttUserPropertiesImpl userPropertiesImpl = MqttBuilderUtil.userProperties(userProperties);
        listBuilder = ImmutableList.builder();
        listBuilder.addAll(userPropertiesImpl.asList());
    }

    @NotNull
    public Mqtt5UserPropertiesBuilder add(@NotNull final Mqtt5UserProperty userProperty) {
        if (listBuilder == null) {
            listBuilder = ImmutableList.builder();
        }
        listBuilder.add(MustNotBeImplementedUtil.checkNotImplemented(userProperty, MqttUserPropertyImpl.class));
        return this;
    }

    @NotNull
    public Mqtt5UserProperties build() {
        return MqttUserPropertiesImpl.build(listBuilder);
    }

}
