package org.mqttbee.mqtt.codec.encoder.mqtt5;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt.codec.encoder.MqttMessageEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageWrapperEncoderApplier;
import org.mqttbee.mqtt.codec.encoder.provider.MqttWrappedMessageEncoderProvider;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.exceptions.MqttVariableByteIntegerExceededException;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuthImpl;
import org.mqttbee.mqtt.message.connect.MqttConnectImpl;
import org.mqttbee.mqtt.message.connect.MqttConnectWrapper;
import org.mqttbee.mqtt.message.publish.MqttWillPublishImpl;
import org.mqttbee.mqtt.message.publish.MqttWillPublishProperty;

import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt.message.connect.MqttConnectImpl.*;
import static org.mqttbee.mqtt.message.connect.MqttConnectProperty.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectEncoder extends Mqtt5WrappedMessageEncoder<MqttConnectImpl, MqttConnectWrapper> {

    public static final MqttWrappedMessageEncoderProvider<MqttConnectImpl, MqttConnectWrapper, MqttMessageEncoderProvider<MqttConnectWrapper>>
            PROVIDER = MqttWrappedMessageEncoderProvider.create(Mqtt5ConnectEncoder::new);

    private static final int VARIABLE_HEADER_FIXED_LENGTH =
            6 /* protocol name */ + 1 /* protocol version */ + 1 /* connect flags */ + 2 /* keep alive */;

    private int willPropertyLength = -1;

    @Override
    int calculateRemainingLengthWithoutProperties() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final SimpleAuthImpl simpleAuth = message.getRawSimpleAuth();
        if (simpleAuth != null) {
            remainingLength += nullableEncodedLength(simpleAuth.getRawUsername());
            remainingLength += nullableEncodedLength(simpleAuth.getRawPassword());
        }

        final MqttWillPublishImpl willPublish = message.getRawWillPublish();
        if (willPublish != null) {
            remainingLength += encodedLengthWithHeader(willPropertyLength());
            remainingLength += willPublish.getTopic().encodedLength();
            remainingLength += encodedOrEmptyLength(willPublish.getRawPayload());
        }

        return remainingLength;
    }

    @Override
    int calculatePropertyLength() {
        int propertyLength = 0;

        propertyLength += intPropertyEncodedLength(message.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL);
        propertyLength += booleanPropertyEncodedLength(message.isResponseInformationRequested(),
                DEFAULT_RESPONSE_INFORMATION_REQUESTED);
        propertyLength += booleanPropertyEncodedLength(message.isProblemInformationRequested(),
                DEFAULT_PROBLEM_INFORMATION_REQUESTED);

        final RestrictionsImpl restrictions = message.getRestrictions();
        if (restrictions != RestrictionsImpl.DEFAULT) {
            propertyLength +=
                    shortPropertyEncodedLength(restrictions.getReceiveMaximum(), Restrictions.DEFAULT_RECEIVE_MAXIMUM);
            propertyLength += shortPropertyEncodedLength(restrictions.getTopicAliasMaximum(),
                    Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM);
            propertyLength += intPropertyEncodedLength(restrictions.getMaximumPacketSize(),
                    Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
        }

        propertyLength += message.getUserProperties().encodedLength();

        return propertyLength;
    }

    private int willPropertyLength() {
        if (willPropertyLength == -1) {
            willPropertyLength = calculateWillPropertyLength();
        }
        return willPropertyLength;
    }

    private int calculateWillPropertyLength() {
        int willPropertyLength = 0;

        final MqttWillPublishImpl willPublish = message.getRawWillPublish();
        if (willPublish != null) {
            willPropertyLength = ((Mqtt5PublishEncoder) willPublish.getEncoder()).encodedPropertyLength() +
                    intPropertyEncodedLength(willPublish.getDelayInterval(), Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL);

            if (!MqttVariableByteInteger.isInRange(willPropertyLength)) {
                throw new MqttVariableByteIntegerExceededException("will properties length"); // TODO
            }
        }

        return willPropertyLength;
    }

    @NotNull
    @Override
    public MqttMessageEncoder wrap(@NotNull final MqttConnectWrapper wrapper) {
        return Mqtt5ConnectWrapperEncoder.APPLIER.apply(wrapper, this);
    }


    public static class Mqtt5ConnectWrapperEncoder extends
            Mqtt5MessageWrapperEncoder<MqttConnectWrapper, MqttConnectImpl, MqttMessageEncoderProvider<MqttConnectWrapper>, Mqtt5ConnectEncoder> {

        private static final MqttMessageWrapperEncoderApplier<MqttConnectWrapper, MqttConnectImpl, Mqtt5ConnectEncoder>
                APPLIER = new ThreadLocalMqttMessageWrapperEncoderApplier<>(Mqtt5ConnectWrapperEncoder::new);

        private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
        private static final byte PROTOCOL_VERSION = 5;

        @Override
        int additionalRemainingLength() {
            return message.getClientIdentifier().encodedLength();
        }

        @Override
        int additionalPropertyLength() {
            int additionalPropertyLength = 0;

            final MqttEnhancedAuthImpl enhancedAuth = message.getEnhancedAuth();
            if (enhancedAuth != null) {
                additionalPropertyLength += propertyEncodedLength(enhancedAuth.getMethod());
                additionalPropertyLength += nullablePropertyEncodedLength(enhancedAuth.getRawData());
            }

            return additionalPropertyLength;
        }

        @Override
        public void encode(@NotNull final ByteBuf out, @NotNull final Channel channel) {
            encodeFixedHeader(out);
            encodeVariableHeader(out);
            encodePayload(out);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out) {
            out.writeByte(FIXED_HEADER);
            MqttVariableByteInteger.encode(remainingLength(MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT), out);
        }

        private void encodeVariableHeader(@NotNull final ByteBuf out) {
            final MqttConnectImpl connect = message.getWrapped();

            MqttUTF8StringImpl.PROTOCOL_NAME.to(out);
            out.writeByte(PROTOCOL_VERSION);

            int connectFlags = 0;

            final SimpleAuthImpl simpleAuth = connect.getRawSimpleAuth();
            if (simpleAuth != null) {
                if (simpleAuth.getRawUsername() != null) {
                    connectFlags |= 0b1000_0000;
                }
                if (simpleAuth.getRawPassword() != null) {
                    connectFlags |= 0b0100_0000;
                }
            }

            final MqttWillPublishImpl willPublish = connect.getRawWillPublish();
            if (willPublish != null) {
                if (willPublish.isRetain()) {
                    connectFlags |= 0b0010_0000;
                }
                connectFlags |= willPublish.getQos().getCode() << 3;
                connectFlags |= 0b0000_0100;
            }

            if (connect.isCleanStart()) {
                connectFlags |= 0b0000_0010;
            }

            out.writeByte(connectFlags);

            out.writeShort(connect.getKeepAlive());

            encodeProperties(out);
        }

        private void encodeProperties(@NotNull final ByteBuf out) {
            final MqttConnectImpl connect = message.getWrapped();

            MqttVariableByteInteger.encode(propertyLength(MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT), out);

            encodeIntProperty(
                    SESSION_EXPIRY_INTERVAL, connect.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL, out);
            encodeBooleanProperty(REQUEST_RESPONSE_INFORMATION, connect.isResponseInformationRequested(),
                    DEFAULT_RESPONSE_INFORMATION_REQUESTED, out);
            encodeBooleanProperty(REQUEST_PROBLEM_INFORMATION, connect.isProblemInformationRequested(),
                    DEFAULT_PROBLEM_INFORMATION_REQUESTED, out);

            final MqttEnhancedAuthImpl enhancedAuth = message.getEnhancedAuth();
            if (enhancedAuth != null) {
                encodeProperty(AUTHENTICATION_METHOD, enhancedAuth.getMethod(), out);
                encodeNullableProperty(AUTHENTICATION_DATA, enhancedAuth.getRawData(), out);
            }

            final RestrictionsImpl restrictions = connect.getRestrictions();
            if (restrictions != RestrictionsImpl.DEFAULT) {
                encodeShortProperty(
                        RECEIVE_MAXIMUM, restrictions.getReceiveMaximum(), Restrictions.DEFAULT_RECEIVE_MAXIMUM, out);
                encodeShortProperty(TOPIC_ALIAS_MAXIMUM, restrictions.getTopicAliasMaximum(),
                        Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM, out);
                encodeIntProperty(MAXIMUM_PACKET_SIZE, restrictions.getMaximumPacketSize(),
                        Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT, out);
            }

            encodeOmissibleProperties(MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT, out);
        }

        private void encodePayload(@NotNull final ByteBuf out) {
            message.getClientIdentifier().to(out);

            encodeWillPublish(out);

            final SimpleAuthImpl simpleAuth = message.getWrapped().getRawSimpleAuth();
            if (simpleAuth != null) {
                encodeNullable(simpleAuth.getRawUsername(), out);
                encodeNullable(simpleAuth.getRawPassword(), out);
            }
        }

        private void encodeWillPublish(@NotNull final ByteBuf out) {
            final MqttConnectImpl connect = message.getWrapped();

            final MqttWillPublishImpl willPublish = connect.getRawWillPublish();
            if (willPublish != null) {
                final int willPropertyLength = wrappedEncoder.willPropertyLength();
                MqttVariableByteInteger.encode(willPropertyLength, out);

                ((Mqtt5PublishEncoder) willPublish.getEncoder()).encodeFixedProperties(out);
                willPublish.getUserProperties().encode(out);
                encodeIntProperty(MqttWillPublishProperty.WILL_DELAY_INTERVAL, willPublish.getDelayInterval(),
                        Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL, out);

                willPublish.getTopic().to(out);
                encodeOrEmpty(willPublish.getRawPayload(), out);
            }
        }

    }

}
