package org.mqttbee.mqtt5.codec.decoder;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.mqtt5.codec.Mqtt5DataTypes;
import org.mqttbee.mqtt5.message.Mqtt5ClientIdentifier;
import org.mqttbee.mqtt5.message.Mqtt5UTF8String;
import org.mqttbee.mqtt5.message.Mqtt5UserProperty;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckImpl;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckProperty;
import org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckReasonCode;

import javax.inject.Singleton;

import static org.mqttbee.mqtt5.message.connack.Mqtt5ConnAckImpl.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnAckDecoder implements Mqtt5MessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3;

    @Nullable
    @Override
    public Mqtt5ConnAckImpl decode(final int flags, @NotNull final Channel channel, @NotNull final ByteBuf in) {
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

        int receiveMaximum = Restrictions.DEFAULT_RECEIVE_MAXIMUM;
        boolean receiveMaximumPresent = false;
        int topicAliasMaximum = Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM;
        boolean topicAliasMaximumPresent = false;
        byte maximumQoS = Restrictions.DEFAULT_MAXIMUM_QOS;
        boolean maximumQoSPresent = false;
        boolean retainAvailable = Restrictions.DEFAULT_RETAIN_AVAILABLE;
        boolean retainAvailablePresent = false;
        long maximumPacketSize = Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;
        boolean maximumPacketSizePresent = false;
        boolean wildCardSubscriptionAvailable = Restrictions.DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE;
        boolean wildCardSubscriptionAvailablePresent = false;
        boolean subscriptionIdentifierAvailable = Restrictions.DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE;
        boolean subscriptionIdentifierAvailablePresent = false;
        boolean sharedSubscriptionAvailable = Restrictions.DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE;
        boolean sharedSubscriptionAvailablePresent = false;

        Mqtt5UTF8String responseInformation = null;
        Mqtt5UTF8String serverReference = null;
        Mqtt5UTF8String reasonString = null;
        ImmutableList.Builder<Mqtt5UserProperty> userPropertiesBuilder = null;

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
                case Mqtt5ConnAckProperty.SESSION_EXPIRY_INTERVAL:
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
                case Mqtt5ConnAckProperty.ASSIGNED_CLIENT_IDENTIFIER:
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
                case Mqtt5ConnAckProperty.SERVER_KEEP_ALIVE:
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
                case Mqtt5ConnAckProperty.AUTHENTICATION_METHOD:
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
                case Mqtt5ConnAckProperty.AUTHENTICATION_DATA:
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
                case Mqtt5ConnAckProperty.RECEIVE_MAXIMUM:
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
                case Mqtt5ConnAckProperty.TOPIC_ALIAS_MAXIMUM:
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
                case Mqtt5ConnAckProperty.MAXIMUM_QOS:
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
                case Mqtt5ConnAckProperty.RETAIN_AVAILABLE:
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
                case Mqtt5ConnAckProperty.MAXIMUM_PACKET_SIZE:
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
                case Mqtt5ConnAckProperty.WILDCARD_SUBSCRIPTION_AVAILABLE:
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
                case Mqtt5ConnAckProperty.SUBSCRIPTION_IDENTIFIER_AVAILABLE:
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
                case Mqtt5ConnAckProperty.SHARED_SUBSCRIPTION_AVAILABLE:
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
                case Mqtt5ConnAckProperty.RESPONSE_INFORMATION:
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
                case Mqtt5ConnAckProperty.SERVER_REFERENCE:
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
                case Mqtt5ConnAckProperty.REASON_STRING:
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
                case Mqtt5ConnAckProperty.USER_PROPERTY:
                    if (userPropertiesBuilder == null) {
                        userPropertiesBuilder = ImmutableList.builder();
                    }
                    final Mqtt5UserProperty userProperty = Mqtt5UserProperty.decode(in);
                    if (userProperty == null) {
                        // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                        in.clear();
                        return null;
                    }
                    userPropertiesBuilder.add(userProperty);
                    break;
                default:
                    // TODO: send Disconnect with reason code 0x81 Malformed Packet and close channel
                    in.clear();
                    return null;
            }
        }

        AuthImpl auth = AuthImpl.DEFAULT_NO_AUTH;
        if (authPresent) {
            auth = new AuthImpl(authenticationMethod, authenticationData);
        }

        RestrictionsImpl restrictions = RestrictionsImpl.DEFAULT;
        if (restrictionsPresent) {
            restrictions = new RestrictionsImpl(
                    receiveMaximum, topicAliasMaximum, maximumPacketSize, maximumQoS, retainAvailable,
                    wildCardSubscriptionAvailable, subscriptionIdentifierAvailable, sharedSubscriptionAvailable);
        }

        ImmutableList<Mqtt5UserProperty> userProperties = Mqtt5UserProperty.DEFAULT_NO_USER_PROPERTIES;
        if (userPropertiesBuilder != null) {
            userProperties = userPropertiesBuilder.build();
        }

        return new Mqtt5ConnAckImpl(
                reasonCode, sessionPresent, sessionExpiryInterval, serverKeepAlive, assignedClientIdentifier, auth,
                restrictions, responseInformation, serverReference, reasonString, userProperties);
    }

}
