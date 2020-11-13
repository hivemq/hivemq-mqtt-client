/*
 * Copyright 2018-present HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.client.internal.mqtt.codec.decoder.mqtt5;

import com.hivemq.client.internal.mqtt.codec.decoder.MqttDecoderContext;
import com.hivemq.client.internal.mqtt.codec.decoder.MqttDecoderException;
import com.hivemq.client.internal.mqtt.codec.decoder.MqttMessageDecoder;
import com.hivemq.client.internal.mqtt.datatypes.*;
import com.hivemq.client.internal.mqtt.message.auth.MqttEnhancedAuth;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnAck;
import com.hivemq.client.internal.mqtt.message.connect.MqttConnAckRestrictions;
import com.hivemq.client.internal.util.collections.ImmutableList;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnAckReturnCode;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

import static com.hivemq.client.internal.mqtt.codec.decoder.MqttMessageDecoderUtil.*;
import static com.hivemq.client.internal.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static com.hivemq.client.internal.mqtt.message.connect.MqttConnAck.KEEP_ALIVE_FROM_CONNECT;
import static com.hivemq.client.internal.mqtt.message.connect.MqttConnAck.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
import static com.hivemq.client.internal.mqtt.message.connect.MqttConnAckProperty.*;
import static com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnAckRestrictions.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5ConnAckDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3;

    @Inject
    Mqtt5ConnAckDecoder() {}

    @Override
    public @NotNull MqttConnAck decode(
            final int flags, final @NotNull ByteBuf in, final @NotNull MqttDecoderContext context)
            throws MqttDecoderException {

        checkFixedHeaderFlags(FLAGS, flags);

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            return tryDecodeMqtt3(in);
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
        MqttClientIdentifierImpl assignedClientIdentifier = null;
        int serverKeepAlive = KEEP_ALIVE_FROM_CONNECT;

        MqttUtf8StringImpl authMethod = null;
        ByteBuffer authData = null;

        int receiveMaximum = DEFAULT_RECEIVE_MAXIMUM;
        boolean receiveMaximumPresent = false;
        int topicAliasMaximum = DEFAULT_TOPIC_ALIAS_MAXIMUM;
        boolean topicAliasMaximumPresent = false;
        MqttQos maximumQos = DEFAULT_MAXIMUM_QOS;
        boolean maximumQosPresent = false;
        boolean retainAvailable = DEFAULT_RETAIN_AVAILABLE;
        boolean retainAvailablePresent = false;
        int maximumPacketSize = DEFAULT_MAXIMUM_PACKET_SIZE_NO_LIMIT;
        boolean maximumPacketSizePresent = false;
        boolean wildCardSubscriptionAvailable = DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE;
        boolean wildCardSubscriptionAvailablePresent = false;
        boolean subscriptionIdentifiersAvailable = DEFAULT_SUBSCRIPTION_IDENTIFIERS_AVAILABLE;
        boolean subscriptionIdentifiersAvailablePresent = false;
        boolean sharedSubscriptionAvailable = DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE;
        boolean sharedSubscriptionAvailablePresent = false;

        MqttUtf8StringImpl responseInformation = null;
        MqttUtf8StringImpl serverReference = null;
        MqttUtf8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        boolean restrictionsPresent = false;

        while (in.isReadable()) {
            final int propertyIdentifier = decodePropertyIdentifier(in);

            switch (propertyIdentifier) {
                case SESSION_EXPIRY_INTERVAL:
                    sessionExpiryInterval = decodeSessionExpiryInterval(sessionExpiryInterval, in);
                    break;

                case ASSIGNED_CLIENT_IDENTIFIER:
                    if (assignedClientIdentifier != null) {
                        throw moreThanOnce("client identifier");
                    }
                    assignedClientIdentifier = MqttClientIdentifierImpl.decode(in);
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
                    authData = decodeAuthData(authData, in, context);
                    break;

                case RECEIVE_MAXIMUM:
                    receiveMaximum = unsignedShortOnlyOnce(receiveMaximumPresent, "receive maximum", in);
                    if (receiveMaximum == 0) {
                        throw new MqttDecoderException(
                                Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "receive maximum must not be 0");
                    }
                    receiveMaximumPresent = true;
                    restrictionsPresent |= receiveMaximum != DEFAULT_RECEIVE_MAXIMUM;
                    break;

                case TOPIC_ALIAS_MAXIMUM:
                    topicAliasMaximum = unsignedShortOnlyOnce(topicAliasMaximumPresent, "receive maximum", in);
                    topicAliasMaximumPresent = true;
                    restrictionsPresent |= topicAliasMaximum != DEFAULT_TOPIC_ALIAS_MAXIMUM;
                    break;

                case MAXIMUM_QOS:
                    final short maximumQosCode = unsignedByteOnlyOnce(maximumQosPresent, "maximum Qos", in);
                    if (maximumQosCode != 0 && maximumQosCode != 1) {
                        throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "wrong maximum Qos");
                    }
                    maximumQos = MqttQos.fromCode(maximumQosCode);
                    assert maximumQos != null : "maximumQosCode = 0 or = 1";
                    maximumQosPresent = true;
                    restrictionsPresent |= maximumQos != DEFAULT_MAXIMUM_QOS;
                    break;

                case RETAIN_AVAILABLE:
                    retainAvailable = booleanOnlyOnce(retainAvailablePresent, "retain available", in);
                    retainAvailablePresent = true;
                    restrictionsPresent |= retainAvailable != DEFAULT_RETAIN_AVAILABLE;
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
                    restrictionsPresent |= wildCardSubscriptionAvailable != DEFAULT_WILDCARD_SUBSCRIPTION_AVAILABLE;
                    break;

                case SUBSCRIPTION_IDENTIFIERS_AVAILABLE:
                    subscriptionIdentifiersAvailable = booleanOnlyOnce(subscriptionIdentifiersAvailablePresent,
                            "subscription identifier available", in);
                    subscriptionIdentifiersAvailablePresent = true;
                    restrictionsPresent |=
                            subscriptionIdentifiersAvailable != DEFAULT_SUBSCRIPTION_IDENTIFIERS_AVAILABLE;
                    break;

                case SHARED_SUBSCRIPTION_AVAILABLE:
                    sharedSubscriptionAvailable =
                            booleanOnlyOnce(sharedSubscriptionAvailablePresent, "shared subscription available", in);
                    sharedSubscriptionAvailablePresent = true;
                    restrictionsPresent |= sharedSubscriptionAvailable != DEFAULT_SHARED_SUBSCRIPTION_AVAILABLE;
                    break;

                case RESPONSE_INFORMATION:
                    if (!context.isResponseInformationRequested()) { // TODO
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

        MqttEnhancedAuth enhancedAuth = null;
        if (authMethod != null) {
            enhancedAuth = new MqttEnhancedAuth(authMethod, authData);
        } else if (authData != null) {
            throw new MqttDecoderException(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "auth data must not be included if auth method is absent");
        }

        MqttConnAckRestrictions restrictions = MqttConnAckRestrictions.DEFAULT;
        if (restrictionsPresent) {
            restrictions = new MqttConnAckRestrictions(receiveMaximum, maximumPacketSize, topicAliasMaximum, maximumQos,
                    retainAvailable, wildCardSubscriptionAvailable, sharedSubscriptionAvailable,
                    subscriptionIdentifiersAvailable);
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttConnAck(reasonCode, sessionPresent, sessionExpiryInterval, serverKeepAlive,
                assignedClientIdentifier, enhancedAuth, restrictions, responseInformation, serverReference,
                reasonString, userProperties);
    }

    public @NotNull MqttConnAck tryDecodeMqtt3(final @NotNull ByteBuf in) throws MqttDecoderException {
        if (in.readableBytes() == 2) {
            in.readUnsignedByte(); // ignore connAckFlags
            final Mqtt3ConnAckReturnCode returnCode = Mqtt3ConnAckReturnCode.fromCode(in.readUnsignedByte());
            if (returnCode == Mqtt3ConnAckReturnCode.UNSUPPORTED_PROTOCOL_VERSION) {
                return new MqttConnAck(Mqtt5ConnAckReasonCode.UNSUPPORTED_PROTOCOL_VERSION, false,
                        SESSION_EXPIRY_INTERVAL_FROM_CONNECT, KEEP_ALIVE_FROM_CONNECT, null, null,
                        MqttConnAckRestrictions.DEFAULT, null, null, null, MqttUserPropertiesImpl.NO_USER_PROPERTIES);
            }
        }
        throw remainingLengthTooShort();
    }
}
