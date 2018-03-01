package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionData;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5DisconnectEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnect;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnect.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
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
    public MqttDisconnect decode(
            final int flags, @NotNull final ByteBuf in, @NotNull final MqttClientConnectionData clientConnectionData)
            throws MqttDecoderException {

        checkFixedHeaderFlags(FLAGS, flags);

        Mqtt5DisconnectReasonCode reasonCode = DEFAULT_REASON_CODE;
        long sessionExpiryInterval = SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
        MqttUTF8StringImpl serverReference = null;
        MqttUTF8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5DisconnectReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                throw wrongReasonCode();
            }

            if (in.isReadable()) {
                checkPropertyLengthNoPayload(in);

                while (in.isReadable()) {
                    final int propertyIdentifier = decodePropertyIdentifier(in);

                    switch (propertyIdentifier) {
                        case SESSION_EXPIRY_INTERVAL:
                            sessionExpiryInterval = decodeSessionExpiryInterval(sessionExpiryInterval, in);
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
            }
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttDisconnect(reasonCode, sessionExpiryInterval, serverReference, reasonString, userProperties,
                Mqtt5DisconnectEncoder.PROVIDER);
    }

}
