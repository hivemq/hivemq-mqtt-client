package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5WillPublish;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
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

    private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 6 + 1 + 1 + 2;
    private static final byte PROTOCOL_VERSION = 5;

    @Override
    public void encode(
            @NotNull final Mqtt5ConnectImpl connect, @NotNull final Channel channel, @NotNull final ByteBuf out) {

        final int propertyLength = calculatePropertyLength(connect);
        final int willPropertyLength = calculateWillPropertyLength(connect);
        final int remainingLength = calculateRemainingLength(connect, propertyLength, willPropertyLength);

        final int fixedHeaderLength = 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(remainingLength);
        final int packetSize = fixedHeaderLength + remainingLength;
        final Integer maximumPacketSize = channel.attr(ChannelAttributes.MAXIMUM_OUTGOING_PACKET_SIZE_KEY).get();
        if ((maximumPacketSize != null) && (packetSize > maximumPacketSize)) {
            // TODO: exception maximum packet size exceeded
        }

        encodeFixedHeader(remainingLength, out);
        encodeVariableHeader(connect, propertyLength, out);
        encodePayload(connect, willPropertyLength, out);
    }

    private int calculateRemainingLength(
            @NotNull final Mqtt5ConnectImpl connect, final int propertiesLength, final int willPropertiesLength) {

        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;

        remainingLength += connect.getClientIdentifier().encodedLength();

        final AuthImpl auth = connect.getRawAuth();
        if (auth != AuthImpl.DEFAULT_NO_AUTH) {
            final Mqtt5UTF8String username = auth.getRawUsername();
            if (username != null) {
                remainingLength += username.encodedLength();
            }
            final byte[] password = auth.getRawPassword();
            if (password != null) {
                remainingLength += Mqtt5DataTypes.encodedBinaryDataLength(password);
            }
        }

        final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
        if (willPublish != Mqtt5WillPublishImpl.DEFAULT_NO_WILL_PUBLISH) {
            remainingLength += willPublish.getTopic().encodedLength() +
                    Mqtt5DataTypes.encodedBinaryDataLength(willPublish.getRawPayload()) +
                    Mqtt5DataTypes.encodedVariableByteIntegerLength(willPropertiesLength) +
                    willPropertiesLength;
        }

        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertiesLength) + propertiesLength;

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(remainingLength)) {
            // TODO exception remaining size exceeded
        }

        return remainingLength;
    }

    private int calculatePropertyLength(@NotNull final Mqtt5ConnectImpl connect) {
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
            if (restrictions.getMaximumPacketSize() != Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT) {
                propertyLength += 5;
            }
            if (restrictions.getTopicAliasMaximum() != Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM) {
                propertyLength += 3;
            }
        }

        final AuthImpl auth = connect.getRawAuth();
        if (auth != AuthImpl.DEFAULT_NO_AUTH) {
            final Mqtt5UTF8String method = auth.getRawMethod();
            if (method != null) {
                propertyLength += 1 + method.encodedLength();
            }
            final byte[] data = auth.getRawData();
            if (data != null) {
                propertyLength += 1 + Mqtt5DataTypes.encodedBinaryDataLength(data);
            }
        }

        propertyLength += Mqtt5UserProperty.encodedLength(connect.getUserProperties());

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(propertyLength)) {
            // TODO exception properties size exceeded
        }

        return propertyLength;
    }

    private int calculateWillPropertyLength(@NotNull final Mqtt5ConnectImpl connect) {
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
            final Mqtt5UTF8String responseTopic = willPublish.getRawResponseTopic();
            if (responseTopic != null) {
                willPropertyLength += 1 + responseTopic.encodedLength();
            }
            final byte[] correlationData = willPublish.getRawCorrelationData();
            if (correlationData != null) {
                willPropertyLength += 1 + Mqtt5DataTypes.encodedBinaryDataLength(correlationData);
            }

            willPropertyLength += Mqtt5UserProperty.encodedLength(willPublish.getUserProperties());

            if (willPublish.getDelayInterval() != Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL) {
                willPropertyLength += 5;
            }
        }

        if (!Mqtt5DataTypes.isInVariableByteIntegerRange(willPropertyLength)) {
            // TODO exception will properties size exceeded
        }

        return willPropertyLength;
    }

    private void encodeFixedHeader(final int remainingLength, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        Mqtt5DataTypes.encodeVariableByteInteger(remainingLength, out);
    }

    private void encodeVariableHeader(
            @NotNull final Mqtt5ConnectImpl connect, final int propertyLength, @NotNull final ByteBuf out) {

        Mqtt5UTF8String.PROTOCOL_NAME.to(out);
        out.writeByte(PROTOCOL_VERSION);

        int connectFlags = 0;

        final AuthImpl auth = connect.getRawAuth();
        if (auth != AuthImpl.DEFAULT_NO_AUTH) {
            if (auth.getRawUsername() != null) {
                connectFlags |= 0b1000_0000;
            }
            if (auth.getRawPassword() != null) {
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

        encodeProperties(connect, propertyLength, out);
    }

    private void encodeProperties(
            @NotNull final Mqtt5ConnectImpl connect, final int propertyLength, @NotNull final ByteBuf out) {

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

        final AuthImpl auth = connect.getRawAuth();
        if (auth != AuthImpl.DEFAULT_NO_AUTH) {
            final Mqtt5UTF8String method = auth.getRawMethod();
            if (method != null) {
                out.writeByte(Mqtt5ConnectProperty.AUTHENTICATION_METHOD);
                method.to(out);
            }
            final byte[] data = auth.getRawData();
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
            }
            final long topicAliasMaximum = restrictions.getTopicAliasMaximum();
            if (topicAliasMaximum != Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM) {
                out.writeByte(Mqtt5ConnectProperty.TOPIC_ALIAS_MAXIMUM);
                out.writeInt((int) topicAliasMaximum);
            }
            final int maximumPacketSize = restrictions.getMaximumPacketSize();
            if (maximumPacketSize != Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT) {
                out.writeByte(Mqtt5ConnectProperty.MAXIMUM_PACKET_SIZE);
                out.writeShort(maximumPacketSize);
            }
        }

        Mqtt5UserProperty.encode(connect.getUserProperties(), out);
    }

    private void encodePayload(
            @NotNull final Mqtt5ConnectImpl connect, final int willPropertyLength, @NotNull final ByteBuf out) {

        connect.getClientIdentifier().to(out);

        encodeWillPublish(connect, willPropertyLength, out);

        final AuthImpl auth = connect.getRawAuth();
        if (auth != AuthImpl.DEFAULT_NO_AUTH) {
            final Mqtt5UTF8String username = auth.getRawUsername();
            if (username != null) {
                username.to(out);
            }
            final byte[] password = auth.getRawPassword();
            if (password != null) {
                Mqtt5DataTypes.encodeBinaryData(password, out);
            }
        }
    }

    private void encodeWillPublish(
            @NotNull final Mqtt5ConnectImpl connect, final int willPropertyLength, @NotNull final ByteBuf out) {

        final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
        if (willPublish != Mqtt5WillPublishImpl.DEFAULT_NO_WILL_PUBLISH) {

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
            final Mqtt5UTF8String responseTopic = willPublish.getRawResponseTopic();
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
