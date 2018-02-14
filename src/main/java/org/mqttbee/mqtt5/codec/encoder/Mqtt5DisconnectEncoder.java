package org.mqttbee.mqtt5.codec.encoder;

import io.netty.buffer.ByteBuf;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageWithPropertiesEncoder.Mqtt5MessageWithOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl;

import java.util.function.Function;

import static org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoderUtil.*;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.DEFAULT_REASON_CODE;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectImpl.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectProperty.SERVER_REFERENCE;
import static org.mqttbee.mqtt5.message.disconnect.Mqtt5DisconnectProperty.SESSION_EXPIRY_INTERVAL;

/**
 * @author Silvio Giebl
 */
public class Mqtt5DisconnectEncoder
        extends Mqtt5MessageWithOmissibleReasonCodeEncoder<Mqtt5DisconnectImpl, Mqtt5DisconnectReasonCode> {

    public static final Function<Mqtt5DisconnectImpl, Mqtt5DisconnectEncoder> PROVIDER = Mqtt5DisconnectEncoder::new;

    private static final int FIXED_HEADER = Mqtt5MessageType.DISCONNECT.getCode() << 4;

    private Mqtt5DisconnectEncoder(@NotNull final Mqtt5DisconnectImpl disconnect) {
        super(disconnect);
    }

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
