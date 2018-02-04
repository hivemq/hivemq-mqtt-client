package org.mqttbee.mqtt5.message.unsuback;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mqttbee.api.mqtt5.message.Mqtt5UnsubAck;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserPropertyImpl;

import static org.junit.Assert.*;
import static org.mqttbee.mqtt5.message.Mqtt5UserPropertiesImpl.NO_USER_PROPERTIES;

/**
 * @author David Katz
 */

public class Mqtt5UnsubAckTest {

    private final Mqtt5UTF8StringImpl reasonString = Mqtt5UTF8StringImpl.from("reasonString");
    private final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes =
            ImmutableList.of(Mqtt5UnsubAckReasonCode.SUCCESS);

    @Test
    public void constructor_reasonCodesEmpty_throwsException() {
        final ImmutableList<Mqtt5UnsubAckReasonCode> emptyReasonCodes = ImmutableList.of();
        final Mqtt5UnsubAck mqtt5UnsubAck =
                new Mqtt5UnsubAckImpl(1, emptyReasonCodes, reasonString, NO_USER_PROPERTIES);
        assertEquals(emptyReasonCodes, mqtt5UnsubAck.getReasonCodes());
    }

    @Test
    public void constructor_reasonCodesSingle() {
        final ImmutableList<Mqtt5UnsubAckReasonCode> singleReasonCodes =
                ImmutableList.of(Mqtt5UnsubAckReasonCode.IMPLEMENTATION_SPECIFIC_ERROR);
        final Mqtt5UnsubAck mqtt5UnsubAck =
                new Mqtt5UnsubAckImpl(1, singleReasonCodes, reasonString, NO_USER_PROPERTIES);
        assertEquals(singleReasonCodes, mqtt5UnsubAck.getReasonCodes());
    }

    @Test
    public void constructor_reasonCodesMultiple() {
        final ImmutableList<Mqtt5UnsubAckReasonCode> multipleReasonCodes = ImmutableList
                .of(Mqtt5UnsubAckReasonCode.NO_SUBSCRIPTIONS_EXISTED, Mqtt5UnsubAckReasonCode.NOT_AUTHORIZED);
        final Mqtt5UnsubAck mqtt5UnsubAck =
                new Mqtt5UnsubAckImpl(1, multipleReasonCodes, reasonString, NO_USER_PROPERTIES);
        assertEquals(multipleReasonCodes, mqtt5UnsubAck.getReasonCodes());
    }

    @Test
    public void constructor_reasonStringNull() {
        final Mqtt5UnsubAck mqtt5UnsubAck = new Mqtt5UnsubAckImpl(1, reasonCodes, null, NO_USER_PROPERTIES);
        assertFalse(mqtt5UnsubAck.getReasonString().isPresent());
    }

    @Test
    public void constructor_reasonString() {
        final Mqtt5UnsubAck mqtt5UnsubAck = new Mqtt5UnsubAckImpl(1, reasonCodes, reasonString, NO_USER_PROPERTIES);
        assertTrue(mqtt5UnsubAck.getReasonString().isPresent());
        assertEquals(reasonString, mqtt5UnsubAck.getReasonString().get());
    }

    @Test
    public void constructor_userPropertiesEmpty() {
        final Mqtt5UserPropertiesImpl emptyUserProperties = NO_USER_PROPERTIES;
        final Mqtt5UnsubAck mqtt5UnsubAck = new Mqtt5UnsubAckImpl(1, reasonCodes, reasonString, emptyUserProperties);
        assertEquals(emptyUserProperties, mqtt5UnsubAck.getUserProperties());
    }

    @Test
    public void constructor_userPropertiesMultiple() {
        final Mqtt5UTF8StringImpl name1 = Mqtt5UTF8StringImpl.from("name1");
        final Mqtt5UTF8StringImpl name2 = Mqtt5UTF8StringImpl.from("name2");
        final Mqtt5UTF8StringImpl name3 = Mqtt5UTF8StringImpl.from("name3");
        final Mqtt5UTF8StringImpl value = Mqtt5UTF8StringImpl.from("value");
        assertNotNull(name1);
        assertNotNull(name2);
        assertNotNull(name3);
        assertNotNull(value);

        final Mqtt5UserPropertyImpl userProperty1 = new Mqtt5UserPropertyImpl(name1, value);
        final Mqtt5UserPropertyImpl userProperty2 = new Mqtt5UserPropertyImpl(name2, value);
        final Mqtt5UserPropertyImpl userProperty3 = new Mqtt5UserPropertyImpl(name3, value);
        final Mqtt5UserPropertiesImpl multipleUserProperties =
                Mqtt5UserPropertiesImpl.of(ImmutableList.of(userProperty1, userProperty2, userProperty3));

        final Mqtt5UnsubAck mqtt5UnsubAck = new Mqtt5UnsubAckImpl(1, reasonCodes, reasonString, multipleUserProperties);
        assertEquals(multipleUserProperties, mqtt5UnsubAck.getUserProperties());
    }
}
