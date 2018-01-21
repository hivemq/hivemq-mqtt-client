package org.mqttbee.mqtt5.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DataTypesTest {

    private final Random random = new Random();

    @Test
    public void test_decodeVariableByteInteger_1_byte() {
        final ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 127; i++) {
            byteBuf.writeByte(i);
            assertEquals(i, Mqtt5DataTypes.decodeVariableByteInteger(byteBuf));
            byteBuf.clear();
        }
        byteBuf.release();
    }

    @Test
    public void test_decodeVariableByteInteger_2_bytes() {
        final ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 127; i++) {
            for (int j = 1; j < 127; j++) {
                byteBuf.writeByte(128 + i).writeByte(j);
                assertEquals(i + j * 128, Mqtt5DataTypes.decodeVariableByteInteger(byteBuf));
                byteBuf.clear();
            }
        }
        byteBuf.release();
    }

    @Test
    public void test_decodeVariableByteInteger_3_bytes() {
        final ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 127; i++) {
            for (int j = 0; j < 127; j++) {
                for (int k = 1; k < 127; k++) {
                    byteBuf.writeByte(128 + i).writeByte(128 + j).writeByte(k);
                    assertEquals(i + j * 128 + k * 128 * 128, Mqtt5DataTypes.decodeVariableByteInteger(byteBuf));
                    byteBuf.clear();
                }
            }
        }
        byteBuf.release();
    }

    @Test
    public void test_decodeVariableByteInteger_4_bytes() {
        final ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 127; i++) {
            for (int j = 0; j < 127; j++) {
                for (int k = 0; k < 127; k++) {
                    for (int l = 1; l < 127; l++) {
                        byteBuf.writeByte(128 + i).writeByte(128 + j).writeByte(128 + k).writeByte(l);
                        assertEquals(i + j * 128 + k * 128 * 128 + l * 128 * 128 * 128,
                                Mqtt5DataTypes.decodeVariableByteInteger(byteBuf));
                        byteBuf.clear();
                    }
                }
            }
        }
        byteBuf.release();
    }

    @Test
    public void test_decodeVariableByteInteger_not_enough_bytes() {
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0x80);
        assertEquals(Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES,
                Mqtt5DataTypes.decodeVariableByteInteger(byteBuf));
        byteBuf.release();
    }

    @Test
    public void test_decodeVariableByteInteger_too_large() {
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0xFF).writeByte(0xFF).writeByte(0xFF).writeByte(0xFF);
        assertEquals(Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_TOO_LARGE, Mqtt5DataTypes.decodeVariableByteInteger(byteBuf));
        byteBuf.release();
    }

    @Test
    public void test_decodeVariableByteInteger_not_minumum_byte() {
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0xFF).writeByte(0x00);
        assertEquals(Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES,
                Mqtt5DataTypes.decodeVariableByteInteger(byteBuf));
        byteBuf.release();
    }

    @Test
    public void test_encodeVariableByteInteger_1_byte() {
        final ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 127; i++) {
            Mqtt5DataTypes.encodeVariableByteInteger(i, byteBuf);
            assertEquals(i, byteBuf.readUnsignedByte());
            assertFalse(byteBuf.isReadable());
            byteBuf.clear();
        }
        byteBuf.release();
    }

    @Test
    public void test_encodeVariableByteInteger_2_bytes() {
        final ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 127; i++) {
            for (int j = 1; j < 127; j++) {
                Mqtt5DataTypes.encodeVariableByteInteger(i + j * 128, byteBuf);
                assertEquals(128 + i, byteBuf.readUnsignedByte());
                assertEquals(j, byteBuf.readUnsignedByte());
                assertFalse(byteBuf.isReadable());
                byteBuf.clear();
            }
        }
        byteBuf.release();
    }

    @Test
    public void test_encodeVariableByteInteger_3_bytes() {
        final ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 127; i++) {
            for (int j = 0; j < 127; j++) {
                for (int k = 1; k < 127; k++) {
                    Mqtt5DataTypes.encodeVariableByteInteger(i + j * 128 + k * 128 * 128, byteBuf);
                    assertEquals(128 + i, byteBuf.readUnsignedByte());
                    assertEquals(128 + j, byteBuf.readUnsignedByte());
                    assertEquals(k, byteBuf.readUnsignedByte());
                    assertFalse(byteBuf.isReadable());
                    byteBuf.clear();
                }
            }
        }
        byteBuf.release();
    }

    @Test
    public void test_encodeVariableByteInteger_4_bytes() {
        final ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 127; i++) {
            for (int j = 0; j < 127; j++) {
                for (int k = 0; k < 127; k++) {
                    for (int l = 1; l < 127; l++) {
                        Mqtt5DataTypes.encodeVariableByteInteger(
                                i + j * 128 + k * 128 * 128 + l * 128 * 128 * 128, byteBuf);
                        assertEquals(128 + i, byteBuf.readUnsignedByte());
                        assertEquals(128 + j, byteBuf.readUnsignedByte());
                        assertEquals(128 + k, byteBuf.readUnsignedByte());
                        assertEquals(l, byteBuf.readUnsignedByte());
                        assertFalse(byteBuf.isReadable());
                        byteBuf.clear();
                    }
                }
            }
        }
        byteBuf.release();
    }

    @Test
    public void test_isInVariableByteIntegerRange() {
        assertFalse(Mqtt5DataTypes.isInVariableByteIntegerRange(-1));
        for (int i = 0; i < 268_435_455; i++) {
            assertTrue(Mqtt5DataTypes.isInVariableByteIntegerRange(i));
        }
        assertFalse(Mqtt5DataTypes.isInVariableByteIntegerRange(268_435_456));
    }

    @Test
    public void test_encodedVariableByteIntegerLength() {
        for (int i = 0; i < 127; i++) {
            assertEquals(1, Mqtt5DataTypes.encodedVariableByteIntegerLength(i));
        }
        for (int i = 128; i < 16_383; i++) {
            assertEquals(2, Mqtt5DataTypes.encodedVariableByteIntegerLength(i));
        }
        for (int i = 16_384; i < 2_097_151; i++) {
            assertEquals(3, Mqtt5DataTypes.encodedVariableByteIntegerLength(i));
        }
        for (int i = 2_097_152; i < 268_435_455; i++) {
            assertEquals(4, Mqtt5DataTypes.encodedVariableByteIntegerLength(i));
        }
    }

    @Test
    public void test_decodeBinaryData_zero_length() {
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0).writeByte(0);
        final byte[] expected = {};
        final byte[] actual = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        assertArrayEquals(expected, actual);
        byteBuf.release();
    }

    @Test
    public void test_decodeBinaryData_full_length() {
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0xFF).writeByte(0xFF);
        final byte[] expected = new byte[65_535];
        random.nextBytes(expected);
        byteBuf.writeBytes(expected);
        final byte[] actual = Mqtt5DataTypes.decodeBinaryData(byteBuf);
        assertArrayEquals(expected, actual);
        byteBuf.release();
    }

    @Test
    public void test_decodeBinaryData_not_enough_bytes_for_length() {
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0);
        assertNull(Mqtt5DataTypes.decodeBinaryData(byteBuf));
    }

    @Test
    public void test_decodeBinaryData_not_enough_bytes_for_value() {
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0).writeByte(2).writeByte(1);
        assertNull(Mqtt5DataTypes.decodeBinaryData(byteBuf));
    }

    @Test
    public void test_encodeBinaryData_zero_length() {
        final ByteBuf byteBuf = Unpooled.buffer();
        final byte[] binary = {};
        Mqtt5DataTypes.encodeBinaryData(binary, byteBuf);
        assertEquals(0, byteBuf.readUnsignedByte());
        assertEquals(0, byteBuf.readUnsignedByte());
        assertFalse(byteBuf.isReadable());
        byteBuf.release();
    }

    @Test
    public void test_encodeBinaryData_random_full_length() {
        final ByteBuf byteBuf = Unpooled.buffer();
        final byte[] binary = new byte[65_535];
        random.nextBytes(binary);
        Mqtt5DataTypes.encodeBinaryData(binary, byteBuf);
        assertEquals(0xFF, byteBuf.readUnsignedByte());
        assertEquals(0xFF, byteBuf.readUnsignedByte());
        for (final byte b : binary) {
            assertEquals(b, byteBuf.readByte());
        }
        assertFalse(byteBuf.isReadable());
        byteBuf.release();
    }

    @Test
    public void test_isInBinaryDataLength() {
        final byte[] binary = new byte[65_535];
        random.nextBytes(binary);
        assertTrue(Mqtt5DataTypes.isInBinaryDataRange(binary));
        final byte[] binary2 = new byte[65_536];
        random.nextBytes(binary2);
        assertFalse(Mqtt5DataTypes.isInBinaryDataRange(binary2));
    }

    @Test
    public void test_encodedBinaryDataLength() {
        final byte[] binary = new byte[random.nextInt(65_535)];
        random.nextBytes(binary);
        assertEquals(2 + binary.length, Mqtt5DataTypes.encodedBinaryDataLength(binary));
    }

}