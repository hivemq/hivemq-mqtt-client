package org.mqttbee.mqtt5.codec.encoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.publish.pubrec.Mqtt5PubRecReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageWithPropertiesEncoder.Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.publish.pubrec.Mqtt5PubRecImpl;

import java.util.function.Function;

import static org.mqttbee.mqtt5.message.publish.pubrec.Mqtt5PubRecImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRecEncoder
        extends Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<Mqtt5PubRecImpl, Mqtt5PubRecReasonCode> {

    public static final Function<Mqtt5PubRecImpl, Mqtt5PubRecEncoder> PROVIDER = Mqtt5PubRecEncoder::new;

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBREC.getCode() << 4;

    private Mqtt5PubRecEncoder(@NotNull final Mqtt5PubRecImpl pubRec) {
        super(pubRec);
    }

    @Override
    protected int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    protected Mqtt5PubRecReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

}
