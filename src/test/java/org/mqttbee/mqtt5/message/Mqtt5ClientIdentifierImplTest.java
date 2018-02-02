package org.mqttbee.mqtt5.message;

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.charset.Charset;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
@RunWith(Parameterized.class)
public class Mqtt5ClientIdentifierImplTest {

    @Parameterized.Parameters
    public static Collection<Boolean> parameters() {
        return ImmutableSet.of(false, true);
    }

    private final boolean isFromByteBuf;

    public Mqtt5ClientIdentifierImplTest(final boolean isFromByteBuf) {
        this.isFromByteBuf = isFromByteBuf;
    }

    private Mqtt5ClientIdentifierImpl from(final String string) {
        if (isFromByteBuf) {
            final ByteBuf byteBuf = Unpooled.buffer();
            final byte[] binary = string.getBytes(Charset.forName("UTF-8"));
            byteBuf.writeShort(binary.length);
            byteBuf.writeBytes(binary);
            final Mqtt5ClientIdentifierImpl mqtt5ClientIdentifier = Mqtt5ClientIdentifierImpl.from(byteBuf);
            byteBuf.release();
            return mqtt5ClientIdentifier;
        } else {
            return Mqtt5ClientIdentifierImpl.from(string);
        }
    }

    @Test
    public void test_must_be_allowed_by_server() {
        final String string = "abc123DEF";
        final Mqtt5ClientIdentifierImpl mqtt5ClientIdentifier = from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertTrue(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_be_allowed_by_server_length_23() {
        final String string = "abcdefghijklmnopqrstuvw";
        final Mqtt5ClientIdentifierImpl mqtt5ClientIdentifier = from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertTrue(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_not_be_allowed_by_server_length_24() {
        final String string = "abcdefghijklmnopqrstuvwx";
        final Mqtt5ClientIdentifierImpl mqtt5ClientIdentifier = from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_not_be_allowed_by_server_zero_length() {
        final String string = "";
        final Mqtt5ClientIdentifierImpl mqtt5ClientIdentifier = from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_not_be_allowed_by_server_character() {
        final String string = "abc123-DEF";
        final Mqtt5ClientIdentifierImpl mqtt5ClientIdentifier = from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

}