package org.mqttbee.mqtt5.message;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ClientIdentifierTest {

    @Test
    public void test_must_be_allowed_by_server() {
        final String string = "abc123DEF";
        final Mqtt5ClientIdentifier mqtt5ClientIdentifier = Mqtt5ClientIdentifier.from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertTrue(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_be_allowed_by_server_length_23() {
        final String string = "abcdefghijklmnopqrstuvw";
        final Mqtt5ClientIdentifier mqtt5ClientIdentifier = Mqtt5ClientIdentifier.from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertTrue(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_not_be_allowed_by_server_length_24() {
        final String string = "abcdefghijklmnopqrstuvwx";
        final Mqtt5ClientIdentifier mqtt5ClientIdentifier = Mqtt5ClientIdentifier.from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_not_be_allowed_by_server_zero_length() {
        final String string = "";
        final Mqtt5ClientIdentifier mqtt5ClientIdentifier = Mqtt5ClientIdentifier.from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_not_be_allowed_by_server_character() {
        final String string = "abc123-DEF";
        final Mqtt5ClientIdentifier mqtt5ClientIdentifier = Mqtt5ClientIdentifier.from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

}