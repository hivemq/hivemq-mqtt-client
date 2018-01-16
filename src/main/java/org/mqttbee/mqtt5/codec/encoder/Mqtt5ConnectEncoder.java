package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5WillPublish;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.exceptions.Mqtt5BinaryDataExceededException;
import org.mqttbee.mqtt5.exceptions.Mqtt5VariableByteIntegerExceededException;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5Topic;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.auth.Mqtt5ExtendedAuthImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectProperty;
import org.mqttbee.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishProperty;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnectEncoder implements Mqtt5MessageEncoder<Mqtt5ConnectImpl> {

    public static final Mqtt5ConnectEncoder INSTANCE = new Mqtt5ConnectEncoder();

    private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 6 + 1 + 1 + 2;
    private static final byte PROTOCOL_VERSION = 5;

    @Override
    public void encode(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        encodeFixedHeader(connect, out);
        encodeVariableHeader(connect, out, channel);
        encodePayload(connect, out);
    }

    public int encodedRemainingLength(@NotNull final Mqtt5ConnectImpl connect) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        remainingLength += connect.getRawClientIdentifier().encodedLength();

        final SimpleAuthImpl simpleAuth = connect.getRawSimpleAuth();
        if (simpleAuth != null) {
            final Mqtt5UTF8String username = simpleAuth.getRawUsername();
            if (username != null) {
                remainingLength += username.encodedLength();
            }
            final byte[] password = simpleAuth.getRawPassword();
            if (password != null) {
                if (!Mqtt5DataTypes.isInBinaryDataRange(password)) {
                    throw new Mqtt5BinaryDataExceededException("password");
                }
                remainingLength += Mqtt5DataTypes.encodedBinaryDataLength(password);
            }
        }

        final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
        if (willPublish != Mqtt5WillPublishImpl.DEFAULT_NO_WILL_PUBLISH) {
            final byte[] willPayload = willPublish.getRawPayload();
            if (!Mqtt5DataTypes.isInBinaryDataRange(willPayload)) {
                throw new Mqtt5BinaryDataExceededException("will payload");
            }

            final int willPropertyLength = connect.encodedWillPropertyLength();
            remainingLength += willPublish.getTopic().encodedLength() +
                    Mqtt5DataTypes.encodedBinaryDataLength(willPayload) +
                    Mqtt5DataTypes.encodedVariableByteIntegerLength(willPropertyLength) + willPropertyLength;
        }

        final int propertyLength = connect.encodedPropertyLength();
        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertyLength) + propertyLength;

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("remaining length");
        }
        return remainingLength;
    }

    public int encodedPropertyLength(@NotNull final Mqtt5ConnectImpl connect) {
        int propertyLength = 0;

        if (connect.getSessionExpiryInterval() != DEFAULT_SESSION_EXPIRY_INTERVAL) {
            propertyLength += 5;
        }
        if (connect.isResponseInformationRequested() != DEFAULT_RESPONSE_INFORMATION_REQUESTED) {
            propertyLength += 2;
        }
        if (connect.isProblemInformationRequested() != DEFAULT_PROBLEM_INFORMATION_REQUESTED) {
            propertyLength += 2;
        }

        final RestrictionsImpl restrictions = connect.getRawRestrictions();
        if (restrictions != RestrictionsImpl.DEFAULT) {
            if (restrictions.getReceiveMaximum() != Restrictions.DEFAULT_RECEIVE_MAXIMUM) {
                propertyLength += 3;
            }
            if (restrictions.getTopicAliasMaximum() != Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM) {
                propertyLength += 3;
            }
            if (restrictions.getMaximumPacketSize() != Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT) {
                propertyLength += 5;
            }
        }

        final Mqtt5ExtendedAuthImpl extendedAuth = connect.getRawExtendedAuth();
        if (extendedAuth != null) {
            propertyLength += 1 + extendedAuth.getMethod().encodedLength();

            final byte[] data = extendedAuth.getRawData();
            if (data != null) {
                if (!Mqtt5DataTypes.isInBinaryDataRange(data)) {
                    throw new Mqtt5BinaryDataExceededException("authentication data");
                }
                propertyLength += 1 + Mqtt5DataTypes.encodedBinaryDataLength(data);
            }
        }

        propertyLength += Mqtt5UserProperty.encodedLength(connect.getUserProperties());

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            throw new Mqtt5VariableByteIntegerExceededException("property length");
        }
        return propertyLength;
    }

    public int encodedWillPropertyLength(@NotNull final Mqtt5ConnectImpl connect) {
        int willPropertyLength = 0;

        final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
        if (willPublish != Mqtt5WillPublishImpl.DEFAULT_NO_WILL_PUBLISH) {
            if (willPublish.getMessageExpiryInterval() != Mqtt5WillPublish.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY) {
                willPropertyLength += 5;
            }
            if (willPublish.getRawPayloadFormatIndicator() != null) {
                willPropertyLength += 2;
            }
            final Mqtt5UTF8String contentType = willPublish.getRawContentType();
            if (contentType != null) {
                willPropertyLength += 1 + contentType.encodedLength();
            }
            final Mqtt5Topic responseTopic = willPublish.getRawResponseTopic();
            if (responseTopic != null) {
                willPropertyLength += 1 + responseTopic.encodedLength();
            }
            final byte[] correlationData = willPublish.getRawCorrelationData();
            if (correlationData != null) {
                if (!Mqtt5DataTypes.isInBinaryDataRange(correlationData)) {
                    throw new Mqtt5BinaryDataExceededException("will correlation data");
                }
                willPropertyLength += 1 + Mqtt5DataTypes.encodedBinaryDataLength(correlationData);
            }

            willPropertyLength += Mqtt5UserProperty.encodedLength(willPublish.getUserProperties());

            if (willPublish.getDelayInterval() != Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL) {
                willPropertyLength += 5;
            }

            if (!Mqtt5DataTypes.isInVariableByteIntegerRange(willPropertyLength)) {
                throw new Mqtt5VariableByteIntegerExceededException("will properties length");
            }
        }

        return willPropertyLength;
    }

    private void encodeFixedHeader(@NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(connect.encodedRemainingLength(), out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out, @NotNull final Channel channel) {

        Mqtt5UTF8String.PROTOCOL_NAME.to(out);
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

        encodeProperties(connect, out, channel);
    }

    private void encodeProperties(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out, @NotNull final Channel channel) {

        final int propertyLength = connect.encodedPropertyLength();
        Mqtt5DataTypes.encodeVariableByteInteger(propertyLength, out);

        final long sessionExpiryInterval = connect.getSessionExpiryInterval();
        if (sessionExpiryInterval != DEFAULT_SESSION_EXPIRY_INTERVAL) {
            out.writeByte(Mqtt5ConnectProperty.SESSION_EXPIRY_INTERVAL);
            out.writeInt((int) sessionExpiryInterval);
        }

        final boolean responseInformationRequested = connect.isResponseInformationRequested();
        if (responseInformationRequested != DEFAULT_RESPONSE_INFORMATION_REQUESTED) {
            out.writeByte(Mqtt5ConnectProperty.REQUEST_RESPONSE_INFORMATION);
            out.writeByte(NOT_DEFAULT_RESPONSE_INFORMATION_REQUESTED);
        }

        final boolean problemInformationRequested = connect.isProblemInformationRequested();
        if (problemInformationRequested != DEFAULT_PROBLEM_INFORMATION_REQUESTED) {
            out.writeByte(Mqtt5ConnectProperty.REQUEST_PROBLEM_INFORMATION);
            out.writeByte(NOT_DEFAULT_PROBLEM_INFORMATION_REQUESTED);
        }

        final Mqtt5ExtendedAuthImpl extendedAuth = connect.getRawExtendedAuth();
        if (extendedAuth != null) {
            out.writeByte(Mqtt5ConnectProperty.AUTHENTICATION_METHOD);
            extendedAuth.getMethod().to(out);

            final byte[] data = extendedAuth.getRawData();
            if (data != null) {
                out.writeByte(Mqtt5ConnectProperty.AUTHENTICATION_DATA);
                Mqtt5DataTypes.encodeBinaryData(data, out);
            }
        }

        final RestrictionsImpl restrictions = connect.getRawRestrictions();
        if (restrictions != RestrictionsImpl.DEFAULT) {
            final int receiveMaximum = restrictions.getReceiveMaximum();
            if (receiveMaximum != Restrictions.DEFAULT_RECEIVE_MAXIMUM) {
                out.writeByte(Mqtt5ConnectProperty.RECEIVE_MAXIMUM);
                out.writeShort(receiveMaximum);
                channel.attr(ChannelAttributes.INCOMING_RECEIVE_MAXIMUM).set(receiveMaximum);
            }
            final int topicAliasMaximum = restrictions.getTopicAliasMaximum();
            if (topicAliasMaximum != Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM) {
                out.writeByte(Mqtt5ConnectProperty.TOPIC_ALIAS_MAXIMUM);
                out.writeShort(topicAliasMaximum);
                channel.attr(ChannelAttributes.INCOMING_TOPIC_ALIAS_MAPPING)
                        .set(new Mqtt5Topic[topicAliasMaximum]);
            }
            final long maximumPacketSize = restrictions.getMaximumPacketSize();
            if (maximumPacketSize != Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT) {
                out.writeByte(Mqtt5ConnectProperty.MAXIMUM_PACKET_SIZE);
                out.writeInt((int) maximumPacketSize);
                channel.attr(ChannelAttributes.INCOMING_MAXIMUM_PACKET_SIZE).set(maximumPacketSize);
            }
        }

        Mqtt5UserProperty.encode(connect.getUserProperties(), out);
    }

    private void encodePayload(@NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {
        connect.getRawClientIdentifier().to(out);

        encodeWillPublish(connect, out);

        final SimpleAuthImpl simpleAuth = connect.getRawSimpleAuth();
        if (simpleAuth != null) {
            final Mqtt5UTF8String username = simpleAuth.getRawUsername();
            if (username != null) {
                username.to(out);
            }
            final byte[] password = simpleAuth.getRawPassword();
            if (password != null) {
                Mqtt5DataTypes.encodeBinaryData(password, out);
            }
        }
    }

    private void encodeWillPublish(@NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {
        final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
        if (willPublish != Mqtt5WillPublishImpl.DEFAULT_NO_WILL_PUBLISH) {

            final int willPropertyLength = connect.encodedWillPropertyLength();
            Mqtt5DataTypes.encodeVariableByteInteger(willPropertyLength, out);

            final long messageExpiryInterval = willPublish.getMessageExpiryInterval();
            if (messageExpiryInterval != Mqtt5WillPublish.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY) {
                out.writeByte(Mqtt5WillPublishProperty.MESSAGE_EXPIRY_INTERVAL);
                out.writeInt((int) messageExpiryInterval);
            }
            final Mqtt5PayloadFormatIndicator payloadFormatIndicator = willPublish.getRawPayloadFormatIndicator();
            if (payloadFormatIndicator != null) {
                out.writeByte(Mqtt5WillPublishProperty.PAYLOAD_FORMAT_INDICATOR);
                out.writeByte(payloadFormatIndicator.getCode());
            }
            final Mqtt5UTF8String contentType = willPublish.getRawContentType();
            if (contentType != null) {
                out.writeByte(Mqtt5WillPublishProperty.CONTENT_TYPE);
                contentType.to(out);
            }
            final Mqtt5Topic responseTopic = willPublish.getRawResponseTopic();
            if (responseTopic != null) {
                out.writeByte(Mqtt5WillPublishProperty.RESPONSE_TOPIC);
                responseTopic.to(out);
            }
            final byte[] correlationData = willPublish.getRawCorrelationData();
            if (correlationData != null) {
                out.writeByte(Mqtt5WillPublishProperty.CORRELATION_DATA);
                Mqtt5DataTypes.encodeBinaryData(correlationData, out);
            }

            Mqtt5UserProperty.encode(willPublish.getUserProperties(), out);

            final long delayInterval = willPublish.getDelayInterval();
            if (delayInterval != Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL) {
                out.writeByte(Mqtt5WillPublishProperty.WILL_DELAY_INTERVAL);
                out.writeInt((int) delayInterval);
            }

            willPublish.getTopic().to(out);
            Mqtt5DataTypes.encodeBinaryData(willPublish.getRawPayload(), out);
        }
    }

}
