package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5WillPublish;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishProperty;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl.*;
import static org.mqttbee.mqtt5.message.connect.Mqtt5ConnectProperty.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnectEncoder implements Mqtt5MessageEncoder<Mqtt5ConnectImpl> {

    public static final Mqtt5ConnectEncoder INSTANCE = new Mqtt5ConnectEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH =
            6 /* protocol name */ + 1 /* protocol version */ + 1 /* connect flags */ + 2 /* keep alive */;
    private static final byte PROTOCOL_VERSION = 5;

    @Override
    public void encode(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        encodeFixedHeader(connect, out);
        encodeVariableHeader(connect, out);
        encodePayload(connect, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5ConnectImpl connect) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        remainingLength += connect.getRawClientIdentifier().encodedLength();

        final SimpleAuthImpl simpleAuth = connect.getRawSimpleAuth();
        if (simpleAuth != null) {
            remainingLength += nullableEncodedLength(simpleAuth.getRawUsername());
            remainingLength += nullableEncodedLength(simpleAuth.getRawPassword());
        }

        final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
        if (willPublish != Mqtt5WillPublishImpl.DEFAULT_NO_WILL_PUBLISH) {
            remainingLength += encodedLengthWithHeader(connect.encodedWillPropertyLength());
            remainingLength += willPublish.getTopic().encodedLength();
            remainingLength += Mqtt5DataTypes.encodedBinaryDataLength(willPublish.getRawPayload());
        }

        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5ConnectImpl connect) {
        int propertyLength = 0;

        propertyLength += intPropertyEncodedLength(connect.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL);
        propertyLength += booleanPropertyEncodedLength(connect.isResponseInformationRequested(),
                DEFAULT_RESPONSE_INFORMATION_REQUESTED);
        propertyLength += booleanPropertyEncodedLength(connect.isProblemInformationRequested(),
                DEFAULT_PROBLEM_INFORMATION_REQUESTED);

        final RestrictionsImpl restrictions = connect.getRestrictions();
        if (restrictions != RestrictionsImpl.DEFAULT) {
            propertyLength +=
                    shortPropertyEncodedLength(restrictions.getReceiveMaximum(), Restrictions.DEFAULT_RECEIVE_MAXIMUM);
            propertyLength += shortPropertyEncodedLength(restrictions.getTopicAliasMaximum(),
                    Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM);
            propertyLength += intPropertyEncodedLength(restrictions.getMaximumPacketSize(),
                    Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT);
        }

        final Mqtt5ExtendedAuthImpl extendedAuth = connect.getRawExtendedAuth();
        if (extendedAuth != null) {
            propertyLength += propertyEncodedLength(extendedAuth.getMethod());
            propertyLength += nullablePropertyEncodedLength(extendedAuth.getRawData());
        }

        propertyLength += connect.getUserProperties().encodedLength();

        return propertyLength;
    }

    public int encodedWillPropertyLength(@NotNull final Mqtt5ConnectImpl connect) {
        int willPropertyLength = 0;

        final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
        if (willPublish != Mqtt5WillPublishImpl.DEFAULT_NO_WILL_PUBLISH) {
            willPropertyLength += intPropertyEncodedLength(willPublish.getRawMessageExpiryInterval(),
                    Mqtt5WillPublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY);
            willPropertyLength += propertyEncodedLength(willPublish.getRawPayloadFormatIndicator());
            willPropertyLength += nullablePropertyEncodedLength(willPublish.getRawContentType());
            willPropertyLength += nullablePropertyEncodedLength(willPublish.getRawResponseTopic());
            willPropertyLength += nullablePropertyEncodedLength(willPublish.getRawCorrelationData());
            willPropertyLength += willPublish.getUserProperties().encodedLength();
            willPropertyLength +=
                    intPropertyEncodedLength(willPublish.getDelayInterval(), Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL);

            if (!Mqtt5DataTypes.isInVariableByteIntegerRange(willPropertyLength)) {
                throw new Mqtt5VariableByteIntegerExceededException("will properties length"); // TODO
            }
        }

        return willPropertyLength;
    }

    private void encodeFixedHeader(@NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        final int remainingLength = connect.encodedRemainingLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT);
        Mqtt5DataTypes.encodeVariableByteInteger(remainingLength, out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {

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
        if (willPublish != Mqtt5WillPublishImpl.DEFAULT_NO_WILL_PUBLISH) {
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

        encodeProperties(connect, out);
    }

    private void encodeProperties(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {

        final int propertyLength = connect.encodedPropertyLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT);
        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        encodeIntProperty(
                SESSION_EXPIRY_INTERVAL, connect.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL, out);
        encodeBooleanProperty(REQUEST_RESPONSE_INFORMATION, connect.isResponseInformationRequested(),
                DEFAULT_RESPONSE_INFORMATION_REQUESTED, out);
        encodeBooleanProperty(REQUEST_PROBLEM_INFORMATION, connect.isProblemInformationRequested(),
                DEFAULT_PROBLEM_INFORMATION_REQUESTED, out);

        final Mqtt5ExtendedAuthImpl extendedAuth = connect.getRawExtendedAuth();
        if (extendedAuth != null) {
            encodeProperty(AUTHENTICATION_METHOD, extendedAuth.getMethod(), out);
            encodeNullableProperty(AUTHENTICATION_DATA, extendedAuth.getRawData(), out);
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

        connect.encodeUserProperties(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT, out);
    }

    private void encodePayload(@NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {
        connect.getRawClientIdentifier().to(out);

        encodeWillPublish(connect, out);

        final SimpleAuthImpl simpleAuth = connect.getRawSimpleAuth();
        if (simpleAuth != null) {
            encodeNullable(simpleAuth.getRawUsername(), out);
            encodeNullable(simpleAuth.getRawPassword(), out);
        }
    }

    private void encodeWillPublish(@NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {
        final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
        if (willPublish != Mqtt5WillPublishImpl.DEFAULT_NO_WILL_PUBLISH) {

            final int willPropertyLength = connect.encodedWillPropertyLength();
            Mqtt5DataTypes.encodeVariableByteInteger(willPropertyLength, out);

            encodeIntProperty(Mqtt5WillPublishProperty.MESSAGE_EXPIRY_INTERVAL,
                    willPublish.getRawMessageExpiryInterval(), Mqtt5WillPublishImpl.MESSAGE_EXPIRY_INTERVAL_INFINITY,
                    out);
            encodeNullableProperty(
                    Mqtt5WillPublishProperty.PAYLOAD_FORMAT_INDICATOR, willPublish.getRawPayloadFormatIndicator(), out);
            encodeNullableProperty(Mqtt5WillPublishProperty.CONTENT_TYPE, willPublish.getRawContentType(), out);
            encodeNullableProperty(Mqtt5WillPublishProperty.RESPONSE_TOPIC, willPublish.getRawResponseTopic(), out);
            encodeNullableProperty(Mqtt5WillPublishProperty.CORRELATION_DATA, willPublish.getRawCorrelationData(), out);
            willPublish.getUserProperties().encode(out);
            encodeIntProperty(Mqtt5WillPublishProperty.WILL_DELAY_INTERVAL, willPublish.getDelayInterval(),
                    Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL, out);

            willPublish.getTopic().to(out);
            Mqtt5DataTypes.encodeBinaryData(willPublish.getRawPayload(), out);
        }
    }

}
