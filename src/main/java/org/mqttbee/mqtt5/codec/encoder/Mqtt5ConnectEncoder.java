package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.publish.Mqtt5WillPublish;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageWithPropertiesEncoder.Mqtt5MessageWithUserPropertiesEncoder;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8StringImpl;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishProperty;

import java.util.function.Function;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl.*;
import static org.mqttbee.mqtt5.message.connect.Mqtt5ConnectProperty.*;

/**
 * @author Silvio Giebl
 */
public class Mqtt5ConnectEncoder extends Mqtt5MessageWithUserPropertiesEncoder<Mqtt5ConnectImpl> {

    public static final Function<Mqtt5ConnectImpl, Mqtt5ConnectEncoder> PROVIDER = Mqtt5ConnectEncoder::new;

    private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH =
            6 /* protocol name */ + 1 /* protocol version */ + 1 /* connect flags */ + 2 /* keep alive */;
    private static final byte PROTOCOL_VERSION = 5;

    private int willPropertyLength = -1;

    private Mqtt5ConnectEncoder(@NotNull final Mqtt5ConnectImpl message) {
        super(message);
    }

    @Override
    int calculateRemainingLength() {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        remainingLength += message.getRawClientIdentifier().encodedLength();

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
        propertyLength += booleanPropertyEncodedLength(
                message.isResponseInformationRequested(),
                DEFAULT_RESPONSE_INFORMATION_REQUESTED);
        propertyLength += booleanPropertyEncodedLength(
                message.isProblemInformationRequested(),
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

        final Mqtt5ExtendedAuthImpl extendedAuth = message.getRawExtendedAuth();
        if (extendedAuth != null) {
            propertyLength += propertyEncodedLength(extendedAuth.getMethod());
            propertyLength += nullablePropertyEncodedLength(extendedAuth.getRawData());
        }

        propertyLength += omissiblePropertiesLength();

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
        Mqtt5UTF8StringImpl.PROTOCOL_NAME.to(out);
        out.writeByte(PROTOCOL_VERSION);

        int connectFlags = 0;

        final SimpleAuthImpl simpleAuth = message.getRawSimpleAuth();
        if (simpleAuth != null) {
            if (simpleAuth.getRawUsername() != null) {
                connectFlags |= 0b1000_0000;
            }
            if (simpleAuth.getRawPassword() != null) {
                connectFlags |= 0b0100_0000;
            }
        }

        final Mqtt5WillPublishImpl willPublish = message.getRawWillPublish();
        if (willPublish != null) {
            if (willPublish.isRetain()) {
                connectFlags |= 0b0010_0000;
            }
            connectFlags |= willPublish.getQos().getCode() << 3;
            connectFlags |= 0b0000_0100;
        }

        if (message.isCleanStart()) {
            connectFlags |= 0b0000_0010;
        }

        out.writeByte(connectFlags);

        out.writeShort(message.getKeepAlive());

        encodeProperties(out);
    }

    private void encodeProperties(@NotNull final ByteBuf out) {
        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength(Mqtt5DataTypes.MAXIMUM_PACKET_SIZE_LIMIT), out);

        encodeIntProperty(
                SESSION_EXPIRY_INTERVAL, message.getSessionExpiryInterval(), DEFAULT_SESSION_EXPIRY_INTERVAL, out);
        encodeBooleanProperty(REQUEST_RESPONSE_INFORMATION, message.isResponseInformationRequested(),
                DEFAULT_RESPONSE_INFORMATION_REQUESTED, out);
        encodeBooleanProperty(REQUEST_PROBLEM_INFORMATION, message.isProblemInformationRequested(),
                DEFAULT_PROBLEM_INFORMATION_REQUESTED, out);

        final Mqtt5ExtendedAuthImpl extendedAuth = message.getRawExtendedAuth();
        if (extendedAuth != null) {
            encodeProperty(AUTHENTICATION_METHOD, extendedAuth.getMethod(), out);
            encodeNullableProperty(AUTHENTICATION_DATA, extendedAuth.getRawData(), out);
        }

        final RestrictionsImpl restrictions = message.getRestrictions();
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
        message.getRawClientIdentifier().to(out);

        encodeWillPublish(out);

        final SimpleAuthImpl simpleAuth = message.getRawSimpleAuth();
        if (simpleAuth != null) {
            encodeNullable(simpleAuth.getRawUsername(), out);
            encodeNullable(simpleAuth.getRawPassword(), out);
        }
    }

    private void encodeWillPublish(@NotNull final ByteBuf out) {
        final Mqtt5WillPublishImpl willPublish = message.getRawWillPublish();
        if (willPublish != null) {
            Mqtt5DataTypes.encodeVariableByteInteger(willPropertyLength(), out);

            willPublish.getEncoder().encodeFixedProperties(out);
            willPublish.getUserProperties().encode(out);
            encodeIntProperty(Mqtt5WillPublishProperty.WILL_DELAY_INTERVAL, willPublish.getDelayInterval(),
                    Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL, out);

            willPublish.getTopic().to(out);
            encodeOrEmpty(willPublish.getRawPayload(), out);
        }
    }

}
