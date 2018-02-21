package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelReasonCode;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelImpl;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelReasonCode.SUCCESS;

/**
 * @author David Katz
 */
class Mqtt5PubRelEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt5PubRelEncoderTest() {
        super(true);
    }

    @Test
    void encode_simple() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                0, 5,
                // reason code
                (byte) 0x92
        };

        final Mqtt5PubRelReasonCode reasonCode = Mqtt5PubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final MqttUTF8StringImpl reasonString = null;
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;
        final MqttPubRelImpl pubRel =
                new MqttPubRelImpl(5, reasonCode, reasonString, userProperties, Mqtt5PubRelEncoder.PROVIDER);

        encode(expected, pubRel);
    }

    @Test
    void encode_reasonCodeOmittedWhenSuccessWithoutProperties() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                2,
                // variable header
                //   packet identifier
                0, 5
        };

        final MqttPubRelImpl pubRel = new MqttPubRelImpl(5, SUCCESS, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                Mqtt5PubRelEncoder.PROVIDER);

        encode(expected, pubRel);
    }

    @ParameterizedTest
    @EnumSource(value = Mqtt5PubRelReasonCode.class, mode = EXCLUDE, names = {"SUCCESS"})
    void encode_reasonCodes(final Mqtt5PubRelReasonCode reasonCode) {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                3,
                // variable header
                //   packet identifier
                6, 5,
                //   reason code placeholder
                (byte) 0xFF
        };

        expected[4] = (byte) reasonCode.getCode();
        final MqttPubRelImpl pubRel =
                new MqttPubRelImpl(0x0605, reasonCode, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                        Mqtt5PubRelEncoder.PROVIDER);

        encode(expected, pubRel);
    }

    @Test
    void encode_reasonString() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                13,
                // variable header
                //   packet identifier
                0, 9,
                //   reason code
                (byte) 0x92,
                //   properties
                9,
                // reason string
                0x1F, 0, 6, 'r', 'e', 'a', 's', 'o', 'n'
        };

        final Mqtt5PubRelReasonCode reasonCode = Mqtt5PubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final MqttUTF8StringImpl reasonString = MqttUTF8StringImpl.from("reason");
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;
        final MqttPubRelImpl pubRel =
                new MqttPubRelImpl(9, reasonCode, reasonString, userProperties, Mqtt5PubRelEncoder.PROVIDER);

        encode(expected, pubRel);
    }

    @Test
    void encode_userProperty() {
        final byte[] expected = {
                // fixed header
                //   type, flags
                0b0110_0010,
                //   remaining length
                17,
                // variable header
                //   packet identifier
                0, 5,
                //   reason code
                (byte) 0x92,
                //   properties
                13,
                // user Property
                0x26, 0, 3, 'k', 'e', 'y', 0, 5, 'v', 'a', 'l', 'u', 'e'
        };

        final Mqtt5PubRelReasonCode reasonCode = Mqtt5PubRelReasonCode.PACKET_IDENTIFIER_NOT_FOUND;
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(ImmutableList.of(
                new MqttUserPropertyImpl(requireNonNull(MqttUTF8StringImpl.from("key")),
                        requireNonNull(MqttUTF8StringImpl.from("value")))));
        final MqttPubRelImpl pubRel =
                new MqttPubRelImpl(5, reasonCode, null, userProperties, Mqtt5PubRelEncoder.PROVIDER);

        encode(expected, pubRel);
    }

    @Test
    @Disabled("transform to encode_maximumPacketSizeExceeded_omitUserPropertiesAndReasonString")
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final MqttPubRelImpl pubRel = new MqttPubRelImpl(1, SUCCESS, maxPacket.getMaxPaddedReasonString("a"),
                maxPacket.getMaxPossibleUserProperties(), Mqtt5PubRelEncoder.PROVIDER);

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(pubRel));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    @Disabled("transform to encode_propertyLengthExceeded_omitUserPropertiesAndReasonString")
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final MqttPubRelImpl pubRel = new MqttPubRelImpl(1, SUCCESS, maxPacket.getMaxPaddedReasonString(),
                maxPacket.getMaxPossibleUserProperties(1), Mqtt5PubRelEncoder.PROVIDER);

        final Throwable exception = assertThrows(EncoderException.class, () -> channel.writeOutbound(pubRel));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
    }


    private void encode(final byte[] expected, final MqttPubRelImpl pubRel) {
        channel.writeOutbound(pubRel);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private class MaximumPacketBuilder {

        private StringBuilder reasonStringBuilder;
        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));

        MaximumPacketBuilder build() {
            final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                    - 4  // remaining length
                    - 4  // property length
                    - 2  // packet identifier
                    - 1; // reason code

            final int remainingBytes = maxPropertyLength - 3; // reason string identifier and length
            final int userPropertyBytes = 1 // identifier
                    + 2 // key length
                    + 4 // bytes to encode "user"
                    + 2 // value length
                    + 8; // bytes to encode "property"
            final int reasonStringBytes = remainingBytes % userPropertyBytes;

            reasonStringBuilder = new StringBuilder();
            for (int i = 0; i < reasonStringBytes; i++) {
                reasonStringBuilder.append(i);
            }

            final int numberOfUserProperties = remainingBytes / userPropertyBytes;
            userPropertiesBuilder = new ImmutableList.Builder<>();
            final MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
        }

        MqttUTF8StringImpl getMaxPaddedReasonString() {
            return getMaxPaddedReasonString("");
        }

        MqttUTF8StringImpl getMaxPaddedReasonString(final String withSuffix) {
            return MqttUTF8StringImpl.from(reasonStringBuilder.toString() + withSuffix);
        }

        MqttUserPropertiesImpl getMaxPossibleUserProperties() {
            return getMaxPossibleUserProperties(0);
        }

        MqttUserPropertiesImpl getMaxPossibleUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(new MqttUserPropertyImpl(user, property));
            }
            return MqttUserPropertiesImpl.of(userPropertiesBuilder.build());
        }
    }
}
