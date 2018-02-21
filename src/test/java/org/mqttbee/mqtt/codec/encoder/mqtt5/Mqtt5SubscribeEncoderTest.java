package org.mqttbee.mqtt.codec.encoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.EncoderException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mqttbee.api.mqtt5.message.Mqtt5QoS;
import org.mqttbee.api.mqtt5.message.subscribe.Mqtt5RetainHandling;
import org.mqttbee.mqtt.codec.encoder.AbstractMqtt5EncoderTest;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeImpl;
import org.mqttbee.mqtt.message.subscribe.MqttSubscribeWrapper;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mqttbee.api.mqtt5.message.subscribe.Mqtt5Subscribe.Subscription.*;


/**
 * @author David Katz
 */
class Mqtt5SubscribeEncoderTest extends AbstractMqtt5EncoderTest {

    private static final int VARIABLE_BYTE_INTEGER_VALUE_BITS = 7;
    private static final int VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE =
            (1 << (VARIABLE_BYTE_INTEGER_VALUE_BITS * 4)) - 1;

    Mqtt5SubscribeEncoderTest() {
        super(true);
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

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty));

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND_IF_SUBSCRIPTION_DOES_NOT_EXIST;
        final boolean isRetainAsPublished = true;
        final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList.of(
                new MqttSubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        isRetainAsPublished));
        final MqttSubscribeImpl subscribe =
                new MqttSubscribeImpl(subscriptions, userProperties, Mqtt5SubscribeEncoder.PROVIDER);
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
                0b0000_0001
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties =
                MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty, mqtt5UserProperty));

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList.of(
                new MqttSubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, DEFAULT_NO_LOCAL,
                        DEFAULT_RETAIN_HANDLING, DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribeImpl subscribe =
                new MqttSubscribeImpl(subscriptions, userProperties, Mqtt5SubscribeEncoder.PROVIDER);
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
                0b0000_0001
        };

        final MqttUTF8StringImpl user = requireNonNull(MqttUTF8StringImpl.from("user"));
        final MqttUTF8StringImpl property = requireNonNull(MqttUTF8StringImpl.from("property"));
        final MqttUserPropertyImpl mqtt5UserProperty = new MqttUserPropertyImpl(user, property);
        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.of(ImmutableList.of(mqtt5UserProperty));

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList.of(
                new MqttSubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, DEFAULT_NO_LOCAL,
                        DEFAULT_RETAIN_HANDLING, DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribeImpl subscribe =
                new MqttSubscribeImpl(subscriptions, userProperties, Mqtt5SubscribeEncoder.PROVIDER);
        encode(expected, subscribe, 10);
    }

    @ParameterizedTest
    @EnumSource(Mqtt5QoS.class)
    void encode_subscriptionOptionsQos(final Mqtt5QoS qos) {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                13,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                0,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0000_0000
        };

        expected[14] |= qos.getCode();

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList.of(
                new MqttSubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, DEFAULT_NO_LOCAL,
                        DEFAULT_RETAIN_HANDLING, DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribeImpl subscribe =
                new MqttSubscribeImpl(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                        Mqtt5SubscribeEncoder.PROVIDER);
        encode(expected, subscribe, 10);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void encode_subscriptionOptionsNoLocal(final int noLocal) {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                13,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                0,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0000_0000
        };

        expected[14] |= noLocal << 2;

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final boolean isNoLocal = noLocal == 1;
        final Mqtt5RetainHandling retainHandling = Mqtt5RetainHandling.SEND;
        final Mqtt5QoS qos = Mqtt5QoS.AT_MOST_ONCE;
        final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList.of(
                new MqttSubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribeImpl subscribe =
                new MqttSubscribeImpl(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                        Mqtt5SubscribeEncoder.PROVIDER);
        encode(expected, subscribe, 10);
    }

    @ParameterizedTest
    @EnumSource(Mqtt5RetainHandling.class)
    void encode_subscriptionOptionsRetain(final Mqtt5RetainHandling retainHandling) {
        final byte[] expected = {
                // fixed header
                // type, reserved
                (byte) 0b1000_0010,
                // remaining length
                13,
                // packet identifier
                0, 10,
                // variable header
                // properties length
                0,
                // payload topic filter
                0, 7, 't', 'o', 'p', 'i', 'c', '/', '#',
                // subscription options
                0b0000_0101
        };

        expected[14] |= retainHandling.getCode() << 4;

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final boolean isNoLocal = true;
        final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList.of(
                new MqttSubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, isNoLocal, retainHandling,
                        DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribeImpl subscribe =
                new MqttSubscribeImpl(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                        Mqtt5SubscribeEncoder.PROVIDER);
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
                0b0000_0001
        };

        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList.of(
                new MqttSubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, DEFAULT_NO_LOCAL,
                        DEFAULT_RETAIN_HANDLING, DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribeImpl subscribe =
                new MqttSubscribeImpl(subscriptions, MqttUserPropertiesImpl.NO_USER_PROPERTIES,
                        Mqtt5SubscribeEncoder.PROVIDER);
        final MqttSubscribeWrapper subscribeInternal = subscribe.wrap(10, 111);

        encodeInternal(expected, subscribeInternal);
    }

    @Test
    @Disabled("transform to encode_maximumPacketSizeExceeded_omitUserPropertiesAndReasonString")
    void encode_maximumPacketSizeExceeded_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final MqttTopicFilterImpl topicFiler = maxPacket.getTopicFilterTooLong();
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList.of(
                new MqttSubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, DEFAULT_NO_LOCAL,
                        DEFAULT_RETAIN_HANDLING, DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribeImpl subscribe =
                new MqttSubscribeImpl(subscriptions, maxPacket.getMaxPossibleUserProperties(),
                        Mqtt5SubscribeEncoder.PROVIDER);

        final int packetIdentifier = 1;
        final MqttSubscribeWrapper subscribeInternal =
                subscribe.wrap(packetIdentifier, MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);

        final Throwable exception =
                assertThrows(EncoderException.class, () -> channel.writeOutbound(subscribeInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for remaining length"));
    }

    @Test
    @Disabled("transform to encode_propertyLengthExceeded_omitUserPropertiesAndReasonString")
    void encode_propertyLengthExceedsMax_throwsEncoderException() {
        final MaximumPacketBuilder maxPacket = new MaximumPacketBuilder().build();
        final MqttTopicFilterImpl topicFiler = MqttTopicFilterImpl.from("topic/#");
        final Mqtt5QoS qos = Mqtt5QoS.AT_LEAST_ONCE;
        final MqttUserPropertiesImpl tooManyUserProperties = maxPacket.getUserProperties(
                (VARIABLE_BYTE_INTEGER_FOUR_BYTES_MAX_VALUE / maxPacket.userPropertyBytes) + 1);

        final ImmutableList<MqttSubscribeImpl.SubscriptionImpl> subscriptions = ImmutableList.of(
                new MqttSubscribeImpl.SubscriptionImpl(requireNonNull(topicFiler), qos, DEFAULT_NO_LOCAL,
                        DEFAULT_RETAIN_HANDLING, DEFAULT_RETAIN_AS_PUBLISHED));
        final MqttSubscribeImpl subscribe =
                new MqttSubscribeImpl(subscriptions, tooManyUserProperties, Mqtt5SubscribeEncoder.PROVIDER);

        final int packetIdentifier = 2;
        final MqttSubscribeWrapper subscribeInternal =
                subscribe.wrap(packetIdentifier, MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);

        final Throwable exception =
                assertThrows(EncoderException.class, () -> channel.writeOutbound(subscribeInternal));
        assertTrue(exception.getMessage().contains("variable byte integer size exceeded for property length"));
    }

    private void encode(final byte[] expected, final MqttSubscribeImpl subscribe, final int packetIdentifier) {
        final MqttSubscribeWrapper subscribeInternal =
                subscribe.wrap(packetIdentifier, MqttSubscribeWrapper.DEFAULT_NO_SUBSCRIPTION_IDENTIFIER);
        encodeInternal(expected, subscribeInternal);
    }

    private void encodeInternal(final byte[] expected, final MqttSubscribeWrapper subscribeInternal) {
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
        private ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder;
        final MqttUserPropertyImpl userProperty =
                new MqttUserPropertyImpl(requireNonNull(MqttUTF8StringImpl.from("user")),
                        requireNonNull(MqttUTF8StringImpl.from("property")));
        final int userPropertyBytes = 1 // identifier
                + 2 // key length
                + 4 // bytes to encode "user"
                + 2 // value length
                + 8; // bytes to encode "property"

        MaximumPacketBuilder build() {
            final int maxPropertyLength = MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT - 1  // type, reserved
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

        MqttUserPropertiesImpl getMaxPossibleUserProperties() {
            return MqttUserPropertiesImpl.of(userPropertiesBuilder.build());
        }

        MqttTopicFilterImpl getTopicFilterTooLong() {
            return requireNonNull(MqttTopicFilterImpl.from(TOPIC + new String(topicStringBytes) + "x"));
        }

        MqttUserPropertiesImpl getUserProperties(final int totalCount) {
            final ImmutableList.Builder<MqttUserPropertyImpl> builder = new ImmutableList.Builder<>();
            for (int i = 0; i < totalCount; i++) {
                builder.add(userProperty);
            }
            return MqttUserPropertiesImpl.of(builder.build());
        }
    }
}
