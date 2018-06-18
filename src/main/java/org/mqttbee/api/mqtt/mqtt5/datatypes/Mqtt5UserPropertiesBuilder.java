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
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.FluentBuilder;
import org.mqttbee.util.MustNotBeImplementedUtil;

import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public class Mqtt5UserPropertiesBuilder<P> extends FluentBuilder<Mqtt5UserProperties, P> {

    private final ImmutableList.Builder<MqttUserPropertyImpl> listBuilder;

    public Mqtt5UserPropertiesBuilder(@Nullable final Function<Mqtt5UserProperties, P> parentConsumer) {
        super(parentConsumer);
        listBuilder = ImmutableList.builder();
    }

    Mqtt5UserPropertiesBuilder(@NotNull final Mqtt5UserProperties userProperties) {
        super(null);
        final ImmutableList<MqttUserPropertyImpl> list = MqttBuilderUtil.userProperties(userProperties).asList();
        listBuilder = ImmutableList.builderWithExpectedSize(list.size() + 1);
        listBuilder.addAll(list);
    }

    @NotNull
    public Mqtt5UserPropertiesBuilder<P> add(@NotNull final String name, @NotNull final String value) {
        listBuilder.add(MqttBuilderUtil.userProperty(name, value));
        return this;
    }

    @NotNull
    public Mqtt5UserPropertiesBuilder<P> add(@NotNull final MqttUTF8String name, @NotNull final MqttUTF8String value) {
        listBuilder.add(MqttBuilderUtil.userProperty(name, value));
        return this;
    }

    @NotNull
    public Mqtt5UserPropertiesBuilder<P> add(@NotNull final Mqtt5UserProperty userProperty) {
        listBuilder.add(MustNotBeImplementedUtil.checkNotImplemented(userProperty, MqttUserPropertyImpl.class));
        return this;
    }

    @NotNull
    @Override
    public Mqtt5UserProperties build() {
        return MqttUserPropertiesImpl.build(listBuilder);
    }

}
