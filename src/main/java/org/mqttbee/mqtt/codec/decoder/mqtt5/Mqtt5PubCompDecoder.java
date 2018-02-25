package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.publish.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5PubCompEncoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.*;
import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompProperty.REASON_STRING;
import static org.mqttbee.mqtt.message.publish.pubcomp.MqttPubCompProperty.USER_PROPERTY;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5PubCompDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 2;

    @Inject
    Mqtt5PubCompDecoder() {
    }

    @Override
    @Nullable
    public MqttPubCompImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("PUBCOMP", channel);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        final int packetIdentifier = in.readUnsignedShort();

        Mqtt5PubCompReasonCode reasonCode = DEFAULT_REASON_CODE;
        MqttUTF8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        if (in.isReadable()) {
            reasonCode = Mqtt5PubCompReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                disconnectWrongReasonCode("PUBCOMP", channel);
                return null;
            }

            if (in.isReadable()) {
                final int propertyLength = MqttVariableByteInteger.decode(in);
                if (propertyLength < 0) {
                    disconnectMalformedPropertyLength(channel);
                    return null;
                }
                if (in.readableBytes() != propertyLength) {
                    disconnectMustNotHavePayload("PUBCOMP", channel);
                    return null;
                }

                while (in.isReadable()) {

                    final int propertyIdentifier = MqttVariableByteInteger.decode(in);
                    if (propertyIdentifier < 0) {
                        disconnectMalformedPropertyIdentifier(channel);
                        return null;
                    }

                    switch (propertyIdentifier) {
                        case REASON_STRING:
                            reasonString = decodeReasonStringCheckProblemInformationRequested(reasonString,
                                    clientConnectionData, in);
                            if (reasonString == null) {
                                return null;
                            }
                            break;

                        case USER_PROPERTY:
                            userPropertiesBuilder =
                                    decodeUserPropertyCheckProblemInformationRequested(userPropertiesBuilder,
                                            clientConnectionData, in);
                            if (userPropertiesBuilder == null) {
                                return null;
                            }
                            break;

                        default:
                            disconnectWrongProperty("PUBCOMP", channel);
                            return null;
                    }
                }
            }
        }

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttPubCompImpl(
                packetIdentifier, reasonCode, reasonString, userProperties, Mqtt5PubCompEncoder.PROVIDER);
    }

}
