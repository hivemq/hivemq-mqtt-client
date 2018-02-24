package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5AuthEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.auth.MqttAuthImpl;
import org.mqttbee.mqtt5.netty.ChannelAttributes;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt.message.auth.MqttAuthProperty.*;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5AuthDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 2; // reason code (1) + property length (min 1)

    @Inject
    Mqtt5AuthDecoder() {
    }

    @Override
    @Nullable
    public MqttAuthImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("AUTH", channel);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        final Mqtt5AuthReasonCode reasonCode = Mqtt5AuthReasonCode.fromCode(in.readUnsignedByte());
        if (reasonCode == null) {
            disconnectWrongReasonCode("AUTH", channel);
            return null;
        }

        final int propertyLength = MqttVariableByteInteger.decode(in);
        if (propertyLength < 0) {
            disconnectMalformedPropertyLength(channel);
            return null;
        }
        if (in.readableBytes() != propertyLength) {
            disconnectMustNotHavePayload("AUTH", channel);
            return null;
        }

        MqttUTF8StringImpl method = null;
        ByteBuffer data = null;
        MqttUTF8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        while (in.isReadable()) {

            final int propertyIdentifier = MqttVariableByteInteger.decode(in);
            if (propertyIdentifier < 0) {
                disconnectMalformedPropertyIdentifier(channel);
                return null;
            }

            switch (propertyIdentifier) {
                case AUTHENTICATION_METHOD:
                    method = decodeUTF8StringOnlyOnce(method, "authentication method", channel, in);
                    if (method == null) {
                        return null;
                    }
                    break;

                case AUTHENTICATION_DATA:
                    data = decodeBinaryDataOnlyOnce(data, "authentication data", channel, in,
                            ChannelAttributes.useDirectBufferForAuth(channel));
                    if (data == null) {
                        return null;
                    }
                    break;

                case REASON_STRING:
                    reasonString =
                            decodeReasonStringCheckProblemInformationRequested(reasonString, clientConnectionData, in);
                    if (reasonString == null) {
                        return null;
                    }
                    break;

                case USER_PROPERTY:
                    userPropertiesBuilder = decodeUserPropertyCheckProblemInformationRequested(userPropertiesBuilder,
                            clientConnectionData, in);
                    if (userPropertiesBuilder == null) {
                        return null;
                    }
                    break;

                default:
                    disconnectWrongProperty("AUTH", channel);
                    return null;
            }
        }

        if (method == null) {
            disconnect(Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "AUTH must not omit authentication method", channel);
            return null;
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttAuthImpl(reasonCode, method, data, reasonString, userProperties, Mqtt5AuthEncoder.PROVIDER);
    }

}
