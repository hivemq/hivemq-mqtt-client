package org.mqttbee.mqtt5.codec.encoder;

import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt5.message.pubrel.Mqtt5PubRelReasonCode;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageWithPropertiesEncoder.Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;
import org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl;

import java.util.function.Function;

import static org.mqttbee.mqtt5.message.pubrel.Mqtt5PubRelImpl.DEFAULT_REASON_CODE;

/**
 * @author Silvio Giebl
 */
public class Mqtt5PubRelEncoder
        extends Mqtt5MessageWithIdAndOmissibleReasonCodeEncoder<Mqtt5PubRelImpl, Mqtt5PubRelReasonCode> {

    public static final Function<Mqtt5PubRelImpl, Mqtt5PubRelEncoder> PROVIDER = Mqtt5PubRelEncoder::new;

    private static final int FIXED_HEADER = (Mqtt5MessageType.PUBREL.getCode() << 4) | 0b0010;

    private Mqtt5PubRelEncoder(@NotNull final Mqtt5PubRelImpl pubRel) {
        super(pubRel);
    }

    @Override
    protected int getFixedHeader() {
        return FIXED_HEADER;
    }

    @Override
    protected Mqtt5PubRelReasonCode getDefaultReasonCode() {
        return DEFAULT_REASON_CODE;
    }

}
