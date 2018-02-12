package org.mqttbee.mqtt3.message.connack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.api.mqtt3.message.Mqtt3ConnAck;
import org.mqttbee.mqtt3.message.Mqtt3Message;

/**
 * @author Daniel Krüger
 */
public class Mqtt3ConnAckImpl implements Mqtt3ConnAck, Mqtt3Message {

    private final Mqtt3ConnAckReasonCode reasonCode;
    private final boolean isSessionPresent;

    public Mqtt3ConnAckImpl(final Mqtt3ConnAckReasonCode reasonCode, final boolean isSessionPresent) {
        this.reasonCode = reasonCode;
        this.isSessionPresent = isSessionPresent;
    }

    @NotNull
    @Override
    public Mqtt3ConnAckReasonCode getReasonCode() {
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
