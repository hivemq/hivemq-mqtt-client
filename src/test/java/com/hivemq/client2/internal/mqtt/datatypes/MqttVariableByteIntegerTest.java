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

package com.hivemq.client2.internal.mqtt.datatypes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Silvio Giebl
 */
class MqttVariableByteIntegerTest {

    @Test
    public void test_decodeVariableByteInteger_1_byte() {
        final ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 127; i++) {
            byteBuf.writeByte(i);
            assertEquals(i, MqttVariableByteInteger.decode(byteBuf));
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
                assertEquals(i + j * 128, MqttVariableByteInteger.decode(byteBuf));
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
                    assertEquals(i + j * 128 + k * 128 * 128, MqttVariableByteInteger.decode(byteBuf));
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
                        assertEquals(
                                i + j * 128 + k * 128 * 128 + l * 128 * 128 * 128,
                                MqttVariableByteInteger.decode(byteBuf));
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
        assertEquals(MqttVariableByteInteger.NOT_ENOUGH_BYTES, MqttVariableByteInteger.decode(byteBuf));
        byteBuf.release();
    }

    @Test
    public void test_decodeVariableByteInteger_too_large() {
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0xFF).writeByte(0xFF).writeByte(0xFF).writeByte(0xFF);
        assertEquals(MqttVariableByteInteger.TOO_LARGE, MqttVariableByteInteger.decode(byteBuf));
        byteBuf.release();
    }

    @Test
    public void test_decodeVariableByteInteger_not_minimum_byte() {
        final ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0xFF).writeByte(0x00);
        assertEquals(MqttVariableByteInteger.NOT_MINIMUM_BYTES, MqttVariableByteInteger.decode(byteBuf));
        byteBuf.release();
    }

    @Test
    public void test_encodeVariableByteInteger_1_byte() {
        final ByteBuf byteBuf = Unpooled.buffer();
        for (int i = 0; i < 127; i++) {
            MqttVariableByteInteger.encode(i, byteBuf);
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
                MqttVariableByteInteger.encode(i + j * 128, byteBuf);
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
                    MqttVariableByteInteger.encode(i + j * 128 + k * 128 * 128, byteBuf);
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
                        MqttVariableByteInteger.encode(i + j * 128 + k * 128 * 128 + l * 128 * 128 * 128, byteBuf);
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
        assertFalse(MqttVariableByteInteger.isInRange(-1));
        for (int i = 0; i < 268_435_455; i++) {
            assertTrue(MqttVariableByteInteger.isInRange(i));
        }
        assertFalse(MqttVariableByteInteger.isInRange(268_435_456));
    }

    @Test
    public void test_encodedVariableByteIntegerLength() {
        for (int i = 0; i < 127; i++) {
            assertEquals(1, MqttVariableByteInteger.encodedLength(i));
        }
        for (int i = 128; i < 16_383; i++) {
            assertEquals(2, MqttVariableByteInteger.encodedLength(i));
        }
        for (int i = 16_384; i < 2_097_151; i++) {
            assertEquals(3, MqttVariableByteInteger.encodedLength(i));
        }
        for (int i = 2_097_152; i < 268_435_455; i++) {
            assertEquals(4, MqttVariableByteInteger.encodedLength(i));
        }
    }

}