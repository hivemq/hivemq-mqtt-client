package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.datatypes.MqttVariableByteInteger;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAckImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.disconnectRemainingLengthTooShort;
import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.disconnectWrongFixedHeaderFlags;
import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAckProperty.REASON_STRING;
import static org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAckProperty.USER_PROPERTY;
import static org.mqttbee.mqtt5.handler.disconnect.MqttDisconnectUtil.disconnect;

/**
 * @author Silvio Giebl
 */
@Singleton
public class Mqtt5UnsubAckDecoder implements MqttMessageDecoder {

    private static final int FLAGS = 0b0000;
    private static final int MIN_REMAINING_LENGTH = 3;

    @Inject
    Mqtt5UnsubAckDecoder() {
    }

    @Override
    @Nullable
    public MqttUnsubAckImpl decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) {

        final Channel channel = clientConnectionData.getChannel();

        if (flags != FLAGS) {
            disconnectWrongFixedHeaderFlags("UNSUBACK", channel);
            return null;
        }

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        final int packetIdentifier = in.readUnsignedShort();

        final int propertyLength = MqttVariableByteInteger.decode(in);
        if (propertyLength < 0) {
            disconnectMalformedPropertyLength(channel);
            return null;
        }
        if (in.readableBytes() < propertyLength) {
            disconnectRemainingLengthTooShort(channel);
            return null;
        }

        MqttUTF8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        final int propertiesStartIndex = in.readerIndex();
        int readPropertyLength;
        while ((readPropertyLength = in.readerIndex() - propertiesStartIndex) < propertyLength) {

            final int propertyIdentifier = MqttVariableByteInteger.decode(in);
            if (propertyIdentifier < 0) {
                disconnectMalformedPropertyIdentifier(channel);
                return null;
            }

            switch (propertyIdentifier) {
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
                    disconnectWrongProperty("UNSUBACK", channel);
                    return null;
            }
        }

        if (readPropertyLength != propertyLength) {
            disconnectMalformedPropertyLength(channel);
            return null;
        }

        final int reasonCodeCount = in.readableBytes();
        if (reasonCodeCount == 0) {
            disconnect(channel, Mqtt5DisconnectReasonCode.PROTOCOL_ERROR,
                    "UNSUBACK must contain at least one reason code");
            return null;
        }

        final ImmutableList.Builder<Mqtt5UnsubAckReasonCode> reasonCodesBuilder =
                ImmutableList.builderWithExpectedSize(reasonCodeCount);
        for (int i = 0; i < reasonCodeCount; i++) {
            final Mqtt5UnsubAckReasonCode reasonCode = Mqtt5UnsubAckReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                disconnectWrongReasonCode("UNSUBACK", channel);
                return null;
            }
            reasonCodesBuilder.add(reasonCode);
        }
        final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes = reasonCodesBuilder.build();

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttUnsubAckImpl(packetIdentifier, reasonCodes, reasonString, userProperties);
    }

}
