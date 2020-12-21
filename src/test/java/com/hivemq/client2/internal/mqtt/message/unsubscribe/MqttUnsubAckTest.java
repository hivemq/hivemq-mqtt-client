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

package com.hivemq.client2.internal.mqtt.message.unsubscribe;

import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertyImpl;
import com.hivemq.client2.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client2.internal.util.collections.ImmutableList;
import com.hivemq.client2.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAck;
import com.hivemq.client2.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubAckReasonCode;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.hivemq.client2.internal.mqtt.datatypes.MqttUserPropertiesImpl.NO_USER_PROPERTIES;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author David Katz
 * @author Silvio Giebl
 */
class MqttUnsubAckTest {

    @Test
    void equals() {
        EqualsVerifier.forClass(MqttUnsubAck.class)
                .withIgnoredAnnotations(NotNull.class) // EqualsVerifier thinks @NotNull Optional is @NotNull
                .withNonnullFields("reasonCodes", "userProperties")
                .withIgnoredFields("packetIdentifier")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    private final @NotNull MqttUtf8StringImpl reasonString = MqttUtf8StringImpl.of("reasonString");
    private final @NotNull ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes =
            ImmutableList.of(Mqtt5UnsubAckReasonCode.SUCCESS);

    @Test
    void constructor_reasonCodesEmpty_throwsException() {
        final ImmutableList<Mqtt5UnsubAckReasonCode> emptyReasonCodes = ImmutableList.of();
        final Mqtt5UnsubAck mqtt5UnsubAck = new MqttUnsubAck(1, emptyReasonCodes, reasonString, NO_USER_PROPERTIES);
        assertEquals(emptyReasonCodes, mqtt5UnsubAck.getReasonCodes());
    }

    @Test
    void constructor_reasonCodesSingle() {
        final ImmutableList<Mqtt5UnsubAckReasonCode> singleReasonCodes =
                ImmutableList.of(Mqtt5UnsubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);
        final Mqtt5UnsubAck mqtt5UnsubAck = new MqttUnsubAck(1, singleReasonCodes, reasonString, NO_USER_PROPERTIES);
        assertEquals(singleReasonCodes, mqtt5UnsubAck.getReasonCodes());
    }

    @Test
    void constructor_reasonCodesMultiple() {
        final ImmutableList<Mqtt5UnsubAckReasonCode> multipleReasonCodes =
                ImmutableList.of(Mqtt5UnsubAckReasonCode.NO_SUBSCRIPTIONS_EXISTED,
                        Mqtt5UnsubAckReasonCode.NOT_AUTHORIZED);
        final Mqtt5UnsubAck mqtt5UnsubAck = new MqttUnsubAck(1, multipleReasonCodes, reasonString, NO_USER_PROPERTIES);
        assertEquals(multipleReasonCodes, mqtt5UnsubAck.getReasonCodes());
    }

    @Test
    void constructor_reasonStringNull() {
        final Mqtt5UnsubAck mqtt5UnsubAck = new MqttUnsubAck(1, reasonCodes, null, NO_USER_PROPERTIES);
        assertFalse(mqtt5UnsubAck.getReasonString().isPresent());
    }

    @Test
    void constructor_reasonString() {
        final Mqtt5UnsubAck mqtt5UnsubAck = new MqttUnsubAck(1, reasonCodes, reasonString, NO_USER_PROPERTIES);
        assertTrue(mqtt5UnsubAck.getReasonString().isPresent());
        assertEquals(reasonString, mqtt5UnsubAck.getReasonString().get());
    }

    @Test
    void constructor_userPropertiesEmpty() {
        final MqttUserPropertiesImpl emptyUserProperties = NO_USER_PROPERTIES;
        final Mqtt5UnsubAck mqtt5UnsubAck = new MqttUnsubAck(1, reasonCodes, reasonString, emptyUserProperties);
        assertEquals(emptyUserProperties, mqtt5UnsubAck.getUserProperties());
    }

    @Test
    void constructor_userPropertiesMultiple() {
        final MqttUtf8StringImpl name1 = MqttUtf8StringImpl.of("name1");
        final MqttUtf8StringImpl name2 = MqttUtf8StringImpl.of("name2");
        final MqttUtf8StringImpl name3 = MqttUtf8StringImpl.of("name3");
        final MqttUtf8StringImpl value = MqttUtf8StringImpl.of("value");

        final MqttUserPropertyImpl userProperty1 = new MqttUserPropertyImpl(name1, value);
        final MqttUserPropertyImpl userProperty2 = new MqttUserPropertyImpl(name2, value);
        final MqttUserPropertyImpl userProperty3 = new MqttUserPropertyImpl(name3, value);
        final MqttUserPropertiesImpl multipleUserProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2, userProperty3));

        final Mqtt5UnsubAck mqtt5UnsubAck = new MqttUnsubAck(1, reasonCodes, reasonString, multipleUserProperties);
        assertEquals(multipleUserProperties, mqtt5UnsubAck.getUserProperties());
    }
}
