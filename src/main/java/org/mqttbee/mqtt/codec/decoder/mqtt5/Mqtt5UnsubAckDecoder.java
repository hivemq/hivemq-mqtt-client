package org.mqttbee.mqtt.codec.decoder.mqtt5;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAckReasonCode;
import org.mqttbee.mqtt.MqttClientConnectionDataImpl;
import org.mqttbee.mqtt.codec.decoder.MqttDecoderException;
import org.mqttbee.mqtt.codec.decoder.MqttMessageDecoder;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertyImpl;
import org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAck;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.checkFixedHeaderFlags;
import static org.mqttbee.mqtt.codec.decoder.MqttMessageDecoderUtil.remainingLengthTooShort;
import static org.mqttbee.mqtt.codec.decoder.mqtt5.Mqtt5MessageDecoderUtil.*;
import static org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAckProperty.REASON_STRING;
import static org.mqttbee.mqtt.message.unsubscribe.unsuback.MqttUnsubAckProperty.USER_PROPERTY;

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
    public MqttUnsubAck decode(
            final int flags, @NotNull final ByteBuf in,
            @NotNull final MqttClientConnectionDataImpl clientConnectionData) throws MqttDecoderException {

        checkFixedHeaderFlags(FLAGS, flags);

        if (in.readableBytes() < MIN_REMAINING_LENGTH) {
            throw remainingLengthTooShort();
        }

        final int packetIdentifier = in.readUnsignedShort();

        final int propertyLength = decodePropertyLength(in);

        MqttUTF8StringImpl reasonString = null;
        ImmutableList.Builder<MqttUserPropertyImpl> userPropertiesBuilder = null;

        final int propertiesStartIndex = in.readerIndex();
        int readPropertyLength;
        while ((readPropertyLength = in.readerIndex() - propertiesStartIndex) < propertyLength) {

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

        if (readPropertyLength != propertyLength) {
            throw malformedPropertyLength();
        }

        final int reasonCodeCount = in.readableBytes();
        if (reasonCodeCount == 0) {
            throw noReasonCodes();
        }

        final ImmutableList.Builder<Mqtt5UnsubAckReasonCode> reasonCodesBuilder =
                ImmutableList.builderWithExpectedSize(reasonCodeCount);
        for (int i = 0; i < reasonCodeCount; i++) {
            final Mqtt5UnsubAckReasonCode reasonCode = Mqtt5UnsubAckReasonCode.fromCode(in.readUnsignedByte());
            if (reasonCode == null) {
                throw wrongReasonCode();
            }
            reasonCodesBuilder.add(reasonCode);
        }
        final ImmutableList<Mqtt5UnsubAckReasonCode> reasonCodes = reasonCodesBuilder.build();

        final MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.build(userPropertiesBuilder);

        return new MqttUnsubAck(packetIdentifier, reasonCodes, reasonString, userProperties);
    }

}
