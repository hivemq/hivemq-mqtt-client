package org.mqttbee.mqtt5.codec.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAck;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckReasonCode;

import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;

import static org.mqttbee.mqtt5.message.connack.Mqtt5ConnAck.*;
import static org.mqttbee.mqtt5.message.connack.Mqtt5ConnAck.Restrictions.*;
import static org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckProperty.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnAckDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3;

    @Nullable
    @Override
    public Mqtt5ConnAck decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
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

        final short connAckFlags = in.readUnsignedByte();
        if ((connAckFlags & 0xFE) != 0) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }
        final boolean sessionPresent = (connAckFlags & 0x1) != 0;

        final Mqtt5ConnAckReasonCode reasonCode = Mqtt5ConnAckReasonCode.fromCode(in.readUnsignedByte());
        if (reasonCode == null) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        final int propertiesLength = Mqtt5DataTypes.decodeVariableByteInteger(in);

        if (propertiesLength < 0) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        if (in.readableBytes() != propertiesLength) {
            // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
            in.clear();
            return null;
        }

        long sessionExpiryInterval = SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
        Mqtt5ClientIdentifier assignedClientIdentifier = CLIENT_IDENTIFIER_FROM_CONNECT;
        int serverKeepAlive = KEEP_ALIVE_FROM_CONNECT;

        Mqtt5UTF8String authenticationMethod = null;
        byte[] authenticationData = null;

        int receiveMaximum = DEFAULT_RECEIVE_MAXIMUM;
        boolean receiveMaximumPresent = false;
        int topicAliasMaximum = DEFAULT_TOPIC_ALIAS_MAXIMUM;
        boolean topicAliasMaximumPresent = false;
        byte maximumQoS = DEFAULT_MAXIMUM_QOS;
        boolean maximumQoSPresent = false;
        boolean retainAvailable = DEFAULT_RETAIN_AVAILABLE;
        boolean retainAvailablePresent = false;
        long maximumPacketSize = DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;
        boolean maximumPacketSizePresent = false;
        boolean wildCardSubscriptionAvailable = DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE;
        boolean wildCardSubscriptionAvailablePresent = false;
        boolean subscriptionIdentifierAvailable = DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE;
        boolean subscriptionIdentifierAvailablePresent = false;
        boolean sharedSubscriptionAvailable = DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE;
        boolean sharedSubscriptionAvailablePresent = false;

        Mqtt5UTF8String responseInformation = null;
        Mqtt5UTF8String serverReference = null;
        Mqtt5UTF8String reasonString = null;
        List<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES;

        boolean authPresent = false;
        boolean restrictionsPresent = false;

        while (in.readableBytes() > 0) {
            final int propertyIdentifier = Mqtt5DataTypes.decodeVariableByteInteger(in);

            if (propertyIdentifier < 0) {
                // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                in.clear();
                return null;
            }

            switch (propertyIdentifier) {
                case SESSION_EXPIRY_INTERVAL:
                    if (sessionExpiryInterval != SESSION_EXPIRY_INTERVAL_FROM_CONNECT) {
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
                    if (assignedClientIdentifier != CLIENT_IDENTIFIER_FROM_CONNECT) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    assignedClientIdentifier = Mqtt5ClientIdentifier.from(in);
                    if (assignedClientIdentifier == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case SERVER_KEEP_ALIVE:
                    if (serverKeepAlive != KEEP_ALIVE_FROM_CONNECT) {
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
                    authenticationMethod = Mqtt5UTF8String.from(in);
                    if (authenticationMethod == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    authPresent = true;
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
                    authPresent = true;
                    break;
                case RECEIVE_MAXIMUM:
                    if (receiveMaximumPresent) {
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
                    receiveMaximumPresent = true;
                    restrictionsPresent = true;
                    break;
                case TOPIC_ALIAS_MAXIMUM:
                    if (topicAliasMaximumPresent) {
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
                    topicAliasMaximumPresent = true;
                    restrictionsPresent = true;
                    break;
                case MAXIMUM_QOS:
                    if (maximumQoSPresent) {
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
                    maximumQoSPresent = true;
                    restrictionsPresent = true;
                    break;
                case RETAIN_AVAILABLE:
                    if (retainAvailablePresent) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 1) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    final byte retainAvailableByte = in.readByte();
                    if (retainAvailableByte != 0 && retainAvailableByte != 1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    retainAvailable = retainAvailableByte == 1;
                    retainAvailablePresent = true;
                    restrictionsPresent = true;
                    break;
                case MAXIMUM_PACKET_SIZE:
                    if (maximumPacketSizePresent) {
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
                    maximumPacketSizePresent = true;
                    restrictionsPresent = true;
                    break;
                case WILDCARD_SUBSCRIPTION_AVAILABLE:
                    if (wildCardSubscriptionAvailablePresent) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 1) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    final byte wildCardSubscriptionAvailableByte = in.readByte();
                    if (wildCardSubscriptionAvailableByte != 0 && wildCardSubscriptionAvailableByte != 1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    wildCardSubscriptionAvailable = wildCardSubscriptionAvailableByte == 1;
                    wildCardSubscriptionAvailablePresent = true;
                    restrictionsPresent = true;
                    break;
                case SUBSCRIPTION_IDENTIFIER_AVAILABLE:
                    if (subscriptionIdentifierAvailablePresent) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 1) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    final byte subscriptionIdentifierAvailableByte = in.readByte();
                    if (subscriptionIdentifierAvailableByte != 0 && subscriptionIdentifierAvailableByte != 1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    subscriptionIdentifierAvailable = subscriptionIdentifierAvailableByte == 1;
                    subscriptionIdentifierAvailablePresent = true;
                    restrictionsPresent = true;
                    break;
                case SHARED_SUBSCRIPTION_AVAILABLE:
                    if (sharedSubscriptionAvailablePresent) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    if (in.readableBytes() < 1) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    final byte sharedSubscriptionAvailableByte = in.readByte();
                    if (sharedSubscriptionAvailableByte != 0 && sharedSubscriptionAvailableByte != 1) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    sharedSubscriptionAvailable = sharedSubscriptionAvailableByte == 1;
                    sharedSubscriptionAvailablePresent = true;
                    restrictionsPresent = true;
                    break;
                case RESPONSE_INFORMATION:
                    if (responseInformation != null) {
                        // TODO: send Disconnect with reason code 0x82 Protocol Error and close channel
                        in.clear();
                        return null;
                    }
                    responseInformation = Mqtt5UTF8String.from(in);
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
                    serverReference = Mqtt5UTF8String.from(in);
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
                    reasonString = Mqtt5UTF8String.from(in);
                    if (reasonString == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    break;
                case USER_PROPERTY:
                    if (userProperties == Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES) {
                        userProperties = new LinkedList<>();
                    }
                    final Mqtt5UserProperty userProperty = Mqtt5UserProperty.decode(in);
                    if (userProperty == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    userProperties.add(userProperty);
                    break;
                default:
                    // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                    in.clear();
                    return null;
            }
        }

        Mqtt5ConnAck.Auth auth = Mqtt5ConnAck.Auth.DEFAULT_NO_AUTH;
        if (authPresent) {
            auth = new Mqtt5ConnAck.Auth(authenticationMethod, authenticationData);
        }

        Mqtt5ConnAck.Restrictions restrictions = Mqtt5ConnAck.Restrictions.DEFAULT;
        if (restrictionsPresent) {
            restrictions = new Mqtt5ConnAck.Restrictions(
                    receiveMaximum, topicAliasMaximum, maximumPacketSize, maximumQoS, retainAvailable,
                    wildCardSubscriptionAvailable, subscriptionIdentifierAvailable, sharedSubscriptionAvailable);
        }

        return new Mqtt5ConnAck(
                reasonCode, reasonString, sessionPresent, sessionExpiryInterval, serverKeepAlive,
                assignedClientIdentifier, auth, restrictions, responseInformation, serverReference, userProperties);
    }

}
