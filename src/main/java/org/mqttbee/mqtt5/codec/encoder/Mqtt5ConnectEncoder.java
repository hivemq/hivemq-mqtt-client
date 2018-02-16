package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5EnhancedAuthImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectWrapper;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishProperty;

import java.util.function.Function;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl.*;
import static org.mqttbee.mqtt5.message.connect.Mqtt5ConnectProperty.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectEncoder extends Mqtt5WrappedMessageEncoder<Mqtt5ConnectImpl, Mqtt5ConnectWrapper> {

    public static final Function<Mqtt5ConnectImpl, Mqtt5ConnectEncoder> PROVIDER = Mqtt5ConnectEncoder::new;

    private static final int VARIABLE_HEADER_FIXED_LENGTH =
            6 /* protocol name */ + 1 /* protocol version */ + 1 /* connect flags */ + 2 /* keep alive */;

    private int willPropertyLength = -1;

    private Mqtt5ConnectEncoder(@NotNull final Mqtt5ConnectImpl message) {
        super(message);
    }

    @Override
    int calculateRemainingLengthWithoutProperties() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        final SimpleAuthImpl simpleAuth = message.getRawSimpleAuth();
        if (simpleAuth != null) {
            remainingLength += nullableEncodedLength(simpleAuth.getRawUsername());
            remainingLength += nullableEncodedLength(simpleAuth.getRawPassword());
        }

        final Mqtt5WillPublishImpl willPublish = message.getRawWillPublish();
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

        final Mqtt5WillPublishImpl willPublish = message.getRawWillPublish();
        if (willPublish != null) {
            willPropertyLength = willPublish.getEncoder().encodedPropertyLength() +
                    intPropertyEncodedLength(willPublish.getDelayInterval(), Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL);

            if (!Mqtt5DataTypes.isInVariableByteIntegerRange(willPropertyLength)) {
                throw new Mqtt5VariableByteIntegerExceededException("will properties length"); // TODO
            }
        }

        return willPropertyLength;
    }

    @Override
    public Function<Mqtt5ConnectWrapper, Mqtt5ConnectWrapperEncoder> wrap() {
        return Mqtt5ConnectWrapperEncoder.PROVIDER;
    }


    public static class Mqtt5ConnectWrapperEncoder
            extends Mqtt5MessageWrapperEncoder<Mqtt5ConnectWrapper, Mqtt5ConnectImpl> {

        static final Function<Mqtt5ConnectWrapper, Mqtt5ConnectWrapperEncoder> PROVIDER =
                Mqtt5ConnectWrapperEncoder::new;

        private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
        private static final byte PROTOCOL_VERSION = 5;

        private Mqtt5ConnectWrapperEncoder(@NotNull final Mqtt5ConnectWrapper wrapper) {
            super(wrapper);
        }

        @Override
        int additionalRemainingLength() {
            return message.getClientIdentifier().encodedLength();
        }

        @Override
        int additionalPropertyLength() {
            int additionalPropertyLength = 0;

            final Mqtt5EnhancedAuthImpl enhancedAuth = message.getEnhancedAuth();
            if (enhancedAuth != null) {
                additionalPropertyLength += propertyEncodedLength(enhancedAuth.getMethod());
                additionalPropertyLength += nullablePropertyEncodedLength(enhancedAuth.getRawData());
            }

            return additionalPropertyLength;
        }

        @Override
        public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
            encodeFixedHeader(out);
            encodeVariableHeader(out);
            encodePayload(out);
        }

        private void encodeFixedHeader(@NotNull final ByteBuf out) {
            out.writeByte(FIXED_HEADER);
            Mqtt5DataTypes.encodeVariableByteInteger(remainingLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT), out);
        }

        private void encodeVariableHeader(@NotNull final ByteBuf out) {
            final Mqtt5ConnectImpl connect = message.getWrapped();

            Mqtt5UTF8StringImpl.PROTOCOL_NAME.to(out);
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

            final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
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
            final Mqtt5ConnectImpl connect = message.getWrapped();

            Mqtt5DataTypes.encodeVariableByteInteger(propertyLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT), out);

            encodeIntProperty(
                    SESSION_EXPIRY_INTERVAL, connect.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL, out);
            encodeBooleanProperty(REQUEST_RESPONSE_INFORMATION, connect.isResponseInformationRequested(),
                    DEFAULT_RESPONSE_INFORMATION_REQUESTED, out);
            encodeBooleanProperty(REQUEST_PROBLEM_INFORMATION, connect.isProblemInformationRequested(),
                    DEFAULT_PROBLEM_INFORMATION_REQUESTED, out);

            final Mqtt5EnhancedAuthImpl enhancedAuth = message.getEnhancedAuth();
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

            encodeOmissibleProperties(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT, out);
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
            final Mqtt5ConnectImpl connect = message.getWrapped();

            final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
            if (willPublish != null) {
                final int willPropertyLength = ((Mqtt5ConnectEncoder) connect.getEncoder()).willPropertyLength();
                Mqtt5DataTypes.encodeVariableByteInteger(willPropertyLength, out);

                willPublish.getEncoder().encodeFixedProperties(out);
                willPublish.getUserProperties().encode(out);
                encodeIntProperty(Mqtt5WillPublishProperty.WILL_DELAY_INTERVAL, willPublish.getDelayInterval(),
                        Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL, out);

                willPublish.getTopic().to(out);
                encodeOrEmpty(willPublish.getRawPayload(), out);
            }
        }

    }

}
