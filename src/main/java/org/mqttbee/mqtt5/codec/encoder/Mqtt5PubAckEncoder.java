package org.mqttbee.mqtt5.codec.encoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.publish.puback.Mqtt5PubAckReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageWithPropertiesEncoder.Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.publish.puback.Mqtt5PubAckImpl;

import java.util.function.Function;

import static org.mqttbee.mqtt5.message.publish.puback.Mqtt5PubAckImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubAckEncoder
        extends Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<Mqtt5PubAckImpl, Mqtt5PubAckReasonCode> {

    public static final Function<Mqtt5PubAckImpl, Mqtt5PubAckEncoder> PROVIDER = Mqtt5PubAckEncoder::new;

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBACK.getCode() << 4;

    private Mqtt5PubAckEncoder(@NotNull final Mqtt5PubAckImpl pubAck) {
        super(pubAck);
    }

    @Override
    protected int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    protected Mqtt5PubAckReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

}
