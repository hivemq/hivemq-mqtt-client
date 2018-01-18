package org.mqttbee.mqtt5.message.unsuback;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mqttbee.api.mqtt5.message.Mqtt5UnsubAck;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;

import static org.junit.Assert.*;
import static org.mqttbee.mqtt5.message.Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES;

/**
 * @author David Katz
 */

public class Mqtt5UnsubAckTest {

    private final Mqtt5UTF8String reasonString = Mqtt5UTF8String.from("reasonString");
    private final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes =
            ImmutableList.of(Mqtt5UnsubAckReasonCode.SUCCESS);

    @Test
    public void constructor_reasonCodesEmpty_throwsException() {
        final ImmutableList<Mqtt5UnsubAckReasonCode> emptyReasonCodes = ImmutableList.of();
        final Mqtt5UnsubAck mqtt5UnsubAck =
                new Mqtt5UnsubAckImpl(emptyReasonCodes, reasonString, DEFAULT_NO_USER_PROPERTIES);
        assertEquals(emptyReasonCodes, mqtt5UnsubAck.getReasonCodes());
    }

    @Test
    public void constructor_reasonCodesSingle() {
        final ImmutableList<Mqtt5UnsubAckReasonCode> singleReasonCodes =
                ImmutableList.of(Mqtt5UnsubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);
        final Mqtt5UnsubAck mqtt5UnsubAck =
                new Mqtt5UnsubAckImpl(singleReasonCodes, reasonString, DEFAULT_NO_USER_PROPERTIES);
        assertEquals(singleReasonCodes, mqtt5UnsubAck.getReasonCodes());
    }

    @Test
    public void constructor_reasonCodesMultiple() {
        final ImmutableList<Mqtt5UnsubAckReasonCode> multipleReasonCodes = ImmutableList.of(
                Mqtt5UnsubAckReasonCode.NO_SUBSCRIPTIONS_EXISTED, Mqtt5UnsubAckReasonCode.NOT_AUTHORIZED);
        final Mqtt5UnsubAck mqtt5UnsubAck =
                new Mqtt5UnsubAckImpl(multipleReasonCodes, reasonString, DEFAULT_NO_USER_PROPERTIES);
        assertEquals(multipleReasonCodes, mqtt5UnsubAck.getReasonCodes());
    }

    @Test
    public void constructor_reasonStringNull() {
        final Mqtt5UnsubAck mqtt5UnsubAck = new Mqtt5UnsubAckImpl(reasonCodes, null, DEFAULT_NO_USER_PROPERTIES);
        assertFalse(mqtt5UnsubAck.getReasonString().isPresent());
    }

    @Test
    public void constructor_reasonString() {
        final Mqtt5UnsubAck mqtt5UnsubAck =
                new Mqtt5UnsubAckImpl(reasonCodes, reasonString, DEFAULT_NO_USER_PROPERTIES);
        assertTrue(mqtt5UnsubAck.getReasonString().isPresent());
        assertEquals(reasonString, mqtt5UnsubAck.getReasonString().get());
    }

    @Test
    public void constructor_userPropertiesEmpty() {
        final ImmutableList<Mqtt5UserProperty> emptyUserProperties = DEFAULT_NO_USER_PROPERTIES;
        final Mqtt5UnsubAck mqtt5UnsubAck = new Mqtt5UnsubAckImpl(reasonCodes, reasonString, emptyUserProperties);
        assertEquals(emptyUserProperties, mqtt5UnsubAck.getUserProperties());
    }

    @Test
    public void constructor_userPropertiesMultiple() {
        final Mqtt5UTF8String name1 = Mqtt5UTF8String.from("name1");
        final Mqtt5UTF8String name2 = Mqtt5UTF8String.from("name2");
        final Mqtt5UTF8String name3 = Mqtt5UTF8String.from("name3");
        final Mqtt5UTF8String value = Mqtt5UTF8String.from("value");
        assertNotNull(name1);
        assertNotNull(name2);
        assertNotNull(name3);
        assertNotNull(value);

        final Mqtt5UserProperty userProperty1 = new Mqtt5UserProperty(name1, value);
        final Mqtt5UserProperty userProperty2 = new Mqtt5UserProperty(name2, value);
        final Mqtt5UserProperty userProperty3 = new Mqtt5UserProperty(name3, value);
        final ImmutableList<Mqtt5UserProperty> multipleUserProperties =
                ImmutableList.of(userProperty1, userProperty2, userProperty3);

        final Mqtt5UnsubAck mqtt5UnsubAck = new Mqtt5UnsubAckImpl(reasonCodes, reasonString, multipleUserProperties);
        assertEquals(multipleUserProperties, mqtt5UnsubAck.getUserProperties());
    }
}
