package org.mqttbee.mqtt5.codec.encoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5TopicFilter;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeImpl;
import org.mqttbee.mqtt5.message.subscribe.Mqtt5SubscribeInternal;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author David Katz
 */
class Mqtt5SubscribeEncoderTest {
    private static final int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    private static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;
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
    void encode_allProperties() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                30,
                // packet identifier
                0, 1,
                // variable header
                // properties length
                17,
                // user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y',
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0001_1101
        };

        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
        final Mqtt5UserProperty mqtt5UserProperty = new Mqtt5UserProperty(user, property);
        final ImmutableList<Mqtt5UserProperty> userProperties = ImmutableList.of(mqtt5UserProperty);

        final Mqtt5TopicFilter topicFiler = Mqtt5TopicFilter.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
        final boolean isRetainAsPublished = true;
        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList
                .of(new Mqtt5SubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        isRetainAsPublished));
        final Mqtt5SubscribeImpl subscribe = new Mqtt5SubscribeImpl(subscriptions, userProperties);
        encode(expected, subscribe, 1);
    }

    @Test
    void encode_multipleUserProperties() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                47,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                34,
                // user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y', //
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y',
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0001_1101
        };

        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
        final Mqtt5UserProperty mqtt5UserProperty = new Mqtt5UserProperty(user, property);
        final ImmutableList<Mqtt5UserProperty> userProperties = ImmutableList.of(mqtt5UserProperty, mqtt5UserProperty);

        final Mqtt5TopicFilter topicFiler = Mqtt5TopicFilter.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
        final boolean isRetainAsPublished = true;
        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList
                .of(new Mqtt5SubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        isRetainAsPublished));
        final Mqtt5SubscribeImpl subscribe = new Mqtt5SubscribeImpl(subscriptions, userProperties);
        encode(expected, subscribe, 10);
    }

    @Test
    void encode_userProperties() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                30,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                17,
                // user properties
                0x26, 0, 4, 'u', 's', 'e', 'r', 0, 8, 'p', 'r', 'o', 'p', 'e', 'r', 't', 'y',
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0001_1101
        };

        final Mqtt5UTF8String user = requireNonNull(Mqtt5UTF8String.from("user"));
        final Mqtt5UTF8String property = requireNonNull(Mqtt5UTF8String.from("property"));
        final Mqtt5UserProperty mqtt5UserProperty = new Mqtt5UserProperty(user, property);
        final ImmutableList<Mqtt5UserProperty> userProperties = ImmutableList.of(mqtt5UserProperty);

        final Mqtt5TopicFilter topicFiler = Mqtt5TopicFilter.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
        final boolean isRetainAsPublished = true;
        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList
                .of(new Mqtt5SubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        isRetainAsPublished));
        final Mqtt5SubscribeImpl subscribe = new Mqtt5SubscribeImpl(subscriptions, userProperties);
        encode(expected, subscribe, 10);
    }

    @Test
    void encode_subscriptionIdentifier() {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                15,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                2,
                // subscription identifier
                0x0B, 111,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0001_1101
        };

        final Mqtt5TopicFilter topicFiler = Mqtt5TopicFilter.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
        final boolean isRetainAsPublished = true;
        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList
                .of(new Mqtt5SubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        isRetainAsPublished));
        final Mqtt5SubscribeImpl subscribe = new Mqtt5SubscribeImpl(subscriptions, ImmutableList.of());
        final Mqtt5SubscribeInternal subscribeInternal = new Mqtt5SubscribeInternal(subscribe, 10, 111);

        encodeInternal(expected, subscribeInternal);
    }

    @Test
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final Mqtt5TopicFilter topicFiler = maxPacket.getTopicFilterTooLong();
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
        final boolean isRetainAsPublished = true;

        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList
                .of(new Mqtt5SubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        isRetainAsPublished));
        final Mqtt5SubscribeImpl subscribe =
                new Mqtt5SubscribeImpl(subscriptions, maxPacket.getMaxPossibleUserProperties());

        final int packetIdentifier = 1;
        final Mqtt5SubscribeInternal subscribeInternal = new Mqtt5SubscribeInternal(subscribe, packetIdentifier);

        final Throwable exception =
                assertThrows(EncoderException.class, () -> channel.writeOutbound(subscribeInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final Mqtt5TopicFilter topicFiler = Mqtt5TopicFilter.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
        final boolean isRetainAsPublished = true;
        final ImmutableList<Mqtt5UserProperty> tooManyUserProperties = maxPacket
                .getUserProperties((VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1);

        final ImmutableList<Mqtt5SubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList
                .of(new Mqtt5SubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        isRetainAsPublished));
        final Mqtt5SubscribeImpl subscribe = new Mqtt5SubscribeImpl(subscriptions, tooManyUserProperties);

        final int packetIdentifier = 2;
        final Mqtt5SubscribeInternal subscribeInternal = new Mqtt5SubscribeInternal(subscribe, packetIdentifier);

        final Throwable exception =
                assertThrows(EncoderException.class, () -> channel.writeOutbound(subscribeInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
    }

    private void encode(final byte[] expected, final Mqtt5SubscribeImpl subscribe, final int packetIdentifier) {
        final Mqtt5SubscribeInternal subscribeInternal = new Mqtt5SubscribeInternal(subscribe, packetIdentifier);
        encodeInternal(expected, subscribeInternal);
    }

    private void encodeInternal(final byte[] expected, final Mqtt5SubscribeInternal subscribeInternal) {
        channel.writeOutbound(subscribeInternal);
        final ByteBuf byteBuf = channel.readOutbound();

        final byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        byteBuf.release();

        assertArrayEquals(expected, actual);
    }

    private class MaximumPacketBuilder {

        private static final String TOPIC = "topic";
        private char[] topicStringBytes;
        private ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder;
        final Mqtt5UserProperty userProperty = new Mqtt5UserProperty(
                requireNonNull(Mqtt5UTF8String.from("user")),
                requireNonNull(Mqtt5UTF8String.from("property")));
        final int userPropertyBytes = 1 // identifier
                + 2 // key length
                + 4 // bytes to encode "user"
                + 2 // value length
                + 8; // bytes to encode "property"

        MaximumPacketBuilder build() {
            final int maxPropertyLength = Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
                    - 2  // packet identifier
                    - 4  // remaining length
                    - 4  // property length
                    - 2  // topic filter length
                    - 1  // subscribe options
                    - TOPIC.length(); // default topic filter

            final int topicFilterLength = maxPropertyLength % userPropertyBytes;

            topicStringBytes = new char[topicFilterLength];
            Arrays.fill(topicStringBytes, 'x');

            final int numberOfUserProperties = maxPropertyLength / userPropertyBytes;
            userPropertiesBuilder = new ImmutableList.Builder<>();
            for (int i = 0; i < numberOfUserProperties; i++) {
                userPropertiesBuilder.add(userProperty);
            }
            return this;
        }

        ImmutableList<Mqtt5UserProperty> getMaxPossibleUserProperties() {
            return userPropertiesBuilder.build();
        }

        Mqtt5TopicFilter getTopicFilterTooLong() {
            return requireNonNull(Mqtt5TopicFilter.from(TOPIC + new String(topicStringBytes) + "x"));
        }

        ImmutableList<Mqtt5UserProperty> getUserProperties(final int totalCount) {
            final ImmutableList.Builder<Mqtt5UserProperty> builder = new ImmutableList.Builder<>();
            for (int i = 0; i < totalCount; i++) {
                builder.add(userProperty);
            }
            return builder.build();
        }
    }
}
