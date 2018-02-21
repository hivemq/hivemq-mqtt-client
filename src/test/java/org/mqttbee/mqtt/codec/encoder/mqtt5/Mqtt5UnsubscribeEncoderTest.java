package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribeImpl;
import org.mqttbee.mqtt.message.unsubscribe.MqttUnsubscribeWrapper;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author David Katz
 */
class Mqtt5UnsubscribeEncoderTest extends AbstractMqtt5EncoderTest {

    Mqtt5UnsubscribeEncoderTest() {
        super(true);
    }

    @Test
    void encode_simple() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1010_0010,
                // remaining length
                29,
                // packet identifier
                0, 1,
                // variable header
                // properties length
                17,
                // user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y',
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#'
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(new MqttUserPropertyImpl(user, property)));

        final ImmutableList<MqttTopicFilterImpl> topicFilters =
                ImmutableList.of(requireNonNull(MqttTopicFilterImpl.from("topic/#")));
        encodeUnsubscribe(expected, userProperties, topicFilters);
    }

    @Test
    void encode_multipleUserProperties() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1010_0010,
                // remaining length
                61,
                // packet identifier
                0, 1,
                // variable header
                // properties length
                51,
                // user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y', //
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y', //
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y',
                // payload topic filter
                0, 5, 't', 'o', 'p', 'i', 'c'
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty, mqtt5UserProperty, mqtt5UserProperty));

        final ImmutableList<MqttTopicFilterImpl> topicFilters =
                ImmutableList.of(requireNonNull(MqttTopicFilterImpl.from("topic")));
        encodeUnsubscribe(expected, userProperties, topicFilters);
    }

    @Test
    @Disabled("transform to encode_maximumPacketSizeExceeded_omitUserPropertiesAndReasonString")
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final MqttUnsubscribeImpl unsubscribe = new MqttUnsubscribeImpl(
                maxPacket.getTopicFilter("extraChars"),
                maxPacket.getMaxPossibleUserProperties(), Mqtt5UnsubscribeEncoder.PROVIDER);

        final int packetIdentifier = 1;
        final MqttUnsubscribeWrapper unsubscribeInternal = unsubscribe.wrap(packetIdentifier);

        final Throwable exception =
                assertThrows(EncoderException.class, () -> channel.writeOutbound(unsubscribeInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    @Disabled("transform to encode_propertyLengthExceeded_omitUserPropertiesAndReasonString")
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final MqttUnsubscribeImpl unsubscribe =
                new MqttUnsubscribeImpl(maxPacket.getTopicFilter(), maxPacket.getMaxPossibleUserProperties(1),
                        Mqtt5UnsubscribeEncoder.PROVIDER);

        final int packetIdentifier = 1;
        final MqttUnsubscribeWrapper unsubscribeInternal = unsubscribe.wrap(packetIdentifier);

        final Throwable exception =
                assertThrows(EncoderException.class, () -> channel.writeOutbound(unsubscribeInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
    }

    private void encodeUnsubscribe(
            final byte[] expected, final MqttUserPropertiesImpl userProperties,
            final ImmutableList<MqttTopicFilterImpl> topicFilters) {
        final MqttUnsubscribeImpl unsubscribe =
                new MqttUnsubscribeImpl(topicFilters, userProperties, Mqtt5UnsubscribeEncoder.PROVIDER);
        final int packetIdentifier = 0x01;
        final MqttUnsubscribeWrapper unsubscribeInternal = unsubscribe.wrap(packetIdentifier);

        encodeInternal(expected, unsubscribeInternal);
    }

    private void encodeInternal(final byte[] expected, final MqttUnsubscribeWrapper unsubscribeInternal) {
        channel.writeOutbound(unsubscribeInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private class MaximumPacketBuilder {

        private static final String TOPIC = "topic";
        private StringBuilder reasonStringBuilder;
        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));

        MaximumPacketBuilder build() {
            final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                    - 4  // remaining length
                    - 4  // property length
                    - 2  // topic filter length
                    - TOPIC.length(); // default topic filter

            final int userPropertyBytes = 1 // identifier
                    + 2 // key length
                    + 4 // bytes to encode "user"
                    + 2 // value length
                    + 8; // bytes to encode "property"
            final int topicFilterBytes = maxPropertyLength % userPropertyBytes;

            reasonStringBuilder = new StringBuilder();
            for (int i = 0; i < topicFilterBytes; i++) {
                reasonStringBuilder.append(i);
            }

            final int numberOfUserProperties = maxPropertyLength / userPropertyBytes;
            userPropertiesBuilder = new ImmutableList.Builder<>();
            final MqttUserPropertyImpl userProperty = new MqttUserPropertyImpl(user, property);
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
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

        ImmutableList<MqttTopicFilterImpl> getTopicFilter(final String withExtraCharacters) {
            return ImmutableList.of(requireNonNull(MqttTopicFilterImpl.from(TOPIC + withExtraCharacters)));
        }

        ImmutableList<MqttTopicFilterImpl> getTopicFilter() {
            return getTopicFilter("");
        }
    }
}
