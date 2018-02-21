package org.mqttbee.mqtt.datatypes;

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
public class MqttClientIdentifierImplTest {

    @Parameterized.Parameters
    public static Collection<Boolean> parameters() {
        return ImmutableSet.of(false, true);
    }

    private final boolean isFromByteBuf;

    public MqttClientIdentifierImplTest(final boolean isFromByteBuf) {
        this.isFromByteBuf = isFromByteBuf;
    }

    private MqttClientIdentifierImpl from(final String string) {
        if (isFromByteBuf) {
            final ByteBuf byteBuf = Unpooled.buffer();
            final byte[] binary = string.getBytes(Charset.forName("UTF-8"));
            byteBuf.writeShort(binary.length);
            byteBuf.writeBytes(binary);
            final MqttClientIdentifierImpl mqtt5ClientIdentifier = MqttClientIdentifierImpl.from(byteBuf);
            byteBuf.release();
            return mqtt5ClientIdentifier;
        } else {
            return MqttClientIdentifierImpl.from(string);
        }
    }

    @Test
    public void test_must_be_allowed_by_server() {
        final String string = "abc123DEF";
        final MqttClientIdentifierImpl mqtt5ClientIdentifier = from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertTrue(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_be_allowed_by_server_length_23() {
        final String string = "abcdefghijklmnopqrstuvw";
        final MqttClientIdentifierImpl mqtt5ClientIdentifier = from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertTrue(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_not_be_allowed_by_server_length_24() {
        final String string = "abcdefghijklmnopqrstuvwx";
        final MqttClientIdentifierImpl mqtt5ClientIdentifier = from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_not_be_allowed_by_server_zero_length() {
        final String string = "";
        final MqttClientIdentifierImpl mqtt5ClientIdentifier = from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

    @Test
    public void test_must_not_be_allowed_by_server_character() {
        final String string = "abc123-DEF";
        final MqttClientIdentifierImpl mqtt5ClientIdentifier = from(string);
        assertNotNull(mqtt5ClientIdentifier);
        assertFalse(mqtt5ClientIdentifier.mustBeAllowedByServer());
    }

}