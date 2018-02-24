package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5DisconnectEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnectProperty.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5DisconnectDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;

    @Inject
    Mqtt5DisconnectDecoder() {
    }

    @Override
    @Nullable
    public MqttDisconnectImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("DISCONNECT", channel);
            return null;
        }

        Mqtt5DisconnectReasonCode reasonCode = DEFAULT_REASON_CODE;
        long sessionExpiryInterval = SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
        MqttUTF8StringImpl serverReference = null;
        MqttUTF8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5DisconnectReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                disconnectWrongReasonCode("DISCONNECT", channel);
                return null;
            }

            if (in.isReadable()) {
                final int propertyLength = MqttVariableByteInteger.decode(in);
                if (propertyLength < 0) {
                    disconnectMalformedPropertyLength(channel);
                    return null;
                }
                if (in.readableBytes() != propertyLength) {
                    disconnectMustNotHavePayload("DISCONNECT", channel);
                    return null;
                }

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

                        case SERVER_REFERENCE:
                            serverReference =
                                    decodeUTF8StringOnlyOnce(serverReference, "server reference", channel, in);
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
                            disconnectWrongProperty("DISCONNECT", channel);
                            return null;
                    }
                }
            }
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttDisconnectImpl(reasonCode, sessionExpiryInterval, serverReference, reasonString, userProperties,
                Mqtt5DisconnectEncoder.PROVIDER);
    }

}
