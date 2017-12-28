package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckReasonCode;

import javax.inject.Singleton;

import java.util.LinkedList;
import java.util.List;

import static org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckProperty.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnackDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0;
    private static final int MIN_REMAINING_LENGTH = 0;

    @Override
    @Nullable
    public Mqtt5ConnAck decode(final int flags, final int remainingLength, @NotNull final ByteBuf in) {
        if (flags != FLAGS) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        final short connackFlags = in.readUnsignedByte();
        if ((connackFlags & 0xFE) != 0) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }
        final boolean sessionPresent = (connackFlags & 0x1) != 0;

        final Mqtt5ConnAckReasonCode connackReasonCode = Mqtt5ConnAckReasonCode.fromCode(in.readUnsignedByte());
        if (connackReasonCode == null) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        final int propertiesLength = Mqtt5DataTypes.decodeVariableByteInteger(in);

        if (propertiesLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_TOO_LARGE ||
                propertiesLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES ||
                propertiesLength == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        if (in.readableBytes() != propertiesLength) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        long sessionExpiryInterval = -1; // default from Connect
        String clientIdenttifier = null; // default from Connect
        int serverKeepAlive = -1; // default from Connect
        String authenticationMethod = null;
        byte[] authenticationData = null;
        String responseInformation = null;
        String serverReference = null;
        String reasonString = null;
        int receiveMaximum = -1; // default = 65_535
        int topicAliasMaximum = -1; // default = 0
        byte maximumQoS = -1; // default = 2
        byte retainAvailable = -1; // default = 1
        List<Mqtt5UserProperty> userProperties = null;
        long maximumPacketSize = -1; // default = infinity
        byte wildCardSubscriptionAvailable = -1; // default = 1
        byte subscriptionIdentifierAvailable = -1; // default = 1
        byte sharedSubscriptionAvailable = -1; // default = 1

        while (in.readableBytes() > 0) {
            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);

            if (propertyIdentifier == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_TOO_LARGE ||
                    propertyIdentifier == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_MINIMUM_BYTES ||
                    propertyIdentifier == Mqtt5DataTypes.VARIABLE_BYTE_INTEGER_NOT_ENOUGH_BYTES) {
                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                in.clear();
                return null;
            }

            switch (propertyIdentifier) {
                case SESSION_EXPIRY_INTERVAL:
                    if (sessionExpiryInterval != -1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 4) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    sessionExpiryInterval = in.readUnsignedInt();
                    break;
                case ASSIGNED_CLIENT_IDENTIFIER:
                    if (clientIdenttifier != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    clientIdenttifier = Mqtt5DataTypes.decodeUTF8String(in);
                    if (clientIdenttifier == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case SERVER_KEEP_ALIVE:
                    if (serverKeepAlive != -1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 2) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    serverKeepAlive = in.readUnsignedShort();
                    break;
                case AUTHENTICATION_METHOD:
                    if (authenticationMethod != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    authenticationMethod = Mqtt5DataTypes.decodeUTF8String(in);
                    if (authenticationMethod == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case AUTHENTICATION_DATA:
                    if (authenticationData != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    authenticationData = Mqtt5DataTypes.decodeBinaryData(in);
                    if (authenticationData == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case RESPONSE_INFORMATION:
                    if (responseInformation != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    responseInformation = Mqtt5DataTypes.decodeUTF8String(in);
                    if (responseInformation == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case SERVER_REFERENCE:
                    if (serverReference != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    serverReference = Mqtt5DataTypes.decodeUTF8String(in);
                    if (serverReference == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case REASON_STRING:
                    if (reasonString != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    reasonString = Mqtt5DataTypes.decodeUTF8String(in);
                    if (reasonString == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case RECEIVE_MAXIMUM:
                    if (receiveMaximum != -1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 2) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    receiveMaximum = in.readUnsignedShort();
                    if (receiveMaximum == 0) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case TOPIC_ALIAS_MAXIMUM:
                    if (topicAliasMaximum != -1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 2) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    topicAliasMaximum = in.readUnsignedShort();
                    break;
                case MAXIMUM_QOS:
                    if (maximumQoS != -1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 1) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    maximumQoS = in.readByte();
                    if (maximumQoS != 0 && maximumQoS != 1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case RETAIN_AVAILABLE:
                    if (retainAvailable != -1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 1) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    retainAvailable = in.readByte();
                    if (retainAvailable != 0 && retainAvailable != 1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case USER_PROPERTY:
                    if (userProperties == null) {
                        userProperties = new LinkedList<>();
                    }
                    final String userPropertyName = Mqtt5DataTypes.decodeUTF8String(in);
                    if (userPropertyName == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    final String userPropertyValue = Mqtt5DataTypes.decodeUTF8String(in);
                    if (userPropertyValue == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    userProperties.add(new Mqtt5UserProperty(userPropertyName, userPropertyValue));
                    break;
                case MAXIMUM_PACKET_SIZE:
                    if (maximumPacketSize != -1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 4) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    maximumPacketSize = in.readUnsignedInt();
                    if (maximumPacketSize == 0) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case WILDCARD_SUBSCRIPTION_AVAILABLE:
                    if (wildCardSubscriptionAvailable != -1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 1) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    wildCardSubscriptionAvailable = in.readByte();
                    if (wildCardSubscriptionAvailable != 0 && wildCardSubscriptionAvailable != 1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case SUBSCRIPTION_IDENTIFIER_AVAILABLE:
                    if (subscriptionIdentifierAvailable != -1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 1) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    subscriptionIdentifierAvailable = in.readByte();
                    if (subscriptionIdentifierAvailable != 0 && subscriptionIdentifierAvailable != 1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case SHARED_SUBSCRIPTION_AVAILABLE:
                    if (sharedSubscriptionAvailable != -1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 1) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    sharedSubscriptionAvailable = in.readByte();
                    if (sharedSubscriptionAvailable != 0 && sharedSubscriptionAvailable != 1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    break;
                default:
                    // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                    in.clear();
                    return null;
            }
        }

        if (in.isReadable()) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        return null;
    }

}
