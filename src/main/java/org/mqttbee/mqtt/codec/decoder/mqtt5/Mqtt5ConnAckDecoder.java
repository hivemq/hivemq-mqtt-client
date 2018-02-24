package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttQoS;
import org.mqttbee.api.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuthImpl;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckImpl;
import org.mqttbee.mqtt5.netty.ChannelAttributes;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt.message.connect.connack.MqttConnAckImpl.*;
import static org.mqttbee.mqtt.message.connect.connack.MqttConnAckProperty.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnAckDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3;

    @Inject
    Mqtt5ConnAckDecoder() {
    }

    @Nullable
    @Override
    public MqttConnAckImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("CONNACK", channel);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        final short connAckFlags = in.readUnsignedByte();
        if ((connAckFlags & 0xFE) != 0) {
            disconnect(Mqtt5DisconnectReasonCode.MALFORMED_PACKET, "wrong CONNACK flags", channel);
            return null;
        }
        final boolean sessionPresent = (connAckFlags & 0x1) != 0;

        final Mqtt5ConnAckReasonCode reasonCode = Mqtt5ConnAckReasonCode.fromCode(in.readUnsignedByte());
        if (reasonCode == null) {
            disconnectWrongReasonCode("CONNACK", channel);
            return null;
        }

        if ((reasonCode != Mqtt5ConnAckReasonCode.SUCCESS) && sessionPresent) {
            disconnect(
                    Mqtt5DisconnectReasonCode.MALFORMED_PACKET,
                    "session present must be false if reason code is not SUCCESS", channel);
            return null;
        }

        final int propertyLength = MqttVariableByteInteger.decode(in);

        if (propertyLength < 0) {
            disconnectMalformedPropertyLength(channel);
            return null;
        }

        if (in.readableBytes() != propertyLength) {
            if (in.readableBytes() < propertyLength) {
                disconnectRemainingLengthTooShort(channel);
            } else {
                disconnectMustNotHavePayload("CONNACK", channel);
            }
            return null;
        }

        long sessionExpiryInterval = SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
        MqttClientIdentifierImpl assignedClientIdentifier = CLIENT_IDENTIFIER_FROM_CONNECT;
        int serverKeepAlive = KEEP_ALIVE_FROM_CONNECT;

        MqttUTF8StringImpl authenticationMethod = null;
        ByteBuffer authenticationData = null;

        int receiveMaximum = Restrictions.DEFAULT_RECEIVE_MAXIMUM;
        boolean receiveMaximumPresent = false;
        int topicAliasMaximum = Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM;
        boolean topicAliasMaximumPresent = false;
        MqttQoS maximumQoS = Restrictions.DEFAULT_MAXIMUM_QOS;
        boolean maximumQoSPresent = false;
        boolean retainAvailable = Restrictions.DEFAULT_RETAIN_AVAILABLE;
        boolean retainAvailablePresent = false;
        int maximumPacketSize = Restrictions.DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;
        boolean maximumPacketSizePresent = false;
        boolean wildCardSubscriptionAvailable = Restrictions.DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE;
        boolean wildCardSubscriptionAvailablePresent = false;
        boolean subscriptionIdentifierAvailable = Restrictions.DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE;
        boolean subscriptionIdentifierAvailablePresent = false;
        boolean sharedSubscriptionAvailable = Restrictions.DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE;
        boolean sharedSubscriptionAvailablePresent = false;

        MqttUTF8StringImpl responseInformation = null;
        MqttUTF8StringImpl serverReference = null;
        MqttUTF8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        boolean restrictionsPresent = false;

        while (in.isReadable()) {

            final int propertyIdentifier = MqttVariableByteInteger.decode(in);
            if (propertyIdentifier < 0) {
                disconnectMalformedPropertyIdentifier(channel);
                return null;
            }

            switch (propertyIdentifier) {
                case SESSION_EXPIRY_INTERVAL:
                    if (!checkIntOnlyOnce(sessionExpiryInterval, SESSION_EXPIRY_INTERVAL_FROM_CONNECT,
                            "session expiry interval", channel, in)) {
                        return null;
                    }
                    sessionExpiryInterval = in.readUnsignedInt();
                    break;

                case ASSIGNED_CLIENT_IDENTIFIER:
                    if (assignedClientIdentifier != CLIENT_IDENTIFIER_FROM_CONNECT) {
                        disconnectOnlyOnce("client identifier", channel);
                        return null;
                    }
                    assignedClientIdentifier = MqttClientIdentifierImpl.from(in);
                    if (assignedClientIdentifier == null) {
                        disconnectMalformedUTF8String("client identifier", channel);
                        return null;
                    }
                    break;

                case SERVER_KEEP_ALIVE:
                    if (!checkShortOnlyOnce(
                            serverKeepAlive, KEEP_ALIVE_FROM_CONNECT, "server keep alive", channel, in)) {
                        return null;
                    }
                    serverKeepAlive = in.readUnsignedShort();
                    break;

                case AUTHENTICATION_METHOD:
                    authenticationMethod =
                            decodeUTF8StringOnlyOnce(authenticationMethod, "authentication method", channel, in);
                    if (authenticationMethod == null) {
                        return null;
                    }
                    break;

                case AUTHENTICATION_DATA:
                    authenticationData =
                            decodeBinaryDataOnlyOnce(authenticationData, "authentication data", channel, in,
                                    ChannelAttributes.useDirectBufferForAuth(channel));
                    if (authenticationData == null) {
                        return null;
                    }
                    break;

                case RECEIVE_MAXIMUM:
                    if (!checkShortOnlyOnce(receiveMaximumPresent, "receive maximum", channel, in)) {
                        return null;
                    }
                    receiveMaximum = in.readUnsignedShort();
                    if (receiveMaximum == 0) {
                        disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "receive maximum must not be 0", channel);
                        return null;
                    }
                    receiveMaximumPresent = true;
                    if (receiveMaximum != Restrictions.DEFAULT_RECEIVE_MAXIMUM) {
                        restrictionsPresent = true;
                    }
                    break;

                case TOPIC_ALIAS_MAXIMUM:
                    if (!checkShortOnlyOnce(topicAliasMaximumPresent, "receive maximum", channel, in)) {
                        return null;
                    }
                    topicAliasMaximum = in.readUnsignedShort();
                    topicAliasMaximumPresent = true;
                    if (topicAliasMaximum != Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM) {
                        restrictionsPresent = true;
                    }
                    break;

                case MAXIMUM_QOS:
                    if (!checkByteOnlyOnce(maximumQoSPresent, "maximum QoS", channel, in)) {
                        return null;
                    }
                    final byte maximumQoSCode = in.readByte();
                    if (maximumQoSCode != 0 && maximumQoSCode != 1) {
                        disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "wrong maximum QoS", channel);
                        return null;
                    }
                    maximumQoS = MqttQoS.fromCode(maximumQoSCode);
                    maximumQoSPresent = true;
                    restrictionsPresent |= maximumQoS != Restrictions.DEFAULT_MAXIMUM_QOS;
                    break;

                case RETAIN_AVAILABLE:
                    if (!checkByteOnlyOnce(retainAvailablePresent, "retain available", channel, in)) {
                        return null;
                    }
                    final byte retainAvailableByte = in.readByte();
                    if (!checkBoolean(retainAvailableByte, "retain available", channel)) {
                        return null;
                    }
                    retainAvailable = decodeBoolean(retainAvailableByte);
                    retainAvailablePresent = true;
                    restrictionsPresent |= retainAvailable != Restrictions.DEFAULT_RETAIN_AVAILABLE;
                    break;

                case MAXIMUM_PACKET_SIZE:
                    if (!checkIntOnlyOnce(maximumPacketSizePresent, "maximum packet size", channel, in)) {
                        return null;
                    }
                    final long maximumPacketSizeTemp = in.readUnsignedInt();
                    if (maximumPacketSizeTemp == 0) {
                        disconnect(
                                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "maximum packet size must not be 0", channel);
                        return null;
                    }
                    maximumPacketSizePresent = true;
                    if (maximumPacketSizeTemp < MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) {
                        maximumPacketSize = (int) maximumPacketSizeTemp;
                        restrictionsPresent = true;
                    }
                    break;

                case WILDCARD_SUBSCRIPTION_AVAILABLE:
                    if (!checkByteOnlyOnce(
                            wildCardSubscriptionAvailablePresent, "wildcard subscription available", channel, in)) {
                        return null;
                    }
                    final byte wildCardSubscriptionAvailableByte = in.readByte();
                    if (!checkBoolean(wildCardSubscriptionAvailableByte, "wildcard subscription available", channel)) {
                        return null;
                    }
                    wildCardSubscriptionAvailable = decodeBoolean(wildCardSubscriptionAvailableByte);
                    wildCardSubscriptionAvailablePresent = true;
                    restrictionsPresent |=
                            wildCardSubscriptionAvailable != Restrictions.DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE;
                    break;

                case SUBSCRIPTION_IDENTIFIER_AVAILABLE:
                    if (!checkByteOnlyOnce(
                            subscriptionIdentifierAvailablePresent, "subscription identifier available", channel, in)) {
                        return null;
                    }
                    final byte subscriptionIdentifierAvailableByte = in.readByte();
                    if (!checkBoolean(
                            subscriptionIdentifierAvailableByte, "subscription identifier available", channel)) {
                        return null;
                    }
                    subscriptionIdentifierAvailable = decodeBoolean(subscriptionIdentifierAvailableByte);
                    subscriptionIdentifierAvailablePresent = true;
                    restrictionsPresent |=
                            subscriptionIdentifierAvailable != Restrictions.DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE;
                    break;

                case SHARED_SUBSCRIPTION_AVAILABLE:
                    if (!checkByteOnlyOnce(
                            sharedSubscriptionAvailablePresent, "shared subscription available", channel, in)) {
                        return null;
                    }
                    final byte sharedSubscriptionAvailableByte = in.readByte();
                    if (!checkBoolean(sharedSubscriptionAvailableByte, "shared subscription available", channel)) {
                        return null;
                    }
                    sharedSubscriptionAvailable = decodeBoolean(sharedSubscriptionAvailableByte);
                    sharedSubscriptionAvailablePresent = true;
                    restrictionsPresent |=
                            sharedSubscriptionAvailable != Restrictions.DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE;
                    break;

                case RESPONSE_INFORMATION:
                    if (!clientConnectionData.isResponseInformationRequested()) {
                        disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                                "response information must not be included if it was not requested", channel);
                        return null;
                    }
                    responseInformation =
                            decodeUTF8StringOnlyOnce(responseInformation, "response information", channel, in);
                    if (responseInformation == null) {
                        return null;
                    }
                    break;

                case SERVER_REFERENCE:
                    serverReference = decodeUTF8StringOnlyOnce(serverReference, "server reference", channel, in);
                    if (serverReference == null) {
                        return null;
                    }
                    break;

                case REASON_STRING:
                    reasonString = decodeUTF8StringOnlyOnce(reasonString, "reason string", channel, in);
                    if (reasonString == null) {
                        return null;
                    }
                    break;

                case USER_PROPERTY:
                    userPropertiesBuilder = decodeUserProperty(userPropertiesBuilder, channel, in);
                    if (userPropertiesBuilder == null) {
                        return null;
                    }
                    break;

                default:
                    disconnectWrongProperty("CONNACK", channel);
                    return null;
            }
        }

        MqttEnhancedAuthImpl enhancedAuth = null;
        if (authenticationMethod != null) {
            enhancedAuth = new MqttEnhancedAuthImpl(authenticationMethod, authenticationData);
        } else if (authenticationData != null) {
            disconnect(
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "authentication data must not be included if authentication method is absent", channel);
            return null;
        }

        RestrictionsImpl restrictions = RestrictionsImpl.DEFAULT;
        if (restrictionsPresent) {
            restrictions = new RestrictionsImpl(receiveMaximum, topicAliasMaximum, maximumPacketSize, maximumQoS,
                    retainAvailable, wildCardSubscriptionAvailable, subscriptionIdentifierAvailable,
                    sharedSubscriptionAvailable);
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttConnAckImpl(reasonCode, sessionPresent, sessionExpiryInterval, serverKeepAlive,
                assignedClientIdentifier, enhancedAuth, restrictions, responseInformation, serverReference,
                reasonString, userProperties);
    }

}
