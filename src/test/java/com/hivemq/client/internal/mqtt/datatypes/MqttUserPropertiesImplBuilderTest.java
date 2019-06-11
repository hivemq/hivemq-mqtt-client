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

package com.hivemq.client.internal.mqtt.datatypes;

import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import util.implementations.CustomUserProperty;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Michael Walter
 */
class MqttUserPropertiesImplBuilderTest {

    @Test
    void addAll_error_when_list_is_null() {
        assertThrows(NullPointerException.class, () -> Mqtt5UserProperties.builder().addAll(null));
    }

    @Test
    void addAll_error_when_one_element_is_custom_implementation() {

        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.of("key", "value");

        final List<Mqtt5UserProperty> userProperties = new ArrayList<>();
        userProperties.add(userProperty);
        userProperties.add(new CustomUserProperty());

        final Mqtt5UserPropertiesBuilder userPropertiesBuilder = Mqtt5UserProperties.builder();

        assertThrows(IllegalArgumentException.class, () -> {
            userPropertiesBuilder.addAll(userProperties);
        });

        final Mqtt5UserProperties properties = userPropertiesBuilder.build();

        assertTrue(properties.asList().isEmpty());
    }

    @Test
    void addAll_empty_list_allowed() {

        final List<Mqtt5UserProperty> userProperties = new ArrayList<>();

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(userProperties).build();

        assertTrue(properties.asList().isEmpty());
    }

    @Test
    void addAll_correct_use_and_correct_order() {

        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.of("key", "value");
        final Mqtt5UserProperty userProperty2 = Mqtt5UserProperty.of("key2", "value2");
        final Mqtt5UserProperty userProperty3 = Mqtt5UserProperty.of("key3", "value3");

        final List<Mqtt5UserProperty> userProperties = new ArrayList<>();
        userProperties.add(userProperty);
        userProperties.add(userProperty2);
        userProperties.add(userProperty3);

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(userProperties).build();

        final List<@NotNull ? extends Mqtt5UserProperty> mqtt5UserProperties = properties.asList();

        assertEquals(3, mqtt5UserProperties.size());
        assertEquals(userProperty, mqtt5UserProperties.get(0));
        assertEquals(userProperty2, mqtt5UserProperties.get(1));
        assertEquals(userProperty3, mqtt5UserProperties.get(2));
    }

    @Test
    void addAll_correct_use_and_correct_order_immutable_list() {

        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.of("key", "value");
        final Mqtt5UserProperty userProperty2 = Mqtt5UserProperty.of("key2", "value2");
        final Mqtt5UserProperty userProperty3 = Mqtt5UserProperty.of("key3", "value3");

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder()
                .addAll(ImmutableList.of(userProperty, userProperty2, userProperty3))
                .build();

        final List<@NotNull ? extends Mqtt5UserProperty> mqtt5UserProperties = properties.asList();

        assertEquals(3, mqtt5UserProperties.size());
        assertEquals(userProperty, mqtt5UserProperties.get(0));
        assertEquals(userProperty2, mqtt5UserProperties.get(1));
        assertEquals(userProperty3, mqtt5UserProperties.get(2));
    }
}