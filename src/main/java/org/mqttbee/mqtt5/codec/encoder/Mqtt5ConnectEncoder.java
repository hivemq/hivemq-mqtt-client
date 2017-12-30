package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.Mqtt5Connect;
import org.mqttbee.api.mqtt5.message.Mqtt5WillPublish;
import org.mqttbee.mqtt5.ChannelAttributes;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.connect.Mqtt5ConnectImpl;
import org.mqttbee.mqtt5.message.publish.Mqtt5WillPublishImpl;

import javax.inject.Singleton;
import java.util.List;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnectEncoder {

    private static final int FIXED_HEADER = Mqtt5MessageType.CONNECT.getCode() << 4;
    private static final int VARIABLE_HEADER_FIXED_LENGTH = 6 + 1 + 1 + 2;
    private static final byte PROTOCOL_VERSION = 5;

    public void encode(@NotNull final Channel channel, @NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {
        encodeFixedHeader(channel, connect, out);
        encodeVariableHeader(connect, out);
        encodePayload(connect, out);
    }

    private void encodeFixedHeader(
            @NotNull final Channel channel, @NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {
        out.writeByte(FIXED_HEADER);

        final int remainingLength = calculateRemainingLength(connect);
        final int fixedHeaderLength = 1 + Mqtt5DataTypes.encodedVariableByteIntegerLength(remainingLength);
        final int packetSize = fixedHeaderLength + remainingLength;
        final Integer maximumPacketSize = channel.attr(ChannelAttributes.MAXIMUM_OUTGOIING_PACKET_SIZE_KEY).get();
        if ((maximumPacketSize != null) && (packetSize > maximumPacketSize)) {
            // TODO: exception
        }

        Mqtt5DataTypes.encodeVariableByteInteger(remainingLength, out);
    }

    private int calculateRemainingLength(@NotNull final Mqtt5ConnectImpl connect) {
        int remainingLength = VARIABLE_HEADER_FIXED_LENGTH;
        int propertiesLength = 0;
        int willPropertiesLength = 0;

        remainingLength += connect.getClientIdentifier().encodedLength();

        if (connect.getSessionExpiryInterval() != Mqtt5Connect.DEFAULT_SESSION_EXPIRY_INTERVAL) {
            propertiesLength += 5;
        }

        if (connect.isResponseInformationRequested() != Mqtt5Connect.DEFAULT_RESPONSE_INFORMATION_REQUESTED) {
            propertiesLength += 2;
        }

        if (connect.isProblemInformationRequested() != Mqtt5Connect.DEFAULT_PROBLEM_INFORMATION_REQUESTED) {
            propertiesLength += 2;
        }

        final Mqtt5ConnectImpl.RestrictionsImpl restrictions = connect.getRawRestrictions();
        if (restrictions != Mqtt5Connect.Restrictions.DEFAULT) {
            if (restrictions.getReceiveMaximum() != Mqtt5Connect.Restrictions.DEFAULT_RECEIVE_MAXIMUM) {
                propertiesLength += 3;
            }
            if (restrictions.getMaximumPacketSize() != Mqtt5Connect.Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_INFINITY) {
                propertiesLength += 5;
            }
            if (restrictions.getTopicAliasMaximum() != Mqtt5Connect.Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM) {
                propertiesLength += 3;
            }
        }

        final Mqtt5ConnectImpl.AuthImpl auth = connect.getRawAuth();
        if (auth != Mqtt5Connect.Auth.DEFAULT_NO_AUTH) {
            final Mqtt5UTF8String username = auth.getRawUsername();
            if (username != null) {
                remainingLength += username.encodedLength();
            }
            final byte[] password = auth.getRawPassword();
            if (password != null) {
                remainingLength += Mqtt5DataTypes.encodedBinaryDataLength(password);
            }
            final Mqtt5UTF8String method = auth.getRawMethod();
            if (method != null) {
                propertiesLength += 1 + method.encodedLength();
            }
            final byte[] data = auth.getRawData();
            if (data != null) {
                propertiesLength += 1 + Mqtt5DataTypes.encodedBinaryDataLength(data);
            }
        }

        final List<Mqtt5UserProperty> userProperties = connect.getUserProperties();
        if (userProperties != Mqtt5UserProperty.NO_USER_PROPERTIES) {
            propertiesLength += Mqtt5UserProperty.encodedLength(userProperties);
        }

        final Mqtt5WillPublishImpl willPublish = connect.getRawWillPublish();
        if (willPublish != Mqtt5WillPublishImpl.DEFAULT_NO_WILL_PUBLISH) {
            remainingLength += willPublish.getTopic().encodedLength();
            remainingLength += Mqtt5DataTypes.encodedBinaryDataLength(willPublish.getRawPayload());

            if (willPublish.getMessageExpiryInterval() != Mqtt5WillPublish.DEFAULT_MESSAGE_EXPIRY_INTERVAL_INFINITY) {
                willPropertiesLength += 5;
            }
            if (willPublish.getRawPayloadFormatIndicator() != null) {
                willPropertiesLength += 2;
            }
            final Mqtt5UTF8String contentType = willPublish.getRawContentType();
            if (contentType != null) {
                willPropertiesLength += 1 + contentType.encodedLength();
            }
            final Mqtt5UTF8String responseTopic = willPublish.getRawResponseTopic();
            if (responseTopic != null) {
                willPropertiesLength += 1 + responseTopic.encodedLength();
            }
            final byte[] correlationData = willPublish.getRawCorrelationData();
            if (correlationData != null) {
                willPropertiesLength += 1 + Mqtt5DataTypes.encodedBinaryDataLength(correlationData);
            }
            final List<Mqtt5UserProperty> willUserProperties = willPublish.getUserProperties();
            if (userProperties != Mqtt5UserProperty.NO_USER_PROPERTIES) {
                willPropertiesLength += Mqtt5UserProperty.encodedLength(willUserProperties);
            }
            if (willPublish.getDelayInterval() != Mqtt5WillPublish.DEFAULT_DELAY_INTERVAL) {
                willPropertiesLength += 5;
            }
        }

        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(propertiesLength) + propertiesLength;
        remainingLength += Mqtt5DataTypes.encodedVariableByteIntegerLength(willPropertiesLength) + willPropertiesLength;

        return remainingLength;
    }

    private void encodeVariableHeader(@NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {
        Mqtt5UTF8String.PROTOCOL_NAME.to(out);
        out.writeByte(PROTOCOL_VERSION);

        int connectFlags = 0;

        final Mqtt5ConnectImpl.AuthImpl auth = connect.getRawAuth();
        if (auth != Mqtt5Connect.Auth.DEFAULT_NO_AUTH) {
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

        // Properties
//        SESSION_EXPIRY_INTERVAL
//        AUTHENTICATION_METHOD
//        AUTHENTICATION_DATA
//        REQUEST_PROBLEM_INFORMATION
//        REQUEST_RESPONSE_INFORMATION
//        RECEIVE_MAXIMUM
//        TOPIC_ALIAS_MAXIMUM
//        USER_PROPERTY
//        MAXIMUM_PACKET_SIZE
    }

    private void encodePayload(@NotNull final Mqtt5ConnectImpl connect, @NotNull final ByteBuf out) {

    }

}
