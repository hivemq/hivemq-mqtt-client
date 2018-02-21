package org.mqttbee.mqtt3.message.connack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import org.mqttbee.api.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import org.mqttbee.mqtt3.message.Mqtt3Message;

/**
 * @author Daniel Krüger
 */
public class Mqtt3ConnAckImpl implements Mqtt3ConnAck, Mqtt3Message {

    private final Mqtt3ConnAckReturnCode reasonCode;
    private final boolean isSessionPresent;

    public Mqtt3ConnAckImpl(final Mqtt3ConnAckReturnCode reasonCode, final boolean isSessionPresent) {
        this.reasonCode = reasonCode;
        this.isSessionPresent = isSessionPresent;
    }

    @NotNull
    @Override
    public Mqtt3ConnAckReturnCode getReasonCode() {
        return reasonCode;
    }

    @Override
    public boolean isSessionPresent() {
        return this.isSessionPresent;
    }

    @Override
    public void encode(@NotNull final Channel channel, @NotNull final ByteBuf out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int encodedLength() {
        throw new UnsupportedOperationException();
    }

}
