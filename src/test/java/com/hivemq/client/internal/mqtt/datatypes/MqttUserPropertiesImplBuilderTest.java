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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Michael Walter
 */
class MqttUserPropertiesImplBuilderTest {

    @Test
    void addAll_error_when_list_is_null() {
        assertThrows(
                NullPointerException.class,
                () -> Mqtt5UserProperties.builder().addAll((ArrayList<Mqtt5UserProperty>) null));
    }

    @Test
    void addAll_error_when_one_element_is_custom_implementation() {

        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.of("key", "value");

        final List<Mqtt5UserProperty> userProperties = new ArrayList<>();
        userProperties.add(userProperty);
        userProperties.add(new CustomUserProperty());

        final Mqtt5UserPropertiesBuilder userPropertiesBuilder = Mqtt5UserProperties.builder();

        assertThrows(IllegalArgumentException.class, () -> userPropertiesBuilder.addAll(userProperties));

        final Mqtt5UserProperties properties = userPropertiesBuilder.build();

        assertTrue(properties.asList().isEmpty());
    }

    @Test
    void addAll_empty_list_allowed() {

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(new ArrayList<>()).build();

        assertTrue(properties.asList().isEmpty());
    }

    @Test
    void addAll_empty_stream_allowed() {

        final List<Mqtt5UserProperty> userProperties = new ArrayList<>();

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(userProperties.stream()).build();

        assertTrue(properties.asList().isEmpty());
    }

    @Test
    void addAll_empty_array_allowed() {

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(new Mqtt5UserProperty[0]).build();

        assertTrue(properties.asList().isEmpty());
    }

    @Test
    void addAll_empty_set_allowed() {

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(new HashSet<>()).build();

        assertTrue(properties.asList().isEmpty());
    }

    @Test
    void addAll_correct_use_and_correct_order_list() {

        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.of("key", "value");
        final Mqtt5UserProperty userProperty2 = Mqtt5UserProperty.of("key2", "value2");
        final Mqtt5UserProperty userProperty3 = Mqtt5UserProperty.of("key3", "value3");

        final List<Mqtt5UserProperty> userProperties = new ArrayList<>();
        userProperties.add(userProperty);
        userProperties.add(userProperty2);
        userProperties.add(userProperty3);

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(userProperties).build();

        final List<@NotNull ? extends Mqtt5UserProperty> mqtt5UserProperties = properties.asList();

        compareProperties(userProperties, mqtt5UserProperties);
    }

    @Test
    void addAll_correct_use_and_correct_order_map() {

        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.of("key", "value");
        final Mqtt5UserProperty userProperty2 = Mqtt5UserProperty.of("key2", "value2");
        final Mqtt5UserProperty userProperty3 = Mqtt5UserProperty.of("key3", "value3");

        final Map<String, Mqtt5UserProperty> userProperties = new HashMap<>();
        userProperties.put("1", userProperty);
        userProperties.put("2", userProperty2);
        userProperties.put("3", userProperty3);

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(userProperties.values()).build();

        final List<@NotNull ? extends Mqtt5UserProperty> mqtt5UserProperties = properties.asList();

        compareProperties(userProperties.values(), mqtt5UserProperties);
    }

    @Test
    void addAll_correct_use_and_correct_order_set() {

        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.of("key", "value");
        final Mqtt5UserProperty userProperty2 = Mqtt5UserProperty.of("key2", "value2");
        final Mqtt5UserProperty userProperty3 = Mqtt5UserProperty.of("key3", "value3");

        final Set<Mqtt5UserProperty> userProperties = new HashSet<>();
        userProperties.add(userProperty);
        userProperties.add(userProperty2);
        userProperties.add(userProperty3);

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(userProperties).build();

        final List<@NotNull ? extends Mqtt5UserProperty> mqtt5UserProperties = properties.asList();

        compareProperties(userProperties, mqtt5UserProperties);
    }

    @Test
    void addAll_correct_use_and_correct_order_array() {

        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.of("key", "value");
        final Mqtt5UserProperty userProperty2 = Mqtt5UserProperty.of("key2", "value2");
        final Mqtt5UserProperty userProperty3 = Mqtt5UserProperty.of("key3", "value3");

        final Mqtt5UserProperty[] userProperties = new Mqtt5UserProperty[]{userProperty, userProperty2, userProperty3};

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(userProperties).build();

        final List<@NotNull ? extends Mqtt5UserProperty> mqtt5UserProperties = properties.asList();

        compareProperties(userProperties, mqtt5UserProperties);
    }

    @Test
    void addAll_correct_use_and_correct_order_stream() {

        final Mqtt5UserProperty userProperty = Mqtt5UserProperty.of("key", "value");
        final Mqtt5UserProperty userProperty2 = Mqtt5UserProperty.of("key2", "value2");
        final Mqtt5UserProperty userProperty3 = Mqtt5UserProperty.of("key3", "value3");

        final List<Mqtt5UserProperty> userProperties = new ArrayList<>();
        userProperties.add(userProperty);
        userProperties.add(userProperty2);
        userProperties.add(userProperty3);

        final Mqtt5UserProperties properties = Mqtt5UserProperties.builder().addAll(userProperties.stream()).build();

        final List<@NotNull ? extends Mqtt5UserProperty> mqtt5UserProperties = properties.asList();

        compareProperties(userProperties, mqtt5UserProperties);
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

    void compareProperties(
            final @NotNull Mqtt5UserProperty[] expected, final List<@NotNull ? extends Mqtt5UserProperty> actual) {

        assertEquals(expected.length, actual.size());

        for (int i = 0; i < expected.length; i++) {
            final Mqtt5UserProperty expectedSubscription = expected[i];
            final Mqtt5UserProperty actualSubscription = actual.get(i);

            assertEquals(expectedSubscription, actualSubscription);
        }
    }

    void compareProperties(
            final @NotNull Collection<Mqtt5UserProperty> expected,
            final List<@NotNull ? extends Mqtt5UserProperty> actual) {
        compareProperties(expected.toArray(new Mqtt5UserProperty[0]), actual);
    }
}