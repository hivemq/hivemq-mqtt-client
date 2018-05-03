/*
 * Copyright 2018 The MQTT Bee project
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

package org.mqttbee.mqtt.datatypes;

import static org.junit.Assert.*;

import com.google.common.base.Charsets;
import com.google.common.base.Utf8;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.junit.Test;
import org.mqttbee.api.mqtt.exceptions.MqttBinaryDataExceededException;

/** @author Silvio Giebl */
public class MqttUTF8StringImplTest {

  @Test
  public void must_not_null_character() {
    assertNull(MqttUTF8StringImpl.from("abc\0def"));

    assertNull(MqttUTF8StringImpl.from(new byte[] {'a', 'b', 'c', '\0', 'd', 'e', 'f'}));
  }

  @Test
  public void must_not_utf16_surrogates() {
    for (char c = '\uD800'; c <= '\uDFFF'; c++) {
      assertNull(MqttUTF8StringImpl.from("abc" + c + "def"));
    }

    for (int b = 0xA0; b <= 0xBF; b++) {
      for (int b2 = 0; b2 < 0xFF; b2++) {
        assertNull(
            MqttUTF8StringImpl.from(
                new byte[] {'a', 'b', 'c', (byte) 0xED, (byte) b, (byte) b2, 'd', 'e', 'f'}));
      }
    }
  }

  @Test
  public void should_not_control_characters() {
    for (char c = '\u0001'; c <= '\u001F'; c++) {
      final MqttUTF8StringImpl string = MqttUTF8StringImpl.from("abc" + c + "def");
      assertNotNull(string);
      assertTrue(string.containsShouldNotCharacters());

      final MqttUTF8StringImpl binary =
          MqttUTF8StringImpl.from(new byte[] {'a', 'b', 'c', (byte) c, 'd', 'e', 'f'});
      assertNotNull(binary);
      assertTrue(binary.containsShouldNotCharacters());
    }
    for (char c = '\u007F'; c <= '\u009F'; c++) {
      final MqttUTF8StringImpl string = MqttUTF8StringImpl.from("abc" + c + "def");
      assertNotNull(string);
      assertTrue(string.containsShouldNotCharacters());
    }

    {
      final MqttUTF8StringImpl binary =
          MqttUTF8StringImpl.from(new byte[] {'a', 'b', 'c', 0x7F, 'd', 'e', 'f'});
      assertNotNull(binary);
      assertTrue(binary.containsShouldNotCharacters());
    }
    for (int b = 0x80; b <= 0x9F; b++) {
      final MqttUTF8StringImpl binary =
          MqttUTF8StringImpl.from(new byte[] {'a', 'b', 'c', (byte) 0xC2, (byte) b, 'd', 'e', 'f'});
      assertNotNull(binary);
      assertTrue(binary.containsShouldNotCharacters());
    }
  }

  @Test
  public void should_not_non_characters() {
    for (int c = 0xFFFE; c <= 0x10_FFFE; c += 0x1_0000) {
      for (int i = 0; i < 2; i++) {
        final String nonCharacterString = String.valueOf(Character.toChars(c + i));
        final MqttUTF8StringImpl string =
            MqttUTF8StringImpl.from("abc" + nonCharacterString + "def");
        assertNotNull(string);
        assertTrue(string.containsShouldNotCharacters());

        final byte[] nonCharacterBinary = nonCharacterString.getBytes(Charsets.UTF_8);
        final byte[] binary = new byte[6 + nonCharacterBinary.length];
        binary[0] = 'a';
        binary[1] = 'b';
        binary[2] = 'c';
        System.arraycopy(nonCharacterBinary, 0, binary, 3, nonCharacterBinary.length);
        binary[3 + nonCharacterBinary.length] = 'd';
        binary[3 + nonCharacterBinary.length + 1] = 'e';
        binary[3 + nonCharacterBinary.length + 2] = 'f';
        final MqttUTF8StringImpl binaryString = MqttUTF8StringImpl.from(binary);
        assertNotNull(binaryString);
        assertTrue(binaryString.containsShouldNotCharacters());
      }
    }
  }

  @Test
  public void no_should_not_characters() {
    final MqttUTF8StringImpl string = MqttUTF8StringImpl.from("abcdef");
    assertNotNull(string);
    assertFalse(string.containsShouldNotCharacters());

    final MqttUTF8StringImpl binary =
        MqttUTF8StringImpl.from(new byte[] {'a', 'b', 'c', 'd', 'e', 'f'});
    assertNotNull(binary);
    assertFalse(binary.containsShouldNotCharacters());
  }

