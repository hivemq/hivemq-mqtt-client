package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubrel.Mqtt5PubRelReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5PubRelEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelProperty.REASON_STRING;
import static org.mqttbee.mqtt.message.publish.pubrel.MqttPubRelProperty.USER_PROPERTY;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubRelDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0010;
    private static final int MIN_REMAINING_LENGTH = 2;

    @Inject
    Mqtt5PubRelDecoder() {
    }

    @Override
    @Nullable
    public MqttPubRelImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) throws MqttDecoderException {

        checkFixedHeaderFlags(FLAGS, flags);

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final int packetIdentifier = in.readUnsignedShort();

        Mqtt5PubRelReasonCode reasonCode = DEFAULT_REASON_CODE;
        MqttUTF8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5PubRelReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                throw wrongReasonCode();
            }

            if (in.isReadable()) {
                checkPropertyLengthNoPayload(in);

                while (in.isReadable()) {
                    final int propertyIdentifier = decodePropertyIdentifier(in);

                    switch (propertyIdentifier) {
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
            }
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttPubRelImpl(
                packetIdentifier, reasonCode, reasonString, userProperties, Mqtt5PubRelEncoder.PROVIDER);
    }

}
