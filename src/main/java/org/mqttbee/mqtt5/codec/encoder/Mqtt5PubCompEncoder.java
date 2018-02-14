package org.mqttbee.mqtt5.codec.encoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.pubcomp.Mqtt5PubCompReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageWithPropertiesEncoder.Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl;

import java.util.function.Function;

import static org.mqttbee.mqtt5.message.pubcomp.Mqtt5PubCompImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubCompEncoder
        extends Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<Mqtt5PubCompImpl, Mqtt5PubCompReasonCode> {

    public static final Function<Mqtt5PubCompImpl, Mqtt5PubCompEncoder> PROVIDER = Mqtt5PubCompEncoder::new;

    private static final int FIXED_HEADER = Mqtt5MessageType.PUBCOMP.getCode() << 4;

    private Mqtt5PubCompEncoder(@NotNull final Mqtt5PubCompImpl pubComp) {
        super(pubComp);
    }

    @Override
    protected int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    protected Mqtt5PubCompReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

}
