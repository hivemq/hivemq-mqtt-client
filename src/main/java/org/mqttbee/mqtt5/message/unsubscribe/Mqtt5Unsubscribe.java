package org.mqttbee.mqtt5.message.unsubscribe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.mqtt5.codec.encoder.Mqtt5MessageEncoders;
import org.mqttbee.mqtt5.message.Mqtt5Message;
import org.mqttbee.mqtt5.message.Mqtt5MessageType;

/**
 * @author Silvio Giebl
 */
public class Mqtt5Unsubscribe implements Mqtt5Message {

//    private final int unsubscribeIdentifier; // TODO remove?
//    private final List<String> topicFilters;
//    private final List<Mqtt5UserProperty> userProperties;

    @NotNull
    @Override
    public Mqtt5MessageType getType() {
        return Mqtt5MessageType.UNSUBSCRIBE;
    }

    @Override
    public void encode(
            @NotNull final Mqtt5MessageEncoders encoders, @NotNull final Channel channel, @NotNull final ByteBuf out) {
        encoders.getUnsubscribeEncoder().encode(this, channel, out);
    }

}
