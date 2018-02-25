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
import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.datatypes.*;
import org.mqttbee.mqtt.message.auth.MqttEnhancedAuthImpl;
import org.mqttbee.mqtt.message.connect.connack.MqttConnAckImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.*;
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
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) throws MqttDecoderException {

        final Channel channel = clientConnectionData.getChannel();

        checkFixedHeaderFlags(FLAGS, flags);

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final short connAckFlags = in.readUnsignedByte();
        if ((connAckFlags & 0xFE) != 0) {
            throw new MqttDecoderException("wrong CONNACK flags, bits 7-1 must be 0");
        }
        final boolean sessionPresent = (connAckFlags & 0x1) != 0;

        final Mqtt5ConnAckReasonCode reasonCode = Mqtt5ConnAckReasonCode.fromCode(in.readUnsignedByte());
        if (reasonCode == null) {
            throw wrongReasonCode();
        }

        if ((reasonCode != Mqtt5ConnAckReasonCode.SUCCESS) && sessionPresent) {
            throw new MqttDecoderException("session present must be 0 if reason code is not SUCCESS");
        }

        checkPropertyLengthNoPayload(in);

        long sessionExpiryInterval = SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
        MqttClientIdentifierImpl assignedClientIdentifier = CLIENT_IDENTIFIER_FROM_CONNECT;
        int serverKeepAlive = KEEP_ALIVE_FROM_CONNECT;

        MqttUTF8StringImpl authMethod = null;
        ByteBuffer authData = null;

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
            final int propertyIdentifier = decodePropertyIdentifier(in);

            switch (propertyIdentifier) {
                case SESSION_EXPIRY_INTERVAL:
                    sessionExpiryInterval = decodeSessionExpiryInterval(sessionExpiryInterval, in);
                    break;

                case ASSIGNED_CLIENT_IDENTIFIER:
                    if (assignedClientIdentifier != CLIENT_IDENTIFIER_FROM_CONNECT) {
                        throw moreThanOnce("client identifier");
                    }
                    assignedClientIdentifier = MqttClientIdentifierImpl.from(in);
                    if (assignedClientIdentifier == null) {
                        throw malformedUTF8String("client identifier");
                    }
                    break;

                case SERVER_KEEP_ALIVE:
                    serverKeepAlive =
                            unsignedShortOnlyOnce(serverKeepAlive, KEEP_ALIVE_FROM_CONNECT, "server keep alive", in);
                    break;

                case AUTHENTICATION_METHOD:
                    authMethod = decodeAuthMethod(authMethod, in);
                    break;

                case AUTHENTICATION_DATA:
                    authData = decodeAuthData(authData, in, channel);
                    break;

                case RECEIVE_MAXIMUM:
                    receiveMaximum = unsignedShortOnlyOnce(receiveMaximumPresent, "receive maximum", in);
                    if (receiveMaximum == 0) {
                        throw new MqttDecoderException(
                                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "receive maximum must not be 0");
                    }
                    receiveMaximumPresent = true;
                    restrictionsPresent |= receiveMaximum != Restrictions.DEFAULT_RECEIVE_MAXIMUM;
                    break;

                case TOPIC_ALIAS_MAXIMUM:
                    topicAliasMaximum = unsignedShortOnlyOnce(topicAliasMaximumPresent, "receive maximum", in);
                    topicAliasMaximumPresent = true;
                    restrictionsPresent |= topicAliasMaximum != Restrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM;
                    break;

                case MAXIMUM_QOS:
                    final short maximumQoSCode = unsignedByteOnlyOnce(maximumQoSPresent, "maximum QoS", in);
                    if (maximumQoSCode != 0 && maximumQoSCode != 1) {
                        throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "wrong maximum QoS");
                    }
                    maximumQoS = MqttQoS.fromCode(maximumQoSCode);
                    maximumQoSPresent = true;
                    restrictionsPresent |= maximumQoS != Restrictions.DEFAULT_MAXIMUM_QOS;
                    break;

                case RETAIN_AVAILABLE:
                    retainAvailable = booleanOnlyOnce(retainAvailablePresent, "retain available", in);
                    retainAvailablePresent = true;
                    restrictionsPresent |= retainAvailable != Restrictions.DEFAULT_RETAIN_AVAILABLE;
                    break;

                case MAXIMUM_PACKET_SIZE:
                    final long maximumPacketSizeTemp =
                            unsignedIntOnlyOnce(maximumPacketSizePresent, "maximum packet size", in);
                    if (maximumPacketSizeTemp == 0) {
                        throw new MqttDecoderException(
                                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "maximum packet size must not be 0");
                    }
                    maximumPacketSizePresent = true;
                    if (maximumPacketSizeTemp < MqttVariableByteInteger.MAXIMUM_PACKET_SIZE_LIMIT) {
                        maximumPacketSize = (int) maximumPacketSizeTemp;
                        restrictionsPresent = true;
                    }
                    break;

                case WILDCARD_SUBSCRIPTION_AVAILABLE:
                    wildCardSubscriptionAvailable =
                            booleanOnlyOnce(wildCardSubscriptionAvailablePresent, "wildcard subscription available",
                                    in);
                    wildCardSubscriptionAvailablePresent = true;
                    restrictionsPresent |=
                            wildCardSubscriptionAvailable != Restrictions.DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE;
                    break;

                case SUBSCRIPTION_IDENTIFIER_AVAILABLE:
                    subscriptionIdentifierAvailable =
                            booleanOnlyOnce(subscriptionIdentifierAvailablePresent, "subscription identifier available",
                                    in);
                    subscriptionIdentifierAvailablePresent = true;
                    restrictionsPresent |=
                            subscriptionIdentifierAvailable != Restrictions.DEFAULT_SUBSCRIPTION_IDENTIFIER_AVAILABLE;
                    break;

                case SHARED_SUBSCRIPTION_AVAILABLE:
                    sharedSubscriptionAvailable =
                            booleanOnlyOnce(sharedSubscriptionAvailablePresent, "shared subscription available", in);
                    sharedSubscriptionAvailablePresent = true;
                    restrictionsPresent |=
                            sharedSubscriptionAvailable != Restrictions.DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE;
                    break;

                case RESPONSE_INFORMATION:
                    if (!clientConnectionData.isResponseInformationRequested()) { // TODO
                        throw new MqttDecoderException(
                                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                                "response information must not be included if it was not requested");
                    }
                    responseInformation = decodeUTF8StringOnlyOnce(responseInformation, "response information", in);
                    break;

                case SERVER_REFERENCE:
                    serverReference = decodeServerReference(serverReference, in);
                    break;

                case REASON_STRING:
                    reasonString = decodeReasonString(reasonString, in);
                    break;

                case USER_PROPERTY:
                    userPropertiesBuilder = decodeUserProperty(userPropertiesBuilder, in);
                    break;

                default:
                    throw wrongProperty(propertyIdentifier);
            }
        }

        MqttEnhancedAuthImpl enhancedAuth = null;
        if (authMethod != null) {
            enhancedAuth = new MqttEnhancedAuthImpl(authMethod, authData);
        } else if (authData != null) {
            throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "auth data must not be included if auth method is absent");
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
