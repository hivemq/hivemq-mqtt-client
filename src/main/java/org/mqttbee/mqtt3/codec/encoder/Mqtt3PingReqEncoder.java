package org.mqttbee.mqtt3.codec.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt3.message.Mqtt3MessageType;
import org.mqttbee.mqtt3.message.pingreq.Mqtt3PingReqImpl;

import javax.inject.Singleton;

/**
 * Encoder for the MQTT 3 PINGREQ message.
 * See the <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html#_Toc398718081">specification</a>
 * for details.
 */
// TODO Question: why are the encoders being annotated with javax.inject.Singleton?
@Singleton
public class Mqtt3PingReqEncoder implements Mqtt3MessageEncoder<Mqtt3PingReqImpl> {

    public static final Mqtt3PingReqEncoder INSTANCE = new Mqtt3PingReqEncoder();

    private static final byte FIXED_HEADER = (byte) (Mqtt3MessageType.PINGREQ.getCode() << 4);
    private static final int REMAINING_LENGTH = 0;

    @Override
    public void encode(@NotNull Mqtt3PingReqImpl message, @NotNull Channel channel, @NotNull ByteBuf out) {
        out.writeByte(FIXED_HEADER);
        out.writeByte(REMAINING_LENGTH);
    }
}