  @Test
  public void zero_width_no_break_space() {
    final MqttUTF8StringImpl string = MqttUTF8StringImpl.from("abc" + '\uFEFF' + "def");
    assertNotNull(string);
    assertTrue(string.toString().contains("\uFEFF"));

    final byte[] binary =
        new byte[] {'a', 'b', 'c', (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'd', 'e', 'f'};
    final MqttUTF8StringImpl binaryString = MqttUTF8StringImpl.from(binary);
    assertNotNull(binaryString);
    assertArrayEquals(binaryString.toBinary(), binary);
    assertTrue(binaryString.toString().contains("\uFEFF"));
  }

  @Test
  public void from_byteBuf() {
    final String string = "abcdef";

    final ByteBuf byteBuf = Unpooled.buffer();
    MqttBinaryData.encode(string.getBytes(Charset.forName("UTF-8")), byteBuf);
    final MqttUTF8StringImpl mqtt5UTF8String = MqttUTF8StringImpl.from(byteBuf);
    byteBuf.release();

    assertNotNull(mqtt5UTF8String);
    assertEquals(string, mqtt5UTF8String.toString());
  }

  @Test
  public void to_byteBuf() {
    final String string = "abcdef";
    final byte[] expected = {0, 6, 'a', 'b', 'c', 'd', 'e', 'f'};

    final MqttUTF8StringImpl mqtt5UTF8String = MqttUTF8StringImpl.from(string);
    assertNotNull(mqtt5UTF8String);

    final ByteBuf byteBuf = Unpooled.buffer();
    mqtt5UTF8String.to(byteBuf);
    final byte[] actual = new byte[byteBuf.readableBytes()];
    byteBuf.readBytes(actual);
    byteBuf.release();

    assertArrayEquals(expected, actual);
  }

  @Test
  public void specification_example() {
    final String string = "A\uD869\uDED4";
    final byte[] expected = {0x41, (byte) 0xF0, (byte) 0xAA, (byte) 0x9B, (byte) 0x94};
    final MqttUTF8StringImpl mqtt5UTF8String = MqttUTF8StringImpl.from(string);
    assertNotNull(mqtt5UTF8String);
    assertArrayEquals(expected, mqtt5UTF8String.toBinary());
    assertEquals(2 + mqtt5UTF8String.toBinary().length, mqtt5UTF8String.encodedLength());
  }

  @Test
  public void encodedLength() {
    final MqttUTF8StringImpl string = MqttUTF8StringImpl.from("abcdef");
    assertNotNull(string);
    assertEquals(2 + Utf8.encodedLength("abcdef"), string.encodedLength());

    final byte[] binary = new byte[] {'a', 'b', 'c', 'd', 'e', 'f'};
    final MqttUTF8StringImpl binaryString = MqttUTF8StringImpl.from(binary);
    assertNotNull(binaryString);
    assertEquals(2 + binary.length, binaryString.encodedLength());
  }

  @Test(expected = MqttBinaryDataExceededException.class)
  public void encodedLength_too_long() {
    final byte[] binary = new byte[65_535 + 1];
    Arrays.fill(binary, (byte) 1);
    final MqttUTF8StringImpl binaryString = MqttUTF8StringImpl.from(binary);
    assertNotNull(binaryString);
    binaryString.encodedLength();
  }

  @Test
  @SuppressWarnings("all")
  public void equals() {
    assertTrue(MqttUTF8StringImpl.from("test").equals(MqttUTF8StringImpl.from("test")));
    assertTrue(
        MqttUTF8StringImpl.from("test")
            .equals(MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'})));
    assertTrue(
        MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'})
            .equals(MqttUTF8StringImpl.from("test")));
    assertTrue(
        MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'})
            .equals(MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'})));

    assertFalse(MqttUTF8StringImpl.from("test").equals(MqttUTF8StringImpl.from("test2")));
    assertFalse(
        MqttUTF8StringImpl.from("test")
            .equals(MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't', '2'})));
    assertFalse(
        MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'})
            .equals(MqttUTF8StringImpl.from("test2")));
    assertFalse(
        MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'})
            .equals(MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't', '2'})));

    assertFalse(MqttUTF8StringImpl.from("test").equals(null));
    assertFalse(MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'}).equals(null));
  }

  @Test
  @SuppressWarnings("all")
  public void equals_same() {
    final MqttUTF8StringImpl string = MqttUTF8StringImpl.from("test");
    final MqttUTF8StringImpl binary = MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'});

    assertTrue(string.equals(string));
    assertTrue(binary.equals(binary));
  }

  @Test
  @SuppressWarnings("all")
  public void equals_converted() {
    final MqttUTF8StringImpl stringAndBinary = MqttUTF8StringImpl.from("test");
    stringAndBinary.toBinary();

    assertTrue(stringAndBinary.equals(MqttUTF8StringImpl.from("test")));
    assertTrue(stringAndBinary.equals(MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'})));

    assertTrue(MqttUTF8StringImpl.from("test").equals(stringAndBinary));
    assertTrue(MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'}).equals(stringAndBinary));
  }

  @Test
  public void hashCode_same_as_string() {
    final MqttUTF8StringImpl string = MqttUTF8StringImpl.from("test");
    final MqttUTF8StringImpl binary = MqttUTF8StringImpl.from(new byte[] {'t', 'e', 's', 't'});
    assertNotNull(string);
    assertNotNull(binary);

    assertEquals("test".hashCode(), string.hashCode());
    assertEquals("test".hashCode(), binary.hashCode());
  }
}
