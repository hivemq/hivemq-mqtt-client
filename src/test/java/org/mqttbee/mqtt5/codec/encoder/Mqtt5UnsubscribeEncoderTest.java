package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilterImpl;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.Mqtt5UserProperties;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeImpl;
import org.mqttbee.mqtt5.message.unsubscribe.Mqtt5UnsubscribeInternal;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author David Katz
 */
class Mqtt5UnsubscribeEncoderTest {

    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel(new Mqtt5Encoder());
    }

    @AfterEach
    void tearDown() {
        channel.close();
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

        final Mqtt5UTF8StringImpl user = requireNonNull(Mqtt5UTF8StringImpl.from("user"));
        final Mqtt5UTF8StringImpl property = requireNonNull(Mqtt5UTF8StringImpl.from("property"));
        final Mqtt5UserProperties userProperties =
                Mqtt5UserProperties.of(ImmutableList.of(new Mqtt5UserProperty(user, property)));

        final ImmutableList<Mqtt5TopicFilterImpl> topicFilters =
                ImmutableList.of(requireNonNull(Mqtt5TopicFilterImpl.from("topic/#")));
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

        final Mqtt5UTF8StringImpl user = requireNonNull(Mqtt5UTF8StringImpl.from("user"));
        final Mqtt5UTF8StringImpl property = requireNonNull(Mqtt5UTF8StringImpl.from("property"));
        final Mqtt5UserProperty mqtt5UserProperty = new Mqtt5UserProperty(user, property);
        final Mqtt5UserProperties userProperties =
                Mqtt5UserProperties.of(ImmutableList.of(mqtt5UserProperty, mqtt5UserProperty, mqtt5UserProperty));

        final ImmutableList<Mqtt5TopicFilterImpl> topicFilters =
                ImmutableList.of(requireNonNull(Mqtt5TopicFilterImpl.from("topic")));
        encodeUnsubscribe(expected, userProperties, topicFilters);
    }

    @Test
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final Mqtt5UnsubscribeImpl unsubscribe = new Mqtt5UnsubscribeImpl(maxPacket.getTopicFilter("extraChars"),
                maxPacket.getMaxPossibleUserProperties());

        final int packetIdentifier = 1;
        final Mqtt5UnsubscribeInternal unsubscribeInternal =
                new Mqtt5UnsubscribeInternal(unsubscribe, packetIdentifier);

        final Throwable exception =
                assertThrows(EncoderException.class, () -> channel.writeOutbound(unsubscribeInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final Mqtt5UnsubscribeImpl unsubscribe =
                new Mqtt5UnsubscribeImpl(maxPacket.getTopicFilter(), maxPacket.getMaxPossibleUserProperties(1));

        final int packetIdentifier = 1;
        final Mqtt5UnsubscribeInternal unsubscribeInternal =
                new Mqtt5UnsubscribeInternal(unsubscribe, packetIdentifier);

        final Throwable exception =
                assertThrows(EncoderException.class, () -> channel.writeOutbound(unsubscribeInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
    }

    private void encodeUnsubscribe(
            final byte[] expected, final Mqtt5UserProperties userProperties,
            final ImmutableList<Mqtt5TopicFilterImpl> topicFilters) {
        final Mqtt5UnsubscribeImpl unsubscribe = new Mqtt5UnsubscribeImpl(topicFilters, userProperties);
        final int packetIdentifier = 0x01;
        final Mqtt5UnsubscribeInternal unsubscribeInternal =
                new Mqtt5UnsubscribeInternal(unsubscribe, packetIdentifier);

        encodeInternal(expected, unsubscribeInternal);
    }

    private void encodeInternal(final byte[] expected, final Mqtt5UnsubscribeInternal unsubscribeInternal) {
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
        private ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder;
        final Mqtt5UTF8StringImpl user = requireNonNull(Mqtt5UTF8StringImpl.from("user"));
        final Mqtt5UTF8StringImpl property = requireNonNull(Mqtt5UTF8StringImpl.from("property"));

        MaximumPacketBuilder build() {
            final int maxPropertyLength = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
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
            final Mqtt5UserProperty userProperty = new Mqtt5UserProperty(user, property);
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
        }

        Mqtt5UserProperties getMaxPossibleUserProperties() {
            return getMaxPossibleUserProperties(0);
        }

        Mqtt5UserProperties getMaxPossibleUserProperties(final int withExtraUserProperties) {
            for (int i = 0; i < withExtraUserProperties; i++) {
                userPropertiesBuilder.add(new Mqtt5UserProperty(user, property));
            }
            return Mqtt5UserProperties.of(userPropertiesBuilder.build());
        }

        ImmutableList<Mqtt5TopicFilterImpl> getTopicFilter(final String withExtraCharacters) {
            return ImmutableList.of(requireNonNull(Mqtt5TopicFilterImpl.from(TOPIC + withExtraCharacters)));
        }

        ImmutableList<Mqtt5TopicFilterImpl> getTopicFilter() {
            return getTopicFilter("");
        }
    }
}
