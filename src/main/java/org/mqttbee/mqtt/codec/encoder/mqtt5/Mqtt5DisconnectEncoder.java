package org.mqttbee.mqtt.codec.encoder.mqtt5;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageWithUserPropertiesEncoder.Mqtt5MessageWithOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt.codec.encoder.provider.MqttMessageEncoderProvider;
import org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl;

import static org.mqttbee.mqtt.codec.encoder.mqtt5.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnectProperty.SERVER_REFERENCE;
import static org.mqttbee.mqtt.message.disconnect.MqttDisconnectProperty.SESSION_EXPIRY_INTERVAL;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectEncoder extends
        Mqtt5MessageWithOmissibleReasonCodeEncoder<MqttDisconnectImpl, Mqtt5DisconnectReasonCode, MqttMessageEncoderProvider<MqttDisconnectImpl>> {

    public static final MqttMessageEncoderProvider<MqttDisconnectImpl> PROVIDER = Mqtt5DisconnectEncoder::new;

    private static final int FIXED_HEADER = Mqtt5MessageType.DISCONNECT.getCode() << 4;

    @Override
    protected int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    protected Mqtt5DisconnectReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

    @Override
    int additionalPropertyLength() {
        return intPropertyEncodedLength(message.getRawSessionExpiryInterval(), SESSION_EXPIRY_INTERVAL_FROM_CONNECT) +
                nullablePropertyEncodedLength(message.getRawServerReference());
    }

    @Override
    void encodeAdditionalProperties(@NotNull final ByteBuf out) {
        encodeIntProperty(SESSION_EXPIRY_INTERVAL, message.getRawSessionExpiryInterval(),
                SESSION_EXPIRY_INTERVAL_FROM_CONNECT, out);
        encodeNullableProperty(SERVER_REFERENCE, message.getRawServerReference(), out);
    }

}
