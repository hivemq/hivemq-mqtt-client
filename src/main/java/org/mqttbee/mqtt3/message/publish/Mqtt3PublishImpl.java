package org.mqttbee.mqtt3.message.publish;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.mqttbee.annotations.NotNull;
import org.mqttbee.annotations.Nullable;
import org.mqttbee.api.mqtt3.message.Mqtt3Publish;
import org.mqttbee.mqtt3.message.Mqtt3Message;
import org.mqttbee.mqtt5.message.Mqtt5QoS;
import org.mqttbee.mqtt5.message.Mqtt5Topic;

import java.util.Optional;

public class Mqtt3PublishImpl implements Mqtt3Publish, Mqtt3Message {


    public static final int PACKET_ID_NOT_SET = -1;
    private final byte[] payload;
    private final Mqtt5Topic topic;
    private final Mqtt5QoS qos;
    private final boolean isRetained;
    private final boolean isDup;
    private final int packetId;

    public Mqtt3PublishImpl(
            final byte[] payload, final Mqtt5Topic topic, final Mqtt5QoS qos, final boolean isRetained,
            final boolean dup, final int packetId) {
        this.payload = payload;
        this.topic = topic;
        this.qos = qos;
        this.isRetained = isRetained;
        this.isDup = dup;
        this.packetId = packetId;
    }


    @Override
    public void encode(
            @NotNull final Channel channel, @NotNull final ByteBuf out) {
        //Mqtt3PublishEncoder.INSTANCE.encode(this, channel, out);

    }

    @Override
    public int encodedLength() {
        //TODO
        return 0;
    }


    public int getPacketId() {
        return packetId;
    }

    @NotNull
    @Override
    public Mqtt5Topic getTopic() {
        return topic;
    }

    @NotNull
    @Override
    public Optional<byte[]> getPayload() {
        return Optional.ofNullable(payload);
    }

    @Nullable
    public byte[] getRawPayload() {
        return payload;
    }

    @NotNull
    @Override
    public Mqtt5QoS getQos() {
        return qos;
    }

    @Override
    public boolean isRetained() {
        return isRetained;
    }

    public boolean isDup() {
        return isDup;
    }
}
