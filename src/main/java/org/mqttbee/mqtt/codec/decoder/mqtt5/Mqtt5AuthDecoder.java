package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5AuthEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.message.auth.MqttAuthImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
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
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) throws MqttDecoderException {

        final Channel channel = clientConnectionData.getChannel();

        checkFixedHeaderFlags(FLAGS, flags);

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final Mqtt5AuthReasonCode reasonCode = Mqtt5AuthReasonCode.fromCode(in.readUnsignedByte());
        if (reasonCode == null) {
            throw wrongReasonCode();
        }

        checkPropertyLengthNoPayload(in);

        MqttUTF8StringImpl method = null;
        ByteBuffer data = null;
        MqttUTF8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        while (in.isReadable()) {
            final int propertyIdentifier = decodePropertyIdentifier(in);

            switch (propertyIdentifier) {
                case AUTHENTICATION_METHOD:
                    method = decodeAuthMethod(method, in);
                    break;

                case AUTHENTICATION_DATA:
                    data = decodeAuthData(data, in, channel);
                    break;

                case REASON_STRING:
                    reasonString = decodeReasonStringIfRequested(reasonString, clientConnectionData, in);
                    break;

                case USER_PROPERTY:
                    userPropertiesBuilder =
                            decodeUserPropertyIfRequested(userPropertiesBuilder, clientConnectionData, in);
                    break;

                default:
                    throw wrongProperty(propertyIdentifier);
            }
        }

        if (method == null) {
            throw new MqttDecoderException(
                    Mqtt5DisconnectReasonCode.PROTOCOL_ERROR, "must not omit authentication method");
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttAuthImpl(reasonCode, method, data, reasonString, userProperties, Mqtt5AuthEncoder.PROVIDER);
    }

}
